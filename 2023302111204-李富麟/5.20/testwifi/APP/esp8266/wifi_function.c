#include "wifi_function.h"
#include "wifi_config.h"
#include "SysTick.h"
#include <string.h> 
#include <stdio.h>  
#include <stdbool.h>


#include <stdint.h>
#include <stdlib.h>



// ?????ID????0?????????
#define Single_ID 0 

/* Default configuration for PZ103 + ESP8266 MQTT demo */
#define WIFI_STA_SSID            "x_pz103_0520_a6r4"
#define WIFI_STA_PASSWORD        "PZ103_0520_esp"
#define MQTT_BROKER_HOST         "10.128.135.31"
#define MQTT_BROKER_PORT         "1883"
#define MQTT_CLIENT_ID           "pz103_client_a6r4"
#define MQTT_USERNAME            "user_a6r4"
#define MQTT_PASSWORD            "PZ103_mqtt_a6r4"


static u32 g_esp8266_baud = 115200;
static u32 g_cmd_seq = 0;

static void ESP8266_ReInitUartBaud(u32 baud)
{
	USART_InitTypeDef USART_InitStructure;

	USART_Cmd(WiFi_ESP8266_GetUart(), DISABLE);
	USART_InitStructure.USART_BaudRate = baud;
	USART_InitStructure.USART_WordLength = USART_WordLength_8b;
	USART_InitStructure.USART_StopBits = USART_StopBits_1;
	USART_InitStructure.USART_Parity = USART_Parity_No;
	USART_InitStructure.USART_HardwareFlowControl = USART_HardwareFlowControl_None;
	USART_InitStructure.USART_Mode = USART_Mode_Rx | USART_Mode_Tx;
	USART_Init(WiFi_ESP8266_GetUart(), &USART_InitStructure);
	USART_Cmd(WiFi_ESP8266_GetUart(), ENABLE);
	delay_ms(80);
}

static bool ESP8266_TryATAtBaud(u32 baud)
{
	u8 i;
	g_esp8266_baud = baud;
	PC_Usart("\r\n[ATDBG] switch ESP UART baud=%d\r\n", baud);
	ESP8266_ReInitUartBaud(baud);
	for (i = 0; i < 3; i++)
	{
		PC_Usart("[ATDBG] try baud=%d round=%d\r\n", baud, i + 1);
		if (ESP8266_Cmd("AT", "OK", NULL, 300))
		{
			PC_Usart("[ATDBG] AT OK at baud=%d round=%d\r\n", baud, i + 1);
			return true;
		}
		delay_ms(120);
	}
	return false;
}

static bool ESP8266_StringEqualsIgnoreCase(const char *a, const char *b)
{
	char ca;
	char cb;

	if ((a == NULL) || (b == NULL))
	{
		return false;
	}

	while ((*a != '\0') && (*b != '\0'))
	{
		ca = *a;
		cb = *b;
		if ((ca >= 'a') && (ca <= 'z'))
		{
			ca = (char)(ca - 'a' + 'A');
		}
		if ((cb >= 'a') && (cb <= 'z'))
		{
			cb = (char)(cb - 'a' + 'A');
		}
		if (ca != cb)
		{
			return false;
		}
		a++;
		b++;
	}

	return ((*a == '\0') && (*b == '\0'));
}



/*
 * ????ESP8266_Choose
 * ??  ???/??WF-ESP8266??
 * ??  ?enumChoose = ENABLE?????
 *         enumChoose = DISABLE?????
 * ??  : ?
 * ??  ??????
 */
void ESP8266_Choose ( FunctionalState enumChoose )
{
	if ( enumChoose == ENABLE )
		ESP8266_CH_HIGH_LEVEL();
	
	else
		ESP8266_CH_LOW_LEVEL();
	
}


/*
 * ????ESP8266_Rst
 * ??  ???WF-ESP8266??
 * ??  ??
 * ??  : ?
 * ??  ??ESP8266_AT_Test??
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
 * ????ESP8266_AT_Test
 * ??  ??WF-ESP8266????AT????
 * ??  ??
 * ??  : ?
 * ??  ??????
 */
void ESP8266_AT_Test ( void )
{
	ESP8266_RST_HIGH_LEVEL();
	
	delay_ms ( 1000 ); 
	
	while ( ! ESP8266_Cmd ( "AT", "OK", NULL, 200 ) ) ESP8266_Rst ();  	

}


/*
 * ????ESP8266_Cmd
 * ??  ??WF-ESP8266????AT??
 * ??  ?cmd???????
 *         reply1?reply2????????NULL??????????????
 *         waittime????????
 * ??  : 1???????
 *         0???????
 * ??  ??????
 */
