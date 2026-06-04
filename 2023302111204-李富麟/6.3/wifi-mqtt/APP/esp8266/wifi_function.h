#ifndef __WIFI_FUNCTION_H
#define __WIFI_FUNCTION_H

#include "system.h"
#include "wifi_config.h"
#include <stdbool.h>
#include <stdio.h>

#define ESP8266_Usart(fmt, ...) USART3_printf(USART3, fmt, ##__VA_ARGS__)
#define PC_Usart(fmt, ...)      printf(fmt, ##__VA_ARGS__)

#define ESP8266_CH_HIGH_LEVEL()  GPIO_SetBits(GPIOA, GPIO_Pin_4)
#define ESP8266_CH_LOW_LEVEL()   GPIO_ResetBits(GPIOA, GPIO_Pin_4)
#define ESP8266_RST_HIGH_LEVEL() GPIO_SetBits(GPIOA, GPIO_Pin_15)
#define ESP8266_RST_LOW_LEVEL()  GPIO_ResetBits(GPIOA, GPIO_Pin_15)

void ESP8266_ClearRxBuffer(void);
void ESP8266_Process(void);
void ESP8266_Choose(FunctionalState enumChoose);
void ESP8266_Rst(void);
bool ESP8266_Init(void);
void ESP8266_AT_Test(void);
bool ESP8266_Cmd(const char *cmd, const char *reply1, const char *reply2, u32 waittime);
bool ESP8266_Net_Mode_Choose(ENUM_Net_ModeTypeDef enumMode);
bool ESP8266_JoinAP(const char *pSSID, const char *pPassWord);
bool ESP8266_Enable_MultipleId(FunctionalState enumEnUnvarnishTx);
bool ESP8266_OpenTCP(const char *host, u16 port);
bool ESP8266_Send(const u8 *data, u16 length);
bool ESP8266_IsConnected(void);
u32 ESP8266_GetSelectedBaud(void);
bool ESP8266_Link_MQTT(const char *host, u16 port);
bool ESP8266_Set_MQTT_UserCfg(const char *client_id, const char *username, const char *password);
bool ESP8266_Set_MQTT_ConnCfg(u16 keepalive_seconds);
bool ESP8266_MQTT_Publish(const char *topicId, const char *val);
bool ESP8266_MQTT_Subscribe(const char *topicId, u8 qos);
bool ESP8266_MQTT_ParseSubFrame(const char *src, char *topic, u16 topic_size, char *payload, u16 payload_size);
bool ESP8266_Is_MQTT_Ready(void);
bool ESP8266_MQTT_PollMessage(char *topic, u16 topic_size, char *payload, u16 payload_size);

#endif
