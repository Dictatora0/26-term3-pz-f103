#include "wifi_function.h"
#include "wifi_config.h"
#include "SysTick.h"
#include "led.h"
#include "esp8266_mqtt_config.h"
#include <string.h> 
#include <stdio.h>  
#include <stdbool.h>


#include <stdint.h>



// 定义单连接ID，假设为0，具体根据实际情况
#define Single_ID 0

static u32 s_esp_cmd_seq = 0;
static bool s_mqtt_ready = false;

static char esp_ascii_upper(char c)
{
    if ((c >= 'a') && (c <= 'z')) {
        return (char)(c - ('a' - 'A'));
    }
    return c;
}

static bool esp_icase_contains(const char *src, const char *token)
{
    const char *p;
    const char *s;
    const char *t;
    char cs;
    char ct;

    if ((src == 0) || (token == 0) || (*token == '\0')) {
        return false;
    }

    for (p = src; *p != '\0'; ++p) {
        s = p;
        t = token;
        while ((*s != '\0') && (*t != '\0')) {
            cs = esp_ascii_upper(*s);
            ct = esp_ascii_upper(*t);
            if (cs != ct) {
                break;
            }
            ++s;
            ++t;
        }
        if (*t == '\0') {
            return true;
        }
    }

    return false;
}

static bool esp_has_text(const char *s)
{
    return (s != 0) && (s[0] != '\0');
}

static void esp_log_at_fail_guide(void)
{
    PC_Usart("[ESP] no AT response. Check hardware before debugging MQTT topics.\r\n");
    PC_Usart("[ESP] checklist: 3.3V supply, common GND, PB10->ESP_RX, PB11<-ESP_TX, EN/CH high, RST high\r\n");
    PC_Usart("[ESP] checklist: module must run ESP-AT firmware, not transparent TCP firmware or user app firmware\r\n");
    PC_Usart("[ESP] checklist: if available, connect USB-UART directly to ESP8266 and verify AT manually\r\n");
}

static bool esp_try_at_baud(u32 baud, u32 cmd_wait_ms)
{
    PC_Usart("[ESP] try baud=%lu\r\n", (unsigned long)baud);
    WiFi_SetUsart3Baud(baud);
    ESP8266_AT_Test();
    return ESP8266_Cmd("AT", "OK", NULL, cmd_wait_ms);
}

static void esp_log_rx_hex(u32 cmd_id, const char *buf, u16 len)
{
    u16 i;
    u16 show_len;

    if ((buf == 0) || (len == 0U)) {
        return;
    }

    show_len = len;
    if (show_len > 48U) {
        show_len = 48U;
    }

    PC_Usart("[ESP_CMD %lu] RX_HEX(%u):", (unsigned long)cmd_id, (unsigned int)show_len);
    for (i = 0; i < show_len; ++i) {
        PC_Usart(" %02X", (unsigned char)buf[i]);
    }
    PC_Usart("\r\n");
}

static void esp_mqtt_diag_probe(const char *host, const char *port)
{
    char cCmd[160];
    bool tcp_ok;

    if ((host == 0) || (port == 0)) {
        return;
    }

    PC_Usart("[MQTT_DIAG] broker=%s:%s\r\n", host, port);
    ESP8266_Cmd("AT+CIFSR", "OK", "STAIP", 1500);

    sprintf(cCmd, "AT+PING=\"%s\"", host);
    ESP8266_Cmd(cCmd, "OK", "+PING", 5000);

    sprintf(cCmd, "AT+CIPSTART=\"TCP\",\"%s\",%s", host, port);
    tcp_ok = ESP8266_Cmd(cCmd, "CONNECT", "OK", 6000);
    if (tcp_ok) {
        ESP8266_Cmd("AT+CIPCLOSE", "OK", "CLOSED", 2000);
    } else {
        PC_Usart("[MQTT_DIAG] TCP probe failed\r\n");
    }

    ESP8266_Cmd("AT+MQTTCONN?", "OK", "+MQTTCONN", 1000);
}

static void esp_log_mqtt_profile(void)
{
    PC_Usart("[MQTT_CFG] WiFi SSID=%s\r\n", WIFI_SSID);
    PC_Usart("[MQTT_CFG] Broker=%s:%s\r\n", MQTT_HOST, MQTT_PORT_STR);
    PC_Usart("[MQTT_CFG] Broker target must be the Windows LAN IP forwarded to WSL, never 127.0.0.1 or WSL NAT IP\r\n");
    PC_Usart("[MQTT_CFG] ClientId=%s User=%s KeepAlive=%d QoS=%d\r\n",
             MQTT_CLIENT_ID,
             esp_has_text(MQTT_USERNAME) ? MQTT_USERNAME : "<anonymous>",
             MQTT_KEEPALIVE_SECONDS,
             MQTT_SUB_QOS);
    PC_Usart("[MQTT_CFG] Topics telemetry=%s control=%s status=%s\r\n",
             MQTT_TOPIC_DATA, MQTT_TOPIC_CMD, MQTT_TOPIC_ACK);
}

bool ESP8266_Is_MQTT_Ready(void)
{
    return s_mqtt_ready;
}

static void esp8266_send_raw_line(const char *s)
{
    if (s == 0) {
        return;
    }

    while (*s != 0) {
        USART_SendData(USART3, (u8)*s++);
        while (USART_GetFlagStatus(USART3, USART_FLAG_TXE) == RESET);
    }

    USART_SendData(USART3, (u8)0x0D);
    while (USART_GetFlagStatus(USART3, USART_FLAG_TXE) == RESET);
    USART_SendData(USART3, (u8)0x0A);
    while (USART_GetFlagStatus(USART3, USART_FLAG_TXE) == RESET);
}

static void esp8266_send_raw_bytes(const char *buf, u16 len)
{
    u16 i;

    if ((buf == 0) || (len == 0U)) {
        return;
    }

    for (i = 0; i < len; ++i) {
        USART_SendData(USART3, (u8)buf[i]);
        while (USART_GetFlagStatus(USART3, USART_FLAG_TXE) == RESET);
    }
}