bool ESP8266_Cmd ( char * cmd, char * reply1, char * reply2, u32 waittime )
{
	u16 len;
	u8 hit1 = 0, hit2 = 0;
	u32 rx_before = g_usart3_rx_total;
	u32 idle_before = g_usart3_idle_total;
	u32 of_before = g_usart3_overflow_total;

	strEsp8266_Fram_Record.InfBit.FramLength = 0;
	strEsp8266_Fram_Record.InfBit.FramFinishFlag = 0;

	g_cmd_seq++;
	PC_Usart("[ATDBG][%lu] TX@%lu: %s\r\n", g_cmd_seq, g_esp8266_baud, cmd);
	ESP8266_Usart("%s\r\n", cmd);

	if ((reply1 == 0) && (reply2 == 0))
	{
		PC_Usart("[ATDBG][%lu] no-reply mode\r\n", g_cmd_seq);
		return true;
	}

	delay_ms(waittime);

	len = strEsp8266_Fram_Record.InfBit.FramLength;
	if (len >= RX_BUF_MAX_LEN)
	{
		len = RX_BUF_MAX_LEN - 1;
	}
	strEsp8266_Fram_Record.Data_RX_BUF[len] = '\0';

	if (reply1 != 0)
	{
		hit1 = (u8)((bool)strstr(strEsp8266_Fram_Record.Data_RX_BUF, reply1));
	}
	if (reply2 != 0)
	{
		hit2 = (u8)((bool)strstr(strEsp8266_Fram_Record.Data_RX_BUF, reply2));
	}

	PC_Usart("[ATDBG][%lu] RXlen=%d finish=%d rxIRQ+%lu idle+%lu of+%lu last=0x%02X\r\n",
		g_cmd_seq,
		len,
		strEsp8266_Fram_Record.InfBit.FramFinishFlag,
		g_usart3_rx_total - rx_before,
		g_usart3_idle_total - idle_before,
		g_usart3_overflow_total - of_before,
		g_usart3_last_rx);
	if (len > 0)
	{
		PC_Usart("[ATDBG][%lu] RXbuf: %s\r\n", g_cmd_seq, strEsp8266_Fram_Record.Data_RX_BUF);
	}

	if ((reply1 != 0) && (reply2 != 0))
	{
		PC_Usart("[ATDBG][%lu] match r1=%d r2=%d\r\n", g_cmd_seq, hit1, hit2);
		return (hit1 || hit2);
	}
	else if (reply1 != 0)
	{
		PC_Usart("[ATDBG][%lu] match r1=%d\r\n", g_cmd_seq, hit1);
		return hit1;
	}
	else
	{
		PC_Usart("[ATDBG][%lu] match r2=%d\r\n", g_cmd_seq, hit2);
		return hit2;
	}
}
/*
 * ????ESP8266_Net_Mode_Choose
 * ??  ???WF-ESP8266???????
 * ??  ?enumMode?????
 * ??  : 1?????
 *         0?????
 * ??  ??????
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
 * ????ESP8266_JoinAP
 * ??  ?WF-ESP8266??????WiFi
 * ??  ?pSSID?WiFi?????
 *       ?pPassWord?WiFi?????
 * ??  : 1?????
 *         0?????
 * ??  ??????
 */
bool ESP8266_JoinAP ( char * pSSID, char * pPassWord )
{
	char cCmd [120];

	sprintf ( cCmd, "AT+CWJAP=\"%s\",\"%s\"", pSSID, pPassWord );
	
	return ESP8266_Cmd ( cCmd, "OK", NULL, 7000 );
	
}


/*
 * ????ESP8266_BuildAP
 * ??  ?WF-ESP8266????WiFi??
 * ??  ?pSSID?WiFi?????
 *       ?pPassWord?WiFi?????
 *       ?enunPsdMode?WiFi?????????
 * ??  : 1?????
 *         0?????
 * ??  ??????
 */
bool ESP8266_BuildAP ( char * pSSID, char * pPassWord, char * enunPsdMode )
{
	char cCmd [120];

	sprintf ( cCmd, "AT+CWSAP=\"%s\",\"%s\",1,%s", pSSID, pPassWord, enunPsdMode );
	
	return ESP8266_Cmd ( cCmd, "OK", 0, 1000 );
	
}


/*
 * ????ESP8266_Enable_MUSART3_printfultipleId
 * ??  ?WF-ESP8266???????
 * ??  ?enumEnUnvarnishTx????????
 * ??  : 1?????
 *         0?????
 * ??  ??????
 */
