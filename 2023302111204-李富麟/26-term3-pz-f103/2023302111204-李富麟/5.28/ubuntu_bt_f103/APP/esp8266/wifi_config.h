#ifndef __WIFI_CONFIG_H
#define __WIFI_CONFIG_H

#include "system.h"
#include <stdbool.h>

#if defined(__CC_ARM)
#pragma anon_unions
#endif

typedef enum {
    STA,
    AP,
    STA_AP
} ENUM_Net_ModeTypeDef;

typedef enum {
    enumTCP,
    enumUDP
} ENUM_NetPro_TypeDef;

typedef enum {
    Multiple_ID_0 = 0,
    Multiple_ID_1 = 1,
    Multiple_ID_2 = 2,
    Multiple_ID_3 = 3,
    Multiple_ID_4 = 4,
    Single_ID_0 = 5
} ENUM_ID_NO_TypeDef;

typedef enum {
    OPEN = 0,
    WEP = 1,
    WPA_PSK = 2,
    WPA2_PSK = 3,
    WPA_WPA2_PSK = 4
} ENUM_AP_PsdMode_TypeDef;

#define RX_BUF_MAX_LEN 1024

extern struct STRUCT_USARTx_Fram {
    char Data_RX_BUF[RX_BUF_MAX_LEN];
    union {
        __IO u16 InfAll;
        struct {
            __IO u16 FramLength : 15;
            __IO u16 FramFinishFlag : 1;
        } InfBit;
    };
} strPc_Fram_Record, strEsp8266_Fram_Record;

void WiFi_Config(void);
void WiFi_SetUsart3Baud(u32 baud);
void USART3_printf(USART_TypeDef *USARTx, char *Data, ...);

void BT_Config(void);
bool BT_IsConnected(void);
char *BT_TryReceiveLine(void);
void BT_SendRaw(const char *buf, u16 len);
void BT_SendLine(const char *line);

#endif