static bool esp8266_wait_reply(u32 cmd_id, const char *reply1, const char *reply2, u32 waittime)
{
    u32 elapsed = 0;
    u16 rx_len;
    bool matched = false;
    bool seen_any = false;

    if ((reply1 == 0) && (reply2 == 0)) {
        PC_Usart("[ESP_CMD %lu] no-reply mode\r\n", (unsigned long)cmd_id);
        return true;
    }

    while (elapsed < waittime) {
        delay_ms(10);
        elapsed += 10;

        if (strEsp8266_Fram_Record.InfBit.FramFinishFlag) {
            rx_len = strEsp8266_Fram_Record.InfBit.FramLength;
            strEsp8266_Fram_Record.Data_RX_BUF[rx_len] = '\0';
            seen_any = true;

            if ((reply1 != 0) && (strstr(strEsp8266_Fram_Record.Data_RX_BUF, reply1) || esp_icase_contains(strEsp8266_Fram_Record.Data_RX_BUF, reply1))) {
                matched = true;
            }
            if ((reply2 != 0) && (strstr(strEsp8266_Fram_Record.Data_RX_BUF, reply2) || esp_icase_contains(strEsp8266_Fram_Record.Data_RX_BUF, reply2))) {
                matched = true;
            }

            if ((strstr(strEsp8266_Fram_Record.Data_RX_BUF, "ERROR") != 0) || esp_icase_contains(strEsp8266_Fram_Record.Data_RX_BUF, "error")) {
                PC_Usart("[ESP_CMD %lu] RX has ERROR\r\n", (unsigned long)cmd_id);
            }
            if ((strstr(strEsp8266_Fram_Record.Data_RX_BUF, "busy") != 0) || esp_icase_contains(strEsp8266_Fram_Record.Data_RX_BUF, "busy")) {
                PC_Usart("[ESP_CMD %lu] RX has busy\r\n", (unsigned long)cmd_id);
            }

            PC_Usart("[ESP_CMD %lu] RX(%u ms,len=%u): %s\r\n",
                     (unsigned long)cmd_id,
                     (unsigned int)elapsed,
                     (unsigned int)rx_len,
                     strEsp8266_Fram_Record.Data_RX_BUF);
            esp_log_rx_hex(cmd_id, strEsp8266_Fram_Record.Data_RX_BUF, rx_len);

            if (matched) {
                strEsp8266_Fram_Record.InfBit.FramLength = 0;
                strEsp8266_Fram_Record.InfBit.FramFinishFlag = 0;
                return true;
            }

            strEsp8266_Fram_Record.InfBit.FramLength = 0;
            strEsp8266_Fram_Record.InfBit.FramFinishFlag = 0;
        }
    }

    if (seen_any) {
        PC_Usart("[ESP_CMD %lu] TIMEOUT unmatched after RX (%lu ms)\r\n", (unsigned long)cmd_id, (unsigned long)waittime);
    } else if (strEsp8266_Fram_Record.InfBit.FramLength > 0) {
        strEsp8266_Fram_Record.Data_RX_BUF[strEsp8266_Fram_Record.InfBit.FramLength] = '\0';
        PC_Usart("[ESP_CMD %lu] TIMEOUT partial: %s\r\n", (unsigned long)cmd_id, strEsp8266_Fram_Record.Data_RX_BUF);
        esp_log_rx_hex(cmd_id, strEsp8266_Fram_Record.Data_RX_BUF, strEsp8266_Fram_Record.InfBit.FramLength);
    } else {
        PC_Usart("[ESP_CMD %lu] TIMEOUT no response (%lu ms)\r\n", (unsigned long)cmd_id, (unsigned long)waittime);
    }

    strEsp8266_Fram_Record.InfBit.FramLength = 0;
    strEsp8266_Fram_Record.InfBit.FramFinishFlag = 0;
    return false;
}
/*
 * 函数名：ESP8266_Choose
 * 描述  ：使能/禁用WF-ESP8266模块
 * 输入  ：enumChoose = ENABLE，使能模块
 *         enumChoose = DISABLE，禁用模块
 * 返回  : 无
 * 调用  ：被外部调用
 */
void ESP8266_Choose ( FunctionalState enumChoose )
{
	if ( enumChoose == ENABLE )
		ESP8266_CH_HIGH_LEVEL();
	
	else
		ESP8266_CH_LOW_LEVEL();
	
}


/*
 * 函数名：ESP8266_Rst
 * 描述  ：重启WF-ESP8266模块
 * 输入  ：无
 * 返回  : 无
 * 调用  ：被ESP8266_AT_Test调用
 */
void ESP8266_Rst ( void )
{
	#if 0
	 ESP8266_Cmd ( "AT+RST", "OK", "ready", 2500 );   	
	
	#else
	 ESP8266_RST_LOW_LEVEL();
	 delay_ms ( 500 ); 
	 ESP8266_RST_HIGH_LEVEL();
	 
	#endif

}


/*
 * 函数名：ESP8266_AT_Test
 * 描述  ：对WF-ESP8266模块进行AT测试启动
 * 输入  ：无
 * 返回  : 无
 * 调用  ：被外部调用
 */
void ESP8266_AT_Test ( void )
{
    u8 i;

    ESP8266_RST_HIGH_LEVEL();
    delay_ms(300);
    PC_Usart("[ESP_AT] probing...\r\n");

    for (i = 0; i < 6; i++)
    {
        if (ESP8266_Cmd("AT", "OK", "ready", 500))
        {
            PC_Usart("[ESP_AT] ok on try %d\r\n", i + 1);
            return;
        }

        if ((i == 1U) || (i == 3U))
        {
            PC_Usart("[ESP_AT] pulse reset\r\n");
            ESP8266_Rst();
            delay_ms(500);
        }
        else
        {
            delay_ms(200);
        }
    }

    PC_Usart("[ESP_AT] failed after retries\r\n");
}


/*
 * 函数名：ESP8266_Cmd
 * 描述  ：对WF-ESP8266模块发送AT指令
 * 输入  ：cmd，待发送的指令
 *         reply1，reply2，期待的响应，为NULL表不需响应，两者为或逻辑关系
 *         waittime，等待响应的时间
 * 返回  : 1，指令发送成功
 *         0，指令发送失败
 * 调用  ：被外部调用
 */
bool ESP8266_Cmd ( const char * cmd, const char * reply1, const char * reply2, u32 waittime )
{
    u32 cmd_id;

    cmd_id = ++s_esp_cmd_seq;
    strEsp8266_Fram_Record.InfBit.FramLength = 0;
    strEsp8266_Fram_Record.InfBit.FramFinishFlag = 0;

    PC_Usart("[ESP_CMD %lu] TX: %s\r\n", (unsigned long)cmd_id, cmd);
    esp8266_send_raw_line(cmd);

    return esp8266_wait_reply(cmd_id, reply1, reply2, waittime);
}


/*
 * 函数名：ESP8266_Net_Mode_Choose
 * 描述  ：选择WF-ESP8266模块的工作模式
 * 输入  ：enumMode，工作模式
 * 返回  : 1，选择成功
 *         0，选择失败
 * 调用  ：被外部调用
 */
bool ESP8266_Net_Mode_Choose ( ENUM_Net_ModeTypeDef enumMode )
{
	switch ( enumMode )
	{
		case STA:
			return ESP8266_Cmd ( "AT+CWMODE=1", "OK", "no change", 2500 ); 
		
	  case AP:
		  return ESP8266_Cmd ( "AT+CWMODE=2", "OK", "no change", 2500 ); 
		
		case STA_AP:
		  return ESP8266_Cmd ( "AT+CWMODE=3", "OK", "no change", 2500 ); 
		
	  default:
		  return false;
  }
	
}