bool ESP8266_Enable_MultipleId ( FunctionalState enumEnUnvarnishTx )
{
	char cStr [20];
	
	sprintf ( cStr, "AT+CIPMUX=%d", ( enumEnUnvarnishTx ? 1 : 0 ) );
	
	return ESP8266_Cmd ( cStr, "OK", 0, 500 );
	
}


/*
 * ????ESP8266_Link_Server
 * ??  ?WF-ESP8266?????????
 * ??  ?enumE?????
 *       ?ip????IP???
 *       ?ComNum?????????
 *       ?id?????????ID
 * ??  : 1?????
 *         0?????
 * ??  ??????
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
 * ??  ?WF-ESP8266?????????
 * ??  ?enumE?????
 *       ?ip????IP???
 *       ?ComNum?????????
 *       ?id?????????ID
 * ??  : 1?????
 *         0?????
 * ??  ??????
 */
bool ESP8266_Link_MQTT (  char * ip, char * ComNum, ENUM_ID_NO_TypeDef id)
{
	char cStr [100] = { 0 }, cCmd [120];

	sprintf ( cStr, "\"%s\",%s",  ip, ComNum );
    sprintf ( cCmd, "AT+MQTTCONN=0,%s,0", cStr );
	return ESP8266_Cmd ( cCmd, "OK", "ALREAY CONNECT", 500 );
	
}




/*
 * ESP8266_Set_MQTT_User
 * ??  ?set MQTT user

 * ??  : 1?????
 *         0?????
 * ??  ??????
 */
bool ESP8266_Set_MQTT_User ()
{
	char cStr [100] = { 0 }, cCmd [120];
    // init  cCmd's value :AT+MQTTUSERCFG=0,1,"testUser4002","user4","1234",0,0,""
	sprintf ( cCmd, "AT+MQTTUSERCFG=0,1,\"%s\",\"%s\",\"%s\",0,0,\"\"", MQTT_CLIENT_ID, MQTT_USERNAME, MQTT_PASSWORD );
	return ESP8266_Cmd ( cCmd, "OK", "ALREAY CONNECT", 500 );
}


/*
 * ESP8266_Set_MQTT_public
 * ??  ?set MQTT public

 * ??  : 1?????
 *         0?????
 * ??  ??????
 */
bool ESP8266_Set_MQTT_Public (char * topicId ,char * val)
{
	char cStr [100] = { 0 }, cCmd [120];
     //construct string: AT+MQTTPUB=0,"topic911","66693",0,0
	sprintf ( cCmd, "AT+MQTTPUB=0,\"%s\",\"%s\",0,0", topicId, val );
	return ESP8266_Cmd ( cCmd, "OK", "ALREAY CONNECT", 500 );
}


//su  esp8266 sub mqtt topic function ?parameter is topicId & qos  ,include  parameter declaration 
bool ESP8266_Set_MQTT_Sub (const  char * topicId,const  char * qos)
{
	char cStr [100] = { 0 }, cCmd [120];
	//construct string: AT+MQTTSUB=0,"topic911",1,0
	sprintf ( cCmd, "AT+MQTTSUB=0,\"%s\",%s", topicId, qos );
	PC_Usart("??????: %s\r\n", cCmd); // ??????
	return ESP8266_Cmd (cCmd, "OK", "ALREAY CONNECT", 500);
}





/*
 * ????ESP8266_StartOrShutServer
 * ??  ?WF-ESP8266????????????
 * ??  ?enumMode???/??
 *       ?pPortNum??????????
 *       ?pTimeOver????????????????
 * ??  : 1?????
 *         0?????
 * ??  ??????
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
 * ????ESP8266_UnvarnishSend
 * ??  ???WF-ESP8266????????
 * ??  ??
 * ??  : 1?????
 *         0?????
 * ??  ??????
 */
bool ESP8266_UnvarnishSend ( void )
{
	return (
	  ESP8266_Cmd ( "AT+CIPMODE=1", "OK", 0, 500 ) &&
	  ESP8266_Cmd ( "AT+CIPSEND", "\r\n", ">", 500 ) );
	
}


