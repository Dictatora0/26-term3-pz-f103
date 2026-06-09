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
	ESP8266_RST_HIGH_LEVEL();
	
	delay_ms ( 1000 ); 
	
	while ( ! ESP8266_Cmd ( "AT", "OK", NULL, 200 ) ) ESP8266_Rst ();  	

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
bool ESP8266_Cmd ( char * cmd, char * reply1, char * reply2, u32 waittime )
{    
	strEsp8266_Fram_Record .InfBit .FramLength = 0;               //从新开始接收新的数据包

	ESP8266_Usart ( "%s\r\n", cmd );

	if ( ( reply1 == 0 ) && ( reply2 == 0 ) )                      //不需要接收数据
		return true;
	
	delay_ms ( waittime );                 //延时
	
	strEsp8266_Fram_Record .Data_RX_BUF [ strEsp8266_Fram_Record .InfBit .FramLength ]  = '\0';

	PC_Usart ( "%s", strEsp8266_Fram_Record .Data_RX_BUF );
  
	if ( ( reply1 != 0 ) && ( reply2 != 0 ) )
		return ( ( bool ) strstr ( strEsp8266_Fram_Record .Data_RX_BUF, reply1 ) || 
						 ( bool ) strstr ( strEsp8266_Fram_Record .Data_RX_BUF, reply2 ) ); 
 	
	else if ( reply1 != 0 )
		return ( ( bool ) strstr ( strEsp8266_Fram_Record .Data_RX_BUF, reply1 ) );
	
	else
		return ( ( bool ) strstr ( strEsp8266_Fram_Record .Data_RX_BUF, reply2 ) );
	
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
bool ESP8266_JoinAP ( char * pSSID, char * pPassWord )
{
	char cCmd [120];

	sprintf ( cCmd, "AT+CWJAP=\"%s\",\"%s\"", pSSID, pPassWord );
	
	return ESP8266_Cmd ( cCmd, "OK", NULL, 7000 );
	
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
bool ESP8266_BuildAP ( char * pSSID, char * pPassWord, char * enunPsdMode )
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
bool ESP8266_Link_Server ( ENUM_NetPro_TypeDef enumE, char * ip, char * ComNum, ENUM_ID_NO_TypeDef id)
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
bool ESP8266_Link_MQTT (  char * ip, char * ComNum, ENUM_ID_NO_TypeDef id)
{
	char cStr [100] = { 0 }, cCmd [120];

	sprintf ( cStr, "\"%s\",%s",  ip, ComNum );
    sprintf ( cCmd, "AT+MQTTCONN=0,%s,0", cStr );
	return ESP8266_Cmd ( cCmd, "+MQTTCONNECTED", "OK", 12000 );
	
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
    /* MQTT over TCP，不使用用户名和密码 */
	sprintf ( cCmd, "AT+MQTTUSERCFG=0,1,\"%s\",\"\",\"\",0,0,\"\"", MQTT_CLIENT_ID );
	return ESP8266_Cmd ( cCmd, "OK", "ALREAY CONNECT", 500 );
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
	char cCmd [520];
    if ((topicId == 0) || (val == 0)) {
        return false;
    }
	sprintf ( cCmd, "AT+MQTTPUB=0,\"%s\",\"%s\",0,0", topicId, val );
    if (strlen(cCmd) >= (sizeof(cCmd) - 2U)) {
        return false;
    }
	return ESP8266_Cmd ( cCmd, "OK", "ALREAY CONNECT", 500 );
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
		PC_Usart ( "\r\nPlease connect the network debugging assistant to the TCP Server on your computer, and input the computer's IP and port number, format: IP+comma+port+space, then click send\r\n" );

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
    // 固定配置
    const char* ssid = "x";                
    const char* password = "12345678";        
    const char* server_ip = "192.168.254.195"; 
    const char* server_port = "8080";        
    
    // **函数体开头：声明所有变量**
    u8 wifi_retry = 0;
    bool wifi_connected = false;
    u8 server_retry = 0;
    bool server_connected = false;
    const char* test_data;
    u32 data_len;

    // 启用模块
    PC_Usart("\r\nInitializing ESP8266...\r\n");
    ESP8266_Choose(ENABLE);
    ESP8266_AT_Test();
    
    // 设置STA模式
    PC_Usart("\r\nConnecting to WiFi: %s...\r\n", ssid);
    ESP8266_Net_Mode_Choose(STA);
    
    // WiFi连接循环（变量已在开头声明，无需重复）
    while (wifi_retry < 3 && !wifi_connected) {
        wifi_connected = ESP8266_JoinAP((char*)ssid, (char*)password);
        if (!wifi_connected) {
            PC_Usart("\r\nWiFi connection failed, retrying %d/3...\r\n", wifi_retry+1);
            wifi_retry++;
            delay_ms(1000);
        }
    }
    
    // 单连接模式
    //ESP8266_Enable_MultipleId(DISABLE); // 关闭多连接
    
    // 连接服务器
    PC_Usart("\r\nConnecting to server: %s:%s...\r\n", server_ip, server_port);
    while (server_retry < 3 && !server_connected) {
        // 强制转换枚举类型为uint8_t（若函数参数为uint8_t）
        server_connected = ESP8266_Link_Server((uint8_t)enumTCP, (char*)server_ip, (char*)server_port, 9); // 假设Single_ID=0
        if (!server_connected) {
            PC_Usart("\r\nServer connection failed, retrying %d/3...\r\n", server_retry+1);
            server_retry++;
            delay_ms(1000);
        }
    }
    
    // 发送数据（变量已在开头声明）
    test_data = "Hello from ESP8266!";
    data_len = strlen(test_data);
    PC_Usart("\r\nSending test data: %s\r\n", test_data);
    if (ESP8266_SendString(DISABLE, (char*)test_data, data_len, 0)) { // 假设Single_ID=0
        PC_Usart("\r\nData sent successfully!\r\n");
    } else {
        PC_Usart("\r\nFailed to send data!\r\n");
    }
    
    // 接收循环
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


void ESP8266_STA_TCP_Client_MQTT ( void )
{
    const char* ssid = WIFI_SSID;
    const char* password = WIFI_PASSWORD;
    const char* mqtt_host = MQTT_HOST;
    const char* mqtt_port = MQTT_PORT_STR;
    const char* sub_topic = MQTT_TOPIC_CMD;
    const char* pub_topic = MQTT_TOPIC_ACK;
    char qos[] = "0";

    u8 retry = 0;
    bool ok = false;

    PC_Usart("\r\n=== ESP8266 MQTT Init ===\r\n");
    ESP8266_Choose(ENABLE);
    ESP8266_AT_Test();
    PC_Usart("[ESP] AT check OK\r\n");

    ESP8266_Net_Mode_Choose(STA);
    ESP8266_Cmd("ATE0", "OK", NULL, 500);

    retry = 0;
    ok = false;
    while (retry < 3 && !ok) {
        ok = ESP8266_JoinAP((char*)ssid, (char*)password);
        if (!ok) {
            retry++;
            PC_Usart("WiFi join failed, retry %d/3\r\n", retry);
            delay_ms(1200);
        }
    }
    if (!ok) {
        PC_Usart("WiFi join failed after retries.\r\n");
        return;
    }
    PC_Usart("[ESP] WiFi connected\r\n");

    retry = 0;
    ok = false;
    while (retry < 3 && !ok) {
        ok = ESP8266_Set_MQTT_User();
        if (!ok) {
            retry++;
            PC_Usart("MQTT user cfg failed, retry %d/3\r\n", retry);
            delay_ms(800);
        }
    }
    if (!ok) {
        PC_Usart("MQTT user cfg failed after retries.\r\n");
        return;
    }
    PC_Usart("[MQTT] user cfg OK\r\n");

    retry = 0;
    ok = false;
    while (retry < 3 && !ok) {
        ok = ESP8266_Link_MQTT((char*)mqtt_host, (char*)mqtt_port, Single_ID_0);
        if (!ok) {
            retry++;
            PC_Usart("MQTT conn failed, retry %d/3\r\n", retry);
            delay_ms(1200);
        }
    }
    if (!ok) {
        PC_Usart("MQTT conn failed after retries.\r\n");
        return;
    }
    PC_Usart("[MQTT] connected\r\n");

    retry = 0;
    ok = false;
    while (retry < 3 && !ok) {
        ok = ESP8266_Set_MQTT_Sub(sub_topic, qos);
        if (!ok) {
            retry++;
            PC_Usart("MQTT sub failed, retry %d/3\r\n", retry);
            delay_ms(800);
        }
    }
    if (!ok) {
        PC_Usart("MQTT sub failed after retries.\r\n");
        return;
    }
    PC_Usart("[MQTT] subscribed: %s\r\n", sub_topic);

    ESP8266_Set_MQTT_Public(pub_topic, "online");
    PC_Usart("MQTT ready. Sub topic: %s, Pub topic: %s\r\n", sub_topic, pub_topic);
}

bool ESP8266_MQTT_ExtractPayload(const char *src, char *payload, u16 payload_size)
{
    const char *first_comma;
    const char *second_comma;
    const char *third_comma;
    const char *start;
    const char *end;
    u16 copy_len;

    if ((src == NULL) || (payload == NULL) || (payload_size < 2U)) {
        return false;
    }

    first_comma = strchr(src, ',');
    if (first_comma == NULL) {
        return false;
    }

    second_comma = strchr(first_comma + 1, ',');
    if (second_comma == NULL) {
        return false;
    }

    third_comma = strchr(second_comma + 1, ',');
    if (third_comma == NULL) {
        return false;
    }

    start = third_comma + 1;
    while ((*start == ' ') || (*start == '"')) {
        start++;
    }

    end = start;
    while ((*end != '\0') && (*end != '\r') && (*end != '\n')) {
        end++;
    }
    while ((end > start) && ((*(end - 1) == ' ') || (*(end - 1) == '"'))) {
        end--;
    }

    copy_len = (u16)(end - start);
    if (copy_len >= payload_size) {
        copy_len = payload_size - 1U;
    }

    memcpy(payload, start, copy_len);
    payload[copy_len] = '\0';
    return true;
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
    char payload[128];

    rx = ESP8266_ReceiveString(DISABLE);
    if (rx == NULL) {
        return;
    }

    PC_Usart("%s", rx);

    if (strstr(rx, "+MQTTSUBRECV:") != NULL) {
        if (ESP8266_MQTT_ExtractPayload(rx, payload, sizeof(payload))) {
            PC_Usart("MQTT payload: %s\r\n", payload);
            MQTT_HandleCommand(payload);
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