/*
 * 函数名：ESP8266_JoinAP
 * 描述  ：WF-ESP8266模块连接外部WiFi
 * 输入  ：pSSID，WiFi名称字符串
 *       ：pPassWord，WiFi密码字符串
 * 返回  : 1，连接成功
 *         0，连接失败
 * 调用  ：被外部调用
 */
bool ESP8266_JoinAP ( const char * pSSID, const char * pPassWord )
{
	char cCmd [120];

	sprintf ( cCmd, "AT+CWJAP=\"%s\",\"%s\"", pSSID, pPassWord );
	
	return ESP8266_Cmd ( cCmd, "OK", NULL, 20000 );
	
}

/*
 * 函数名：ESP8266_BuildAP
 * 描述  ：WF-ESP8266模块创建WiFi热点
 * 输入  ：pSSID，WiFi名称字符串
 *       ：pPassWord，WiFi密码字符串
 *       ：enunPsdMode，WiFi加密方式代号字符串
 * 返回  : 1，创建成功
 *         0，创建失败
 * 调用  ：被外部调用
 */
bool ESP8266_BuildAP ( const char * pSSID, const char * pPassWord, const char * enunPsdMode )
{
	char cCmd [120];

	sprintf ( cCmd, "AT+CWSAP=\"%s\",\"%s\",1,%s", pSSID, pPassWord, enunPsdMode );
	
	return ESP8266_Cmd ( cCmd, "OK", 0, 1000 );
	
}


/*
 * 函数名：ESP8266_Enable_MUSART3_printfultipleId
 * 描述  ：WF-ESP8266模块启动多连接
 * 输入  ：enumEnUnvarnishTx，配置是否多连接
 * 返回  : 1，配置成功
 *         0，配置失败
 * 调用  ：被外部调用
 */
bool ESP8266_Enable_MultipleId ( FunctionalState enumEnUnvarnishTx )
{
	char cStr [20];
	
	sprintf ( cStr, "AT+CIPMUX=%d", ( enumEnUnvarnishTx ? 1 : 0 ) );
	
	return ESP8266_Cmd ( cStr, "OK", 0, 500 );
	
}


/*
 * 函数名：ESP8266_Link_Server
 * 描述  ：WF-ESP8266模块连接外部服务器
 * 输入  ：enumE，网络协议
 *       ：ip，服务器IP字符串
 *       ：ComNum，服务器端口字符串
 *       ：id，模块连接服务器的ID
 * 返回  : 1，连接成功
 *         0，连接失败
 * 调用  ：被外部调用
 */
bool ESP8266_Link_Server ( ENUM_NetPro_TypeDef enumE, const char * ip, const char * ComNum, ENUM_ID_NO_TypeDef id)
{
	char cStr [100] = { 0 }, cCmd [120];

  switch (  enumE )
  {
		case enumTCP:
		  sprintf ( cStr, "\"%s\",\"%s\",%s", "TCP", ip, ComNum );
		  break;
		
		case enumUDP:
		  sprintf ( cStr, "\"%s\",\"%s\",%s", "UDP", ip, ComNum );
		  break;
		
		default:
			break;
  }

  if ( id < 5 )
    sprintf ( cCmd, "AT+CIPSTART=%d,%s", id, cStr);

  else
	  sprintf ( cCmd, "AT+CIPSTART=%s", cStr );

	return ESP8266_Cmd ( cCmd, "OK", "ALREAY CONNECT", 500 );
	
}




/*
 * ESP8266_Link_MQTT
 * 描述  ：WF-ESP8266模块连接外部服务器
 * 输入  ：enumE，网络协议
 *       ：ip，服务器IP字符串
 *       ：ComNum，服务器端口字符串
 *       ：id，模块连接服务器的ID
 * 返回  : 1，连接成功
 *         0，连接失败
 * 调用  ：被外部调用
 */
bool ESP8266_Link_MQTT (  const char * ip, const char * ComNum, ENUM_ID_NO_TypeDef id)
{
	char cStr [100] = { 0 }, cCmd [120];
    bool ok;

    (void)id;
    PC_Usart("[MQTT_DIAG] MQTT connect start\r\n");
    ESP8266_Cmd("AT+MQTTCONN?", "OK", "+MQTTCONN", 1000);
    esp_mqtt_diag_probe(ip, ComNum);

	sprintf ( cStr, "\"%s\",%s",  ip, ComNum );
    sprintf ( cCmd, "AT+MQTTCONN=0,%s,0", cStr );
	ok = ESP8266_Cmd ( cCmd, "+MQTTCONNECTED", "OK", 30000 );
    if (!ok) {
        PC_Usart("[MQTT_DIAG] MQTTCONN failed\r\n");
        ESP8266_Cmd("AT+MQTTCONN?", "OK", "+MQTTCONN", 1000);
        ESP8266_Cmd("AT+MQTTCLEAN=0", "OK", "ERR CODE", 1500);
        ESP8266_Cmd("AT+MQTTCONN?", "OK", "+MQTTCONN", 1000);
    }
	return ok;
	
}

/*
 * ESP8266_Set_MQTT_User
 * 描述  ：set MQTT user

 * 返回  : 1，连接成功
 *         0，连接失败
 * 调用  ：被外部调用
 */
bool ESP8266_Set_MQTT_User ()
{
	char cCmd [160];
	sprintf ( cCmd, "AT+MQTTUSERCFG=0,1,\"%s\",\"%s\",\"%s\",0,0,\"\"",
              MQTT_CLIENT_ID, MQTT_USERNAME, MQTT_PASSWORD );
	return ESP8266_Cmd ( cCmd, "OK", "ALREAY CONNECT", 500 );
}


bool ESP8266_Set_MQTT_ConnCfg ( void )
{
    char cCmd [160];
    sprintf ( cCmd, "AT+MQTTCONNCFG=0,%d,0,\"\",\"\",0,0", MQTT_KEEPALIVE_SECONDS );
    return ESP8266_Cmd ( cCmd, "OK", NULL, 1000 );
}


/*
 * ESP8266_Set_MQTT_public
 * 描述  ：set MQTT public

 * 返回  : 1，连接成功
 *         0，连接失败
 * 调用  ：被外部调用
 */