/*
 * ????ESP8266_SendString
 * ??  ?WF-ESP8266???????
 * ??  ?enumEnUnvarnishTx?????????????
 *       ?pStr????????
 *       ?ulStrLength????????????
 *       ?ucId???ID??????
 * ??  : 1?????
 *         0?????
 * ??  ??????
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
 * ????ESP8266_ReceiveString
 * ??  ?WF-ESP8266???????
 * ??  ?enumEnUnvarnishTx?????????????
 * ??  : ??????????
 * ??  ??????
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
		if ( strstr ( strEsp8266_Fram_Record .Data_RX_BUF, "+IPD" ) )
			pRecStr = strEsp8266_Fram_Record .Data_RX_BUF;

	}

	return pRecStr;
	
}


/*
 * ????ESP8266_STA_TCP_Client
 * ??  ?PZ-ESP8266????STA TCP Clien??
 * ??  ??
 * ??  : ?
 * ??  ??????
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
		//PC_Usart ( "\r\n???????WiFi????????????????+????+????+???????\r\n" );
         // write english 		PC_Usart ( "\r\n???????WiFi????????????????+????+????+???????\r\n" );
		PC_Usart ( "\r\nPlease input the WiFi name and password to connect, format: SSID+comma+password+space, then click send\r\n" );
		scanf ( "%s", cStrInput );

		// write english PC_Usart ( "\r\n???? ??\r\n" );
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
	// write english	PC_Usart ( "\r\n?????????????TCP Server???????????IP?????????????IP+????+???+???????\r\n" );
		PC_Usart ( "\r\nPlease connect the network debugging assistant to the TCP Server on your computer, and input the computer's IP and port number, format: IP+comma+port+space, then click send\r\n" );

		scanf ( "%s", cStrInput );

	// write english		PC_Usart ( "\r\n???? ??\r\n" );
		

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
		// write english	 PC_Usart ( "\r\n?????ID%d???????????????????????+???????\r\n", uc );
		PC_Usart ( "\r\nPlease input the string to send for port ID%d, format: string (no spaces) + space, then click send\r\n", uc );

		scanf ( "%s", cStrInput );

		ul = strlen ( cStrInput );
		
		ESP8266_SendString ( DISABLE, cStrInput, ul, ( ENUM_ID_NO_TypeDef ) uc );
		
	}
	
	
	// write english PC_Usart ( "\r\n?????????????\r\n" );
	PC_Usart ( "\r\nPlease send strings in the network debugging assistant\r\n" );
	while (1)
	{
	  pStr = ESP8266_ReceiveString ( DISABLE );
		PC_Usart ( "%s", pStr );
	}

}





/*
 * ????ESP8266_STA_TCP_Client_Single
 * ??  ?ESP8266????STA TCP Client?????????
 * ??  ??
 * ??  : ?
 * ??  ??????
 */

// ????????????????????????


void ESP8266_STA_TCP_Client_Single ( void )
{
    // ????
    const char* ssid = "x";                
    const char* password = "12345678";        
    const char* server_ip = "192.168.254.195"; 
    const char* server_port = "8080";        
    
    // **????????????**
    u8 wifi_retry = 0;
    bool wifi_connected = false;
    u8 server_retry = 0;
    bool server_connected = false;
    const char* test_data;
    u32 data_len;

    // ????
    PC_Usart("\r\nInitializing ESP8266...\r\n");
    ESP8266_Choose(ENABLE);
    ESP8266_AT_Test();
    
    // ??STA??
    PC_Usart("\r\nConnecting to WiFi: %s...\r\n", ssid);
    ESP8266_Net_Mode_Choose(STA);
    
    // WiFi???????????????????
    while (wifi_retry < 3 && !wifi_connected) {
        wifi_connected = ESP8266_JoinAP((char*)ssid, (char*)password);
        if (!wifi_connected) {
            PC_Usart("\r\nWiFi connection failed, retrying %d/3...\r\n", wifi_retry+1);
            wifi_retry++;
            delay_ms(1000);
        }
    }
    
    // ?????
    //ESP8266_Enable_MultipleId(DISABLE); // ?????
    
    // ?????
    PC_Usart("\r\nConnecting to server: %s:%s...\r\n", server_ip, server_port);
    while (server_retry < 3 && !server_connected) {
        // ?????????uint8_t???????uint8_t?
        server_connected = ESP8266_Link_Server((uint8_t)enumTCP, (char*)server_ip, (char*)server_port, 9); // ??Single_ID=0
        if (!server_connected) {
            PC_Usart("\r\nServer connection failed, retrying %d/3...\r\n", server_retry+1);
            server_retry++;
            delay_ms(1000);
        }
    }
    
    // ??????????????
    test_data = "Hello from ESP8266!";
    data_len = strlen(test_data);
    PC_Usart("\r\nSending test data: %s\r\n", test_data);
    if (ESP8266_SendString(DISABLE, (char*)test_data, data_len, 0)) { // ??Single_ID=0
        PC_Usart("\r\nData sent successfully!\r\n");
    } else {
        PC_Usart("\r\nFailed to send data!\r\n");
    }
    
    // ????
    while (1) { /* ... */ }
}





/*
 * ????ESP8266_STA_TCP_Client_MQTT
 * ??  ?ESP8266????STA TCP Client?????????
 * ??  ??
 * ??  : ?
 * ??  ??????
 */

// ????????????????????????


void ESP8266_STA_TCP_Client_MQTT ( void )
{
    // ????
    const char* ssid = "x";                
    const char* password = "12345678";        
    const char* server_ip = "192.168.254.195"; 
    const char* server_port = "1883";        
    
    // **????????????**
    u8 wifi_retry = 0;
    bool wifi_connected = false;
    u8 server_retry = 0;
    bool server_connected = false;
    u8 setUser_retry = 0;   // ???????????
    bool setUser_mqtt = false;  // ???????????
	u8 setPublic_retry = 0;   // ???????????
	bool setPublic_mqtt = false;  // ???????????
	// sub mqtt topic retry
	u8 sub_retry = 0;   // ?????????
	bool sub_mqtt = false;  // ?????????
	//sub topic id
	const char* topic_id = "topic911"; // ???????ID?topic911 
	// sub qos
	char qos[] = "1"; // ??????????
	// ??????????????



    const char* test_data;
    u32 data_len;

    // ????
    PC_Usart("\r\nInitializing ESP8266...\r\n");
    ESP8266_Choose(ENABLE);
    ESP8266_AT_Test();
    
    // ??STA??
    PC_Usart("\r\nConnecting to WiFi: %s...\r\n", ssid);
    ESP8266_Net_Mode_Choose(STA);
    
    // WiFi???????????????????
    while (wifi_retry < 3 && !wifi_connected) {
        wifi_connected = ESP8266_JoinAP((char*)ssid, (char*)password);
        if (!wifi_connected) {
            PC_Usart("\r\nWiFi connection failed, retrying %d/3...\r\n", wifi_retry+1);
            wifi_retry++;
            delay_ms(1000);
        }
    }
    
    // ?????
    //ESP8266_Enable_MultipleId(DISABLE); // ?????

    //setUser_mqtt  in while loop  3 times
	PC_Usart("\r\nSetting MQTT user...\r\n");
	while (setUser_retry < 3 && !setUser_mqtt) {
		setUser_mqtt = ESP8266_Set_MQTT_User();
		if (!setUser_mqtt) {
			PC_Usart("\r\nSetting MQTT user failed, retrying %d/3...\r\n", setUser_retry+1);
			setUser_retry++;
			delay_ms(1000);
		}
	}

	// link mqtt server
	PC_Usart("\r\nConnecting to MQTT server...\r\n");
	while (server_retry < 3 && !server_connected) {
		// ?????????uint8_t???????uint8_t?
		server_connected = ESP8266_Link_MQTT((char*)server_ip, (char*)server_port, 9); // ??Single_ID=0
		if (!server_connected) {
			PC_Usart("\r\nMQTT server connection failed, retrying %d/3...\r\n", server_retry+1);
			server_retry++;
			delay_ms(1000);
		}
	}


// setPublic_mqtt  in while loop  1 time
	PC_Usart("\r\n MQTT public topic...\r\n");
	while (setPublic_retry < 3 && !setPublic_mqtt) {
		char topicId[] = "topic911";
		char val[] = "{status: 1}"; // ??JSON??
		setPublic_mqtt = ESP8266_Set_MQTT_Public(topicId, val);
		if (!setPublic_mqtt) {
			PC_Usart("\r\nSetting MQTT public topic failed, retrying %d/3...\r\n", setPublic_retry+1);
			setPublic_retry++;
			delay_ms(1000);
		}
	}


	// sub mqtt topic
	PC_Usart("\r\nSubscribing to MQTT topic...\r\n");
	while (sub_retry < 3 && !sub_mqtt) {
		sub_mqtt = ESP8266_Set_MQTT_Sub(topic_id,qos); // ??qos?1
		if (!sub_mqtt) {
			PC_Usart("\r\nSubscribing to MQTT topic failed, retrying %d/3...\r\n", sub_retry+1);
			sub_retry++;
			delay_ms(1000);



		}
	}



    
    // ??????????????
    // test_data = "Hello from ESP8266!";
    // data_len = strlen(test_data);
    // PC_Usart("\r\nSending test data: %s\r\n", test_data);
    // if (ESP8266_SendString(DISABLE, (char*)test_data, data_len, 0)) { // ??Single_ID=0
    //     PC_Usart("\r\nData sent successfully!\r\n");
    // } else {
    //     PC_Usart("\r\nFailed to send data!\r\n");
    // }
    
    // ????
	// while (1) {

	// }
}


/*
 * ????ESP8266_AP_TCP_Server
 * ??  ?PZ-ESP8266????AP TCP Server??
 * ??  ??
 * ??  : ?
 * ??  ??????
 */