bool ESP8266_Set_MQTT_Public (const char * topicId ,const char * val)
{
	char cCmd [220];
    u16 payload_len;
    u32 cmd_id;

    if ((topicId == 0) || (val == 0)) {
        return false;
    }

    payload_len = (u16)strlen(val);
	sprintf ( cCmd, "AT+MQTTPUBRAW=0,\"%s\",%u,0,0", topicId, (unsigned int)payload_len );
    if (strlen(cCmd) >= (sizeof(cCmd) - 2U)) {
        return false;
    }

    cmd_id = ++s_esp_cmd_seq;
    strEsp8266_Fram_Record.InfBit.FramLength = 0;
    strEsp8266_Fram_Record.InfBit.FramFinishFlag = 0;

    PC_Usart("[ESP_CMD %lu] TX: %s\r\n", (unsigned long)cmd_id, cCmd);
    esp8266_send_raw_line(cCmd);
    if (!esp8266_wait_reply(cmd_id, ">", NULL, 2000)) {
        PC_Usart("[MQTT] raw publish prompt failed\r\n");
        return false;
    }

    PC_Usart("[ESP_CMD %lu] TX_RAW(len=%u)\r\n", (unsigned long)cmd_id, (unsigned int)payload_len);
    esp8266_send_raw_bytes(val, payload_len);

	return esp8266_wait_reply(cmd_id, "+MQTTPUB:OK", "OK", 6000 );
}


//su  esp8266 sub mqtt topic function ，parameter is topicId & qos  ,include  parameter declaration 
bool ESP8266_Set_MQTT_Sub (const  char * topicId,const  char * qos)
{
	char cStr [100] = { 0 }, cCmd [120];
	//construct string: AT+MQTTSUB=0,"topic911",1,0
	sprintf ( cCmd, "AT+MQTTSUB=0,\"%s\",%s", topicId, qos );
	PC_Usart("发送订阅指令: %s\r\n", cCmd); // 添加日志输出
	return ESP8266_Cmd (cCmd, "OK", "ALREADY SUBSCRIBE", 1500);
}





/*
 * 函数名：ESP8266_StartOrShutServer
 * 描述  ：WF-ESP8266模块开启或关闭服务器模式
 * 输入  ：enumMode，开启/关闭
 *       ：pPortNum，服务器端口号字符串
 *       ：pTimeOver，服务器超时时间字符串，单位：秒
 * 返回  : 1，操作成功
 *         0，操作失败
 * 调用  ：被外部调用
 */
bool ESP8266_StartOrShutServer ( FunctionalState enumMode, char * pPortNum, char * pTimeOver )
{
	char cCmd1 [120], cCmd2 [120];

	if ( enumMode )
	{
		sprintf ( cCmd1, "AT+CIPSERVER=%d,%s", 1, pPortNum );
		
		sprintf ( cCmd2, "AT+CIPSTO=%s", pTimeOver );

		return ( ESP8266_Cmd ( cCmd1, "OK", 0, 500 ) &&
						 ESP8266_Cmd ( cCmd2, "OK", 0, 500 ) );
	}
	
	else
	{
		sprintf ( cCmd1, "AT+CIPSERVER=%d,%s", 0, pPortNum );

		return ESP8266_Cmd ( cCmd1, "OK", 0, 500 );
	}
	
}


/*
 * 函数名：ESP8266_UnvarnishSend
 * 描述  ：配置WF-ESP8266模块进入透传发送
 * 输入  ：无
 * 返回  : 1，配置成功
 *         0，配置失败
 * 调用  ：被外部调用
 */
bool ESP8266_UnvarnishSend ( void )
{
	return (
	  ESP8266_Cmd ( "AT+CIPMODE=1", "OK", 0, 500 ) &&
	  ESP8266_Cmd ( "AT+CIPSEND", "\r\n", ">", 500 ) );
	
}


/*
 * 函数名：ESP8266_SendString
 * 描述  ：WF-ESP8266模块发送字符串
 * 输入  ：enumEnUnvarnishTx，声明是否已使能了透传模式
 *       ：pStr，要发送的字符串
 *       ：ulStrLength，要发送的字符串的字节数
 *       ：ucId，哪个ID发送的字符串
 * 返回  : 1，发送成功
 *         0，发送失败
 * 调用  ：被外部调用
 */
bool ESP8266_SendString ( FunctionalState enumEnUnvarnishTx, char * pStr, u32 ulStrLength, ENUM_ID_NO_TypeDef ucId )
{
	char cStr [20];
	bool bRet = false;
		
	if ( enumEnUnvarnishTx )
		ESP8266_Usart ( "%s", pStr );

	
	else
	{
		// if ( ucId < 5 )
		// 	sprintf ( cStr, "AT+CIPSEND=%d,%d", ucId, ulStrLength + 2 );

		// else
		// 	sprintf ( cStr, "AT+CIPSEND=%d", ulStrLength + 2 );
		
		sprintf ( cStr, "AT+CIPSEND=%d", ulStrLength + 2 );
		
		ESP8266_Cmd ( cStr, "> ", 0, 1000 );

		bRet = ESP8266_Cmd ( pStr, "SEND OK", 0, 1000 );
  }
	
	return bRet;

}


/*
 * 函数名：ESP8266_ReceiveString
 * 描述  ：WF-ESP8266模块接收字符串
 * 输入  ：enumEnUnvarnishTx，声明是否已使能了透传模式
 * 返回  : 接收到的字符串首地址
 * 调用  ：被外部调用
 */
char * ESP8266_ReceiveString ( FunctionalState enumEnUnvarnishTx )
{
	char * pRecStr = 0;
	
	strEsp8266_Fram_Record .InfBit .FramLength = 0;
	strEsp8266_Fram_Record .InfBit .FramFinishFlag = 0;
	while ( ! strEsp8266_Fram_Record .InfBit .FramFinishFlag );
	strEsp8266_Fram_Record .Data_RX_BUF [ strEsp8266_Fram_Record .InfBit .FramLength ] = '\0';
	
	if ( enumEnUnvarnishTx )
	{
		if ( strstr ( strEsp8266_Fram_Record .Data_RX_BUF, ">" ) )
			pRecStr = strEsp8266_Fram_Record .Data_RX_BUF;

	}
	
	else 
	{
		if ( strstr ( strEsp8266_Fram_Record .Data_RX_BUF, "+IPD" ) ||
		     strstr ( strEsp8266_Fram_Record .Data_RX_BUF, "+MQTTSUBRECV:" ) ||
		     strstr ( strEsp8266_Fram_Record .Data_RX_BUF, "+MQTTDISCONNECTED:" ) )
			pRecStr = strEsp8266_Fram_Record .Data_RX_BUF;

	}

	return pRecStr;
	
}

char * ESP8266_TryReceiveString ( FunctionalState enumEnUnvarnishTx )
{
    char * pRecStr = 0;

    if (!strEsp8266_Fram_Record.InfBit.FramFinishFlag) {
        return 0;
    }

    strEsp8266_Fram_Record.Data_RX_BUF[strEsp8266_Fram_Record.InfBit.FramLength] = '\0';

    if (enumEnUnvarnishTx) {
        if (strstr(strEsp8266_Fram_Record.Data_RX_BUF, ">")) {
            pRecStr = strEsp8266_Fram_Record.Data_RX_BUF;
        }
    } else {
        if ( strstr ( strEsp8266_Fram_Record .Data_RX_BUF, "+IPD" ) ||
             strstr ( strEsp8266_Fram_Record .Data_RX_BUF, "+MQTTSUBRECV:" ) ||
             strstr ( strEsp8266_Fram_Record .Data_RX_BUF, "+MQTTDISCONNECTED:" ) ) {
            pRecStr = strEsp8266_Fram_Record.Data_RX_BUF;
        }
    }

    strEsp8266_Fram_Record.InfBit.FramLength = 0;
    strEsp8266_Fram_Record.InfBit.FramFinishFlag = 0;
    return pRecStr;
}