void ESP8266_AP_TCP_Server ( void )
{
	char cStrInput [100] = { 0 }, * pStrDelimiter [3], * pBuf, * pStr;
	u8 uc = 0;
  u32 ul = 0;

  ESP8266_Choose ( ENABLE );

	ESP8266_AT_Test ();
	
	ESP8266_Net_Mode_Choose ( AP );


	PC_Usart ( "\r\n???????WiFi?????????????????????\
              \r\n0 = OPEN\
              \r\n1 = WEP\
              \r\n2 = WPA_PSK\
	            \r\n3 = WPA2_PSK\
              \r\n4 = WPA_WPA2_PSK\
							\r\n??????????+????+??????+????+????+???????\r\n" );

	scanf ( "%s", cStrInput );

	PC_Usart ( "\r\n???? ??\r\n" );

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
		
	
	PC_Usart ( "\r\n???????????????????0~28800??????????????????+????+??????+???????\r\n" );

	scanf ( "%s", cStrInput );

	PC_Usart ( "\r\n???? ??\r\n" );

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
		PC_Usart ( "\r\n??????IP??\r\n" );
	  ESP8266_Cmd ( "AT+CIFSR", "OK", "Link", 500 );
		
		PC_Usart ( "\r\n???????????WiFi?????????????ID0?????????????TCP Client???????????AP IP???\r\n" );
		delay_ms ( 20000 ) ;
	}	while ( ! ESP8266_Cmd ( "AT+CIPSTATUS", "+CIPSTATUS:0", 0, 500 ) );
	

	PC_Usart ( "\r\n??????????ID0???????????????????????+???????\r\n" );

	scanf ( "%s", cStrInput );

	ul = strlen ( cStrInput );
	
	ESP8266_SendString ( DISABLE, cStrInput, ul, Multiple_ID_0 );

	
	PC_Usart ( "\r\n???????????????\r\n" );
	while (1)
	{
	  pStr = ESP8266_ReceiveString ( DISABLE );
		PC_Usart ( "%s", pStr );
	}

}


/*
 * ????ESP8266_StaTcpClient_ApTcpServer
 * ??  ?PZ-ESP8266????STA(TCP Client)+AP(TCP Server)??
 * ??  ??
 * ??  : ?
 * ??  ??????
 */
void ESP8266_StaTcpClient_ApTcpServer ( void )
{
	char cStrInput [100] = { 0 }, * pStrDelimiter [3], * pBuf, * pStr;
	u8 uc = 0;
  u32 ul = 0;

  ESP8266_Choose ( ENABLE );

	ESP8266_AT_Test ();
	
	ESP8266_Net_Mode_Choose ( STA_AP );


	PC_Usart ( "\r\n???????WiFi?????????????????????\
						\r\n0 = OPEN\
						\r\n1  =WEP\
						\r\n2 = WPA_PSK\
						\r\n3 = WPA2_PSK\
						\r\n4 = WPA_WPA2_PSK\
						\r\n??????????+????+??????+????+????+???????\r\n" );

	scanf ( "%s", cStrInput );

	PC_Usart ( "\r\n???? ??\r\n" );

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
		PC_Usart ( "\r\n???????WiFi????????????????+????+????+???????\r\n" );

		scanf ( "%s", cStrInput );

		PC_Usart ( "\r\n???? ??\r\n" );

		pBuf = cStrInput;
		uc = 0;
		while ( ( pStr = strtok ( pBuf, "," ) ) != NULL )
		{
			pStrDelimiter [ uc ++ ] = pStr;
			pBuf = NULL;
		} 
		
  } while ( ! ESP8266_JoinAP ( pStrDelimiter [0], pStrDelimiter [1] ) );

	
	ESP8266_Enable_MultipleId ( ENABLE );
		
	
	PC_Usart ( "\r\n???????????????????0~28800??????????????????+????+??????+???????\r\n" );

	scanf ( "%s", cStrInput );

	PC_Usart ( "\r\n???? ??\r\n" );

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
		PC_Usart ( "\r\n?????????????TCP Server???????????IP?????????????IP+????+???+???????\r\n" );

		scanf ( "%s", cStrInput );

		PC_Usart ( "\r\n???? ??\r\n" );

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
		PC_Usart ( "\r\n??????IP?????AP IP?????STA IP??\r\n" );
	  ESP8266_Cmd ( "AT+CIFSR", "OK", "Link", 500 );
		
		PC_Usart ( "\r\n???????????WiFi?????????????ID3?????????????TCP Client???????????AP IP???\r\n" );
		delay_ms ( 20000 ) ;
	}	while ( ! ESP8266_Cmd ( "AT+CIPSTATUS", "+CIPSTATUS:3", 0, 500 ) );
	

	for ( uc = 0; uc < 3; uc ++ )
	{
		PC_Usart ( "\r\n?????ID%d???????????????????????+???????\r\n", uc );

		scanf ( "%s", cStrInput );

		ul = strlen ( cStrInput );
		
		ESP8266_SendString ( DISABLE, cStrInput, ul, ( ENUM_ID_NO_TypeDef ) uc );
		
	}
	
	
	PC_Usart ( "\r\n??????????ID3???????????????????????+???????\r\n" );

	scanf ( "%s", cStrInput );

	ul = strlen ( cStrInput );
	
	ESP8266_SendString ( DISABLE, cStrInput, ul, Multiple_ID_3 );

	
	PC_Usart ( "\r\n??????????????????????????\r\n" );
	while (1)
	{
	  pStr = ESP8266_ReceiveString ( DISABLE );
		PC_Usart ( "%s", pStr );
	}
	
}


bool ESP8266_MQTT_Init_Default(void)
{
	u8 retry;
	u8 idx;
	u8 routeIdx;
	bool ok;
	static const u32 baudList[] = {115200, 9600, 57600, 74880, 38400, 19200};
	static const ESP_UART_RouteTypeDef routes[] =
	{
		ESP_UART_ROUTE_USART3_PB10_PB11,
		ESP_UART_ROUTE_USART3_PC10_PC11,
		ESP_UART_ROUTE_USART3_PD8_PD9,
		ESP_UART_ROUTE_USART2_PA2_PA3,
		ESP_UART_ROUTE_USART2_PD5_PD6,
		ESP_UART_ROUTE_UART4_PC10_PC11,
	};
	static const char *routeName[] =
	{
		"USART3 PB10/PB11",
		"USART3 PC10/PC11",
		"USART3 PD8/PD9",
		"USART2 PA2/PA3",
		"USART2 PD5/PD6",
		"UART4 PC10/PC11"
	};

	ESP8266_Choose(ENABLE);
	delay_ms(30);
	ESP8266_RST_LOW_LEVEL();
	delay_ms(220);
	ESP8266_RST_HIGH_LEVEL();
	delay_ms(1200);

	ok = false;
	for (routeIdx = 0; routeIdx < (sizeof(routes) / sizeof(routes[0])); routeIdx++)
	{
		WiFi_ESP8266_SelectRoute(routes[routeIdx]);
		delay_ms(30);
		PC_Usart("\r\n[ATDBG] ==== route %s ====\r\n", routeName[routeIdx]);
		for (idx = 0; idx < (sizeof(baudList) / sizeof(baudList[0])); idx++)
		{
			PC_Usart("[ATDBG] ---- baud candidate %d ----\r\n", baudList[idx]);
			if (ESP8266_TryATAtBaud(baudList[idx]))
			{
				ok = true;
				break;
			}
			PC_Usart("[ATDBG] baud=%d failed, toggle RST\r\n", baudList[idx]);
			ESP8266_Rst();
			delay_ms(150);
		}
		if (ok)
		{
			PC_Usart("[ATDBG] route locked: %s\r\n", routeName[routeIdx]);
			break;
		}
		PC_Usart("[ATDBG] route %s no response\r\n", routeName[routeIdx]);
	}
	if (!ok)
	{
		PC_Usart("\r\n[MQTT] AT no response (check UART jumpers/mux)\r\n");
		return false;
	}
	PC_Usart("\r\n[MQTT] ESP baud=%d\r\n", g_esp8266_baud);
	ESP8266_Cmd("ATE0", "OK", NULL, 300);
	ok = ESP8266_Net_Mode_Choose(STA);
	if (!ok)
	{
		PC_Usart("\r\n[MQTT] CWMODE failed\r\n");
		return false;
	}
	retry = 0;
	ok = false;
	while ((retry < 5) && (!ok))
	{
		ok = ESP8266_JoinAP((char *)WIFI_STA_SSID, (char *)WIFI_STA_PASSWORD);
		if (!ok)
		{
			retry++;
			delay_ms(1500);
		}
	}
	if (!ok)
	{
		PC_Usart("\r\n[MQTT] Join AP failed\r\n");
		return false;
	}
	retry = 0;
	ok = false;
	while ((retry < 3) && (!ok))
	{
		char cmd[180];
		sprintf(cmd, "AT+MQTTUSERCFG=0,1,\"%s\",\"%s\",\"%s\",0,0,\"\"", MQTT_CLIENT_ID, MQTT_USERNAME, MQTT_PASSWORD);
		ok = ESP8266_Cmd(cmd, "OK", NULL, 1200);
		if (!ok)
		{
			retry++;
			delay_ms(800);
		}
	}
	if (!ok)
	{
		PC_Usart("\r\n[MQTT] MQTTUSERCFG failed\r\n");
		return false;
	}
	retry = 0;
	ok = false;
	while ((retry < 3) && (!ok))
	{
		ok = ESP8266_Link_MQTT((char *)MQTT_BROKER_HOST, (char *)MQTT_BROKER_PORT, Single_ID_0);
		if (!ok)
		{
			retry++;
			delay_ms(1200);
		}
	}
	if (!ok)
	{
		PC_Usart("\r\n[MQTT] MQTTCONN failed\r\n");
		return false;
	}
	retry = 0;
	ok = false;
	while ((retry < 3) && (!ok))
	{
		ok = ESP8266_Set_MQTT_Sub(MQTT_TOPIC_LED_CMD, "1");
		if (!ok)
		{
			retry++;
			delay_ms(600);
		}
	}
	if (!ok)
	{
		PC_Usart("\r\n[MQTT] MQTTSUB failed\r\n");
		return false;
	}
	PC_Usart("\r\n[MQTT] Ready. sub=%s pub=%s\r\n", MQTT_TOPIC_LED_CMD, MQTT_TOPIC_TEMP_PUB);
	return true;
}