/*
 * 函数名：ESP8266_STA_TCP_Client
 * 描述  ：PZ-ESP8266模块进行STA TCP Clien测试
 * 输入  ：无
 * 返回  : 无
 * 调用  ：被外部调用
 */
void ESP8266_STA_TCP_Client ( void )
{
	char cStrInput [100] = { 0 }, * pStrDelimiter [2], * pBuf, * pStr;
	u8 uc = 0;
  u32 ul = 0;

	ESP8266_Choose ( ENABLE );	

  ESP8266_AT_Test ();
	
	ESP8266_Net_Mode_Choose ( STA );
  
	ESP8266_Cmd ( "AT+CWLAP", "OK", 0, 5000 );
		
  do
	{
		//PC_Usart ( "\r\n请输入要连接的WiFi名称和密钥，输入格式为：名称字符+英文逗号+密钥字符+空格，点击发送\r\n" );
         // write english 		PC_Usart ( "\r\n请输入要连接的WiFi名称和密钥，输入格式为：名称字符+英文逗号+密钥字符+空格，点击发送\r\n" );
		PC_Usart ( "\r\nPlease input the WiFi name and password to connect, format: SSID+comma+password+space, then click send\r\n" );
		scanf ( "%s", cStrInput );

		// write english PC_Usart ( "\r\n稍等片刻 ……\r\n" );
		PC_Usart ( "\r\nPlease wait a moment...\r\n" );

		pBuf = cStrInput;
		uc = 0;
		while ( ( pStr = strtok ( pBuf, "," ) ) != NULL )
		{
			pStrDelimiter [ uc ++ ] = pStr;
			pBuf = NULL;
		} 
		
  } while ( ! ESP8266_JoinAP ( pStrDelimiter [0], pStrDelimiter [1] ) );
	
	ESP8266_Enable_MultipleId ( ENABLE );
	
	do 
	{
	// write english	PC_Usart ( "\r\n请在电脑上将网络调试助手以TCP Server连接网络，并输入电脑的IP和端口号，输入格式为：电脑IP+英文逗号+端口号+空格，点击发送\r\n" );
		PC_Usart ( "\r\nPlease input the reachable server IP and port, format: IP+comma+port+space, then click send\r\n" );

		scanf ( "%s", cStrInput );

	// write english		PC_Usart ( "\r\n稍等片刻 ……\r\n" );
		

		pBuf = cStrInput;
		uc = 0;
		while ( ( pStr = strtok ( pBuf, "," ) ) != NULL )
		{
			pStrDelimiter [ uc ++ ] = pStr;
			pBuf = NULL;
		} 
		
  } while ( ! ( ESP8266_Link_Server ( enumTCP, pStrDelimiter [0], pStrDelimiter [1], Multiple_ID_0 ) &&
	              ESP8266_Link_Server ( enumTCP, pStrDelimiter [0], pStrDelimiter [1], Multiple_ID_1 ) &&
	              ESP8266_Link_Server ( enumTCP, pStrDelimiter [0], pStrDelimiter [1], Multiple_ID_2 ) &&
	              ESP8266_Link_Server ( enumTCP, pStrDelimiter [0], pStrDelimiter [1], Multiple_ID_3 ) &&
	              ESP8266_Link_Server ( enumTCP, pStrDelimiter [0], pStrDelimiter [1], Multiple_ID_4 ) ) );

  for ( uc = 0; uc < 5; uc ++ )
	{
		// write english	 PC_Usart ( "\r\n请输入端口ID%d要发送的字符串，输入格式为：字符串（不含空格）+空格，点击发送\r\n", uc );
		PC_Usart ( "\r\nPlease input the string to send for port ID%d, format: string (no spaces) + space, then click send\r\n", uc );

		scanf ( "%s", cStrInput );

		ul = strlen ( cStrInput );
		
		ESP8266_SendString ( DISABLE, cStrInput, ul, ( ENUM_ID_NO_TypeDef ) uc );
		
	}
	
	
	// write english PC_Usart ( "\r\n请在网络调试助手发送字符串\r\n" );
	PC_Usart ( "\r\nPlease send strings in the network debugging assistant\r\n" );
	while (1)
	{
	  pStr = ESP8266_ReceiveString ( DISABLE );
		PC_Usart ( "%s", pStr );
	}

}





/*
 * 函数名：ESP8266_STA_TCP_Client_Single
 * 描述  ：ESP8266模块进行STA TCP Client测试（单连接模式）
 * 输入  ：无
 * 返回  : 无
 * 调用  ：被外部调用
 */

// 假设需要的头文件已经包含，这里示例包含常用的一些


void ESP8266_STA_TCP_Client_Single ( void )
{
    const char* ssid = WIFI_SSID;
    const char* password = WIFI_PASSWORD;
    const char* server_ip = TCP_DEMO_SERVER_HOST;
    const char* server_port = TCP_DEMO_SERVER_PORT;
    u8 wifi_retry = 0;
    bool wifi_connected = false;
    u8 server_retry = 0;
    bool server_connected = false;
    const char* test_data;
    u32 data_len;

    PC_Usart("\r\n[LEGACY_TCP] raw TCP demo start\r\n");
    PC_Usart("[LEGACY_TCP] For the real lab path use ESP8266_STA_TCP_Client_MQTT()\r\n");
    ESP8266_Choose(ENABLE);
    ESP8266_AT_Test();
    
    PC_Usart("\r\nConnecting to WiFi: %s...\r\n", ssid);
    if (!ESP8266_Net_Mode_Choose(STA)) {
        PC_Usart("[ESP] set STA mode failed\r\n");
        return;
    }
    
    while (wifi_retry < 3 && !wifi_connected) {
        wifi_connected = ESP8266_JoinAP(ssid, password);
        if (!wifi_connected) {
            PC_Usart("\r\nWiFi connection failed, retrying %d/3...\r\n", wifi_retry+1);
            wifi_retry++;
            delay_ms(1000);
        }
    }
    
    PC_Usart("\r\nConnecting to server: %s:%s...\r\n", server_ip, server_port);
    while (server_retry < 3 && !server_connected) {
        server_connected = ESP8266_Link_Server((uint8_t)enumTCP, server_ip, server_port, 9);
        if (!server_connected) {
            PC_Usart("\r\nServer connection failed, retrying %d/3...\r\n", server_retry+1);
            server_retry++;
            delay_ms(1000);
        }
    }
    
    test_data = "Hello from ESP8266!";
    data_len = strlen(test_data);
    PC_Usart("\r\nSending test data: %s\r\n", test_data);
    if (ESP8266_SendString(DISABLE, (char*)test_data, data_len, 0)) {
        PC_Usart("\r\nData sent successfully!\r\n");
    } else {
        PC_Usart("\r\nFailed to send data!\r\n");
    }
    
    while (1) { /* ... */ }
}