bool ESP8266_MQTT_PublishTemperature(const char *tempText)
{
	char valueBuf[24];

	if (tempText == NULL)
	{
		return false;
	}

	strncpy(valueBuf, tempText, sizeof(valueBuf) - 1);
	valueBuf[sizeof(valueBuf) - 1] = '\0';

	return ESP8266_Set_MQTT_Public((char *)MQTT_TOPIC_TEMP_PUB, valueBuf);
}

bool ESP8266_MQTT_PollLedCommand(u8 *ledOn)
{
	char *line;
	char *payloadStart;
	char *lineEnd;
	char payload[40];
	int payloadLen;
	int i;

	if (ledOn == NULL)
	{
		return false;
	}

	if (strEsp8266_Fram_Record.InfBit.FramFinishFlag == 0)
	{
		return false;
	}

	strEsp8266_Fram_Record.Data_RX_BUF[strEsp8266_Fram_Record.InfBit.FramLength] = '\0';
	line = strstr(strEsp8266_Fram_Record.Data_RX_BUF, "+MQTTSUBRECV:");
	if (line == NULL)
	{
		strEsp8266_Fram_Record.InfBit.FramFinishFlag = 0;
		strEsp8266_Fram_Record.InfBit.FramLength = 0;
		return false;
	}

	payloadStart = strrchr(line, ',');
	if (payloadStart == NULL)
	{
		goto cleanup;
	}
	payloadStart++;

	while ((*payloadStart == ' ') || (*payloadStart == '\"'))
	{
		payloadStart++;
	}

	lineEnd = strchr(payloadStart, '\r');
	if (lineEnd == NULL)
	{
		lineEnd = strchr(payloadStart, '\n');
	}
	if (lineEnd == NULL)
	{
		lineEnd = payloadStart + strlen(payloadStart);
	}

	payloadLen = (int)(lineEnd - payloadStart);
	while ((payloadLen > 0) && ((payloadStart[payloadLen - 1] == '\"') || (payloadStart[payloadLen - 1] == ' ')))
	{
		payloadLen--;
	}

	if (payloadLen <= 0)
	{
		goto cleanup;
	}
	if (payloadLen > ((int)sizeof(payload) - 1))
	{
		payloadLen = (int)sizeof(payload) - 1;
	}

	for (i = 0; i < payloadLen; i++)
	{
		payload[i] = payloadStart[i];
	}
	payload[payloadLen] = '\0';

	if (ESP8266_StringEqualsIgnoreCase(payload, "ON") || ESP8266_StringEqualsIgnoreCase(payload, "1"))
	{
		*ledOn = 1;
		goto success;
	}
	if (ESP8266_StringEqualsIgnoreCase(payload, "OFF") || ESP8266_StringEqualsIgnoreCase(payload, "0"))
	{
		*ledOn = 0;
		goto success;
	}
	goto cleanup;

success:
	strEsp8266_Fram_Record.InfBit.FramFinishFlag = 0;
	strEsp8266_Fram_Record.InfBit.FramLength = 0;
	return true;

cleanup:
	strEsp8266_Fram_Record.InfBit.FramFinishFlag = 0;
	strEsp8266_Fram_Record.InfBit.FramLength = 0;
	return false;
}