/*
 * 函数名：ESP8266_STA_TCP_Client_MQTT
 * 描述  ：ESP8266模块进行STA TCP Client测试（单连接模式）
 * 输入  ：无
 * 返回  : 无
 * 调用  ：被外部调用
 */

// 假设需要的头文件已经包含，这里示例包含常用的一些


bool ESP8266_STA_TCP_Client_MQTT ( void )
{
    const char* ssid = WIFI_SSID;
    const char* password = WIFI_PASSWORD;
    const char* mqtt_host = MQTT_HOST;
    const char* mqtt_port = MQTT_PORT_STR;
    const char* sub_topic = MQTT_TOPIC_CMD;
    const char* pub_topic = MQTT_TOPIC_ACK;
    char qos[4];

    u8 retry = 0;
    bool ok = false;
    const u32 baud_candidates[] = {9600U, 115200U, 74880U, 57600U};
    u8 baud_index;

    PC_Usart("\r\n=== ESP8266 MQTT Init ===\r\n");
    esp_log_mqtt_profile();
    sprintf(qos, "%d", MQTT_SUB_QOS);
    s_mqtt_ready = false;
    ESP8266_Choose(ENABLE);

    ok = false;
    for (baud_index = 0; baud_index < (sizeof(baud_candidates) / sizeof(baud_candidates[0])); ++baud_index) {
        ok = esp_try_at_baud(baud_candidates[baud_index], 400);
        if (ok) {
            PC_Usart("[ESP] AT check OK at %lu baud\r\n", (unsigned long)baud_candidates[baud_index]);
            break;
        }
    }

    if (!ok) {
        PC_Usart("[ESP] no response at tried bauds: 9600, 115200, 74880, 57600.\r\n");
        PC_Usart("[ESP] If USB-UART direct test works, set AT+UART_CUR=115200,8,1,0,0 and reboot the module.\r\n");
        esp_log_at_fail_guide();
        return false;
    }

    if (!ESP8266_Net_Mode_Choose(STA)) {
        PC_Usart("[ESP] set STA mode failed\r\n");
        return false;
    }

    if (!ESP8266_Cmd("ATE0", "OK", NULL, 600)) {
        PC_Usart("[ESP] ATE0 failed (continue)\r\n");
    }
    if (!ESP8266_Cmd("AT+CWAUTOCONN=1", "OK", NULL, 600)) {
        PC_Usart("[ESP] CWAUTOCONN failed (continue)\r\n");
    }
    if (!ESP8266_Cmd("AT+SYSLOG=1", "OK", NULL, 600)) {
        PC_Usart("[ESP] SYSLOG enable failed (continue)\r\n");
    }
    retry = 0;
    ok = false;
    while (retry < 3 && !ok) {
        ok = ESP8266_JoinAP(ssid, password);
        if (!ok) {
            retry++;
            PC_Usart("[ESP] WiFi join failed, retry %d/3\r\n", retry);
            delay_ms(1200);
        }
    }
    if (!ok) {
        PC_Usart("[ESP] WiFi join failed after retries.\r\n");
        return false;
    }
    PC_Usart("[ESP] WiFi connected\r\n");
    if (!ESP8266_Cmd("AT+CIFSR", "OK", "STAIP", 1500)) {
        PC_Usart("[ESP] CIFSR query failed (continue)\r\n");
    }

    retry = 0;
    ok = false;
    while (retry < 3 && !ok) {
        ok = ESP8266_Set_MQTT_User();
        if (!ok) {
            retry++;
            PC_Usart("[MQTT] user cfg failed, retry %d/3\r\n", retry);
            delay_ms(800);
        }
    }
    if (!ok) {
        PC_Usart("[MQTT] user cfg failed after retries.\r\n");
        return false;
    }
    PC_Usart("[MQTT] user cfg OK\r\n");

    retry = 0;
    ok = false;
    while (retry < 3 && !ok) {
        ok = ESP8266_Set_MQTT_ConnCfg();
        if (!ok) {
            retry++;
            PC_Usart("[MQTT] conn cfg failed, retry %d/3\r\n", retry);
            delay_ms(800);
        }
    }
    if (!ok) {
        PC_Usart("[MQTT] conn cfg failed after retries.\r\n");
        return false;
    }
    PC_Usart("[MQTT] conn cfg OK\r\n");

    retry = 0;
    ok = false;
    while (retry < 3 && !ok) {
        ok = ESP8266_Link_MQTT(mqtt_host, mqtt_port, Single_ID_0);
        if (!ok) {
            retry++;
            PC_Usart("[MQTT] conn failed, retry %d/3\r\n", retry);
            delay_ms(1200);
        }
    }
    if (!ok) {
        PC_Usart("[MQTT] conn failed after retries.\r\n");
        return false;
    }
    PC_Usart("[MQTT] connected to %s:%s\r\n", mqtt_host, mqtt_port);

    retry = 0;
    ok = false;
    while (retry < 3 && !ok) {
        ok = ESP8266_Set_MQTT_Sub(sub_topic, qos);
        if (!ok) {
            retry++;
            PC_Usart("[MQTT] sub failed, retry %d/3\r\n", retry);
            delay_ms(800);
        }
    }
    if (!ok) {
        PC_Usart("[MQTT] sub failed after retries.\r\n");
        return false;
    }
    PC_Usart("[MQTT] subscribed: %s\r\n", sub_topic);

    if (ESP8266_Set_MQTT_Public(pub_topic,
                                "{\"event\":\"online\",\"detail\":\"mqtt_ready\",\"device\":\"" MQTT_DEVICE_NAME "\",\"client_id\":\"" MQTT_CLIENT_ID "\"}")) {
        PC_Usart("[MQTT] online status published: %s\r\n", pub_topic);
    } else {
        PC_Usart("[MQTT] online status publish failed: %s\r\n", pub_topic);
    }
    PC_Usart("[MQTT] ready. control=%s telemetry=%s status=%s\r\n",
             sub_topic, MQTT_TOPIC_DATA, pub_topic);
    s_mqtt_ready = true;
    return true;
}

bool ESP8266_MQTT_ParseSubFrame(const char *src, char *topic, u16 topic_size, char *payload, u16 payload_size)
{
    const char *topic_begin;
    const char *topic_end;
    const char *comma_after_topic;
    const char *comma_after_len;
    const char *payload_begin;
    const char *payload_end;
    u16 topic_copy_len;
    u16 payload_copy_len;

    if ((src == NULL) || (topic == NULL) || (payload == NULL) || (topic_size < 2U) || (payload_size < 2U)) {
        return false;
    }

    topic[0] = '\0';
    payload[0] = '\0';

    topic_begin = strchr(src, '"');
    if (topic_begin == NULL) {
        return false;
    }
    topic_begin++;

    topic_end = strchr(topic_begin, '"');
    if (topic_end == NULL) {
        return false;
    }

    comma_after_topic = strchr(topic_end, ',');
    if (comma_after_topic == NULL) {
        return false;
    }

    comma_after_len = strchr(comma_after_topic + 1, ',');
    if (comma_after_len == NULL) {
        return false;
    }

    topic_copy_len = (u16)(topic_end - topic_begin);
    if (topic_copy_len >= topic_size) {
        topic_copy_len = topic_size - 1U;
    }
    memcpy(topic, topic_begin, topic_copy_len);
    topic[topic_copy_len] = '\0';

    payload_begin = comma_after_len + 1;
    while ((*payload_begin == ' ') || (*payload_begin == '\t')) {
        payload_begin++;
    }

    payload_end = payload_begin;
    while ((*payload_end != '\0') && (*payload_end != '\r') && (*payload_end != '\n')) {
        payload_end++;
    }

    while ((payload_end > payload_begin) &&
           ((*(payload_end - 1) == ' ') || (*(payload_end - 1) == '\t'))) {
        payload_end--;
    }

    if ((*payload_begin == '"') && (payload_end > payload_begin) && (*(payload_end - 1) == '"')) {
        payload_begin++;
        payload_end--;
    }

    payload_copy_len = (u16)(payload_end - payload_begin);
    if (payload_copy_len >= payload_size) {
        payload_copy_len = payload_size - 1U;
    }

    memcpy(payload, payload_begin, payload_copy_len);
    payload[payload_copy_len] = '\0';
    return true;
}

bool ESP8266_MQTT_ExtractPayload(const char *src, char *payload, u16 payload_size)
{
    char topic[96];

    return ESP8266_MQTT_ParseSubFrame(src, topic, sizeof(topic), payload, payload_size);
}

static void MQTT_HandleCommand(const char *payload)
{
    if (payload == NULL) {
        return;
    }

    if ((strcmp(payload, "LED1_ON") == 0) || (strcmp(payload, "LED_ON") == 0) || (strcmp(payload, "ON") == 0) || (strcmp(payload, "1") == 0)) {
        LED1 = 0;
        ESP8266_Set_MQTT_Public(MQTT_TOPIC_ACK, "LED_ON_OK");
    } else if ((strcmp(payload, "LED1_OFF") == 0) || (strcmp(payload, "LED_OFF") == 0) || (strcmp(payload, "OFF") == 0) || (strcmp(payload, "0") == 0)) {
        LED1 = 1;
        ESP8266_Set_MQTT_Public(MQTT_TOPIC_ACK, "LED_OFF_OK");
    } else if (strcmp(payload, "PING") == 0) {
        ESP8266_Set_MQTT_Public(MQTT_TOPIC_ACK, "PONG");
    } else {
        ESP8266_Set_MQTT_Public(MQTT_TOPIC_ACK, "UNKNOWN_CMD");
    }
}

void ESP8266_MQTT_Task(void)
{
    char *rx;
    char topic[96];
    char payload[128];

    rx = ESP8266_ReceiveString(DISABLE);
    if (rx == NULL) {
        return;
    }

    PC_Usart("%s", rx);

    if (strstr(rx, "+MQTTSUBRECV:") != NULL) {
        if (ESP8266_MQTT_ParseSubFrame(rx, topic, sizeof(topic), payload, sizeof(payload))) {
            PC_Usart("[MQTT] topic=%s payload=%s\r\n", topic, payload);
            MQTT_HandleCommand(payload);
        } else {
            PC_Usart("[MQTT] parse failed\r\n");
        }
    } else if (strstr(rx, "+MQTTDISCONNECTED:") != NULL) {
        PC_Usart("MQTT disconnected, trying reconnect...\r\n");
        ESP8266_STA_TCP_Client_MQTT();
    }
}


/*
 * 函数名：ESP8266_AP_TCP_Server
 * 描述  ：PZ-ESP8266模块进行AP TCP Server测试
 * 输入  ：无
 * 返回  : 无
 * 调用  ：被外部调用
 */
void ESP8266_AP_TCP_Server ( void )
{
	char cStrInput [100] = { 0 }, * pStrDelimiter [3], * pBuf, * pStr;
	u8 uc = 0;
  u32 ul = 0;

  ESP8266_Choose ( ENABLE );

	ESP8266_AT_Test ();
	
	ESP8266_Net_Mode_Choose ( AP );


	PC_Usart ( "\r\n请输入要创建的WiFi的名称、加密方式和密钥，加密方式的编号为：\
              \r\n0 = OPEN\
              \r\n1 = WEP\
              \r\n2 = WPA_PSK\
	            \r\n3 = WPA2_PSK\
              \r\n4 = WPA_WPA2_PSK\
							\r\n输入格式为：名称字符+英文逗号+加密方式编号+英文逗号+密钥字符+空格，点击发送\r\n" );

	scanf ( "%s", cStrInput );

	PC_Usart ( "\r\n稍等片刻 ……\r\n" );

	pBuf = cStrInput;
	uc = 0;
	while ( ( pStr = strtok ( pBuf, "," ) ) != NULL )
	{
		pStrDelimiter [ uc ++ ] = pStr;
		pBuf = NULL;
	} 
	
	ESP8266_BuildAP ( pStrDelimiter [0], pStrDelimiter [2], pStrDelimiter [1] );
	ESP8266_Cmd ( "AT+RST", "OK", "ready", 2500 ); //*
		

	ESP8266_Enable_MultipleId ( ENABLE );
		
	
	PC_Usart ( "\r\n请输入服务器要开启的端口号和超时时间（0~28800，单位：秒），输入格式为：端口号字符+英文逗号+超时时间字符+空格，点击发送\r\n" );

	scanf ( "%s", cStrInput );

	PC_Usart ( "\r\n稍等片刻 ……\r\n" );

	pBuf = cStrInput;
	uc = 0;
	while ( ( pStr = strtok ( pBuf, "," ) ) != NULL )
	{
		pStrDelimiter [ uc ++ ] = pStr;
		pBuf = NULL;
	} 

	ESP8266_StartOrShutServer ( ENABLE, pStrDelimiter [0], pStrDelimiter [1] );
	
	
	do
	{
		PC_Usart ( "\r\n正查询本模块IP……\r\n" );
	  ESP8266_Cmd ( "AT+CIFSR", "OK", "Link", 500 );
		
		PC_Usart ( "\r\n请用手机连接刚才创建的WiFi，这里只连接一个手机，作为ID0，然后用手机网络调试助手以TCP Client连接刚才开启的服务器（AP IP）……\r\n" );
		delay_ms ( 20000 ) ;
	}	while ( ! ESP8266_Cmd ( "AT+CIPSTATUS", "+CIPSTATUS:0", 0, 500 ) );
	

	PC_Usart ( "\r\n请输入要向端口手机（ID0）发送的字符串，输入格式为：字符串（不含空格）+空格，点击发送\r\n" );

	scanf ( "%s", cStrInput );

	ul = strlen ( cStrInput );
	
	ESP8266_SendString ( DISABLE, cStrInput, ul, Multiple_ID_0 );

	
	PC_Usart ( "\r\n请在手机网络调试助手发送字符串\r\n" );
	while (1)
	{
	  pStr = ESP8266_ReceiveString ( DISABLE );
		PC_Usart ( "%s", pStr );
	}

}


/*
 * 函数名：ESP8266_StaTcpClient_ApTcpServer
 * 描述  ：PZ-ESP8266模块进行STA(TCP Client)+AP(TCP Server)测试
 * 输入  ：无
 * 返回  : 无
 * 调用  ：被外部调用
 */
void ESP8266_StaTcpClient_ApTcpServer ( void )
{
	char cStrInput [100] = { 0 }, * pStrDelimiter [3], * pBuf, * pStr;
	u8 uc = 0;
  u32 ul = 0;

  ESP8266_Choose ( ENABLE );

	ESP8266_AT_Test ();
	
	ESP8266_Net_Mode_Choose ( STA_AP );


	PC_Usart ( "\r\n请输入要创建的WiFi的名称、加密方式和密钥，加密方式的编号为：\
						\r\n0 = OPEN\
						\r\n1  =WEP\
						\r\n2 = WPA_PSK\
						\r\n3 = WPA2_PSK\
						\r\n4 = WPA_WPA2_PSK\
						\r\n输入格式为：名称字符+英文逗号+加密方式编号+英文逗号+密钥字符+空格，点击发送\r\n" );

	scanf ( "%s", cStrInput );

	PC_Usart ( "\r\n稍等片刻 ……\r\n" );

	pBuf = cStrInput;
	uc = 0;
	while ( ( pStr = strtok ( pBuf, "," ) ) != NULL )
	{
		pStrDelimiter [ uc ++ ] = pStr;
		pBuf = NULL;
	} 
	
	ESP8266_BuildAP ( pStrDelimiter [0], pStrDelimiter [2], pStrDelimiter [1] );
	ESP8266_Cmd ( "AT+RST", "OK", "ready", 2500 ); //*
	

	ESP8266_Cmd ( "AT+CWLAP", "OK", 0, 5000 );
		
  do
	{
		PC_Usart ( "\r\n请输入要连接的WiFi名称和密钥，输入格式为：名称字符+英文逗号+密钥字符+空格，点击发送\r\n" );

		scanf ( "%s", cStrInput );

		PC_Usart ( "\r\n稍等片刻 ……\r\n" );

		pBuf = cStrInput;
		uc = 0;
		while ( ( pStr = strtok ( pBuf, "," ) ) != NULL )
		{
			pStrDelimiter [ uc ++ ] = pStr;
			pBuf = NULL;
		} 
		
  } while ( ! ESP8266_JoinAP ( pStrDelimiter [0], pStrDelimiter [1] ) );

	
	ESP8266_Enable_MultipleId ( ENABLE );
		
	
	PC_Usart ( "\r\n请输入服务器要开启的端口号和超时时间（0~28800，单位：秒），输入格式为：端口号字符+英文逗号+超时时间字符+空格，点击发送\r\n" );

	scanf ( "%s", cStrInput );

	PC_Usart ( "\r\n稍等片刻 ……\r\n" );

	pBuf = cStrInput;
	uc = 0;
	while ( ( pStr = strtok ( pBuf, "," ) ) != NULL )
	{
		pStrDelimiter [ uc ++ ] = pStr;
		pBuf = NULL;
	} 

	ESP8266_StartOrShutServer ( ENABLE, pStrDelimiter [0], pStrDelimiter [1] );
	
	
	do 
	{
		PC_Usart ( "\r\n请在电脑上将网络调试助手以TCP Server连接网络，并输入电脑的IP和端口号，输入格式为：电脑IP+英文逗号+端口号+空格，点击发送\r\n" );

		scanf ( "%s", cStrInput );

		PC_Usart ( "\r\n稍等片刻 ……\r\n" );

		pBuf = cStrInput;
		uc = 0;
		while ( ( pStr = strtok ( pBuf, "," ) ) != NULL )
		{
			pStrDelimiter [ uc ++ ] = pStr;
			pBuf = NULL;
		} 
		
  } while ( ! ( ESP8266_Link_Server ( enumTCP, pStrDelimiter [0], pStrDelimiter [1], Multiple_ID_0 ) &&
	              ESP8266_Link_Server ( enumTCP, pStrDelimiter [0], pStrDelimiter [1], Multiple_ID_1 ) &&
	              ESP8266_Link_Server ( enumTCP, pStrDelimiter [0], pStrDelimiter [1], Multiple_ID_2 ) ) );
		
	
	do
	{
		PC_Usart ( "\r\n正查询本模块IP，前一个为AP IP，后一个为STA IP……\r\n" );
	  ESP8266_Cmd ( "AT+CIFSR", "OK", "Link", 500 );
		
		PC_Usart ( "\r\n请用手机连接刚才创建的WiFi，这里只连接一个手机，作为ID3，然后用手机网络调试助手以TCP Client连接刚才开启的服务器（AP IP）……\r\n" );
		delay_ms ( 20000 ) ;
	}	while ( ! ESP8266_Cmd ( "AT+CIPSTATUS", "+CIPSTATUS:3", 0, 500 ) );
	

	for ( uc = 0; uc < 3; uc ++ )
	{
		PC_Usart ( "\r\n请输入端口ID%d要发送的字符串，输入格式为：字符串（不含空格）+空格，点击发送\r\n", uc );

		scanf ( "%s", cStrInput );

		ul = strlen ( cStrInput );
		
		ESP8266_SendString ( DISABLE, cStrInput, ul, ( ENUM_ID_NO_TypeDef ) uc );
		
	}
	
	
	PC_Usart ( "\r\n请输入要向端口手机（ID3）发送的字符串，输入格式为：字符串（不含空格）+空格，点击发送\r\n" );

	scanf ( "%s", cStrInput );

	ul = strlen ( cStrInput );
	
	ESP8266_SendString ( DISABLE, cStrInput, ul, Multiple_ID_3 );

	
	PC_Usart ( "\r\n请在电脑网络调试助手或手机网络调试助手发送字符串……\r\n" );
	while (1)
	{
	  pStr = ESP8266_ReceiveString ( DISABLE );
		PC_Usart ( "%s", pStr );
	}
	
}










