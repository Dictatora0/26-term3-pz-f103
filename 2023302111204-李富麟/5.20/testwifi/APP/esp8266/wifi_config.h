#ifndef __WIFI_CONFIG_H
#define __WIFI_CONFIG_H

#include "system.h"

#if defined ( __CC_ARM )
#pragma anon_unions
#endif

typedef enum{
    STA,
    AP,
    STA_AP
} ENUM_Net_ModeTypeDef;

typedef enum{
    enumTCP,
    enumUDP,
} ENUM_NetPro_TypeDef;

typedef enum{
    Multiple_ID_0 = 0,
    Multiple_ID_1 = 1,
    Multiple_ID_2 = 2,
    Multiple_ID_3 = 3,
    Multiple_ID_4 = 4,
    Single_ID_0 = 5,
} ENUM_ID_NO_TypeDef;

typedef enum{
    ESP_USART3_PINMAP_PB10_PB11 = 0,
    ESP_USART3_PINMAP_PC10_PC11 = 1,
} ESP_USART3_PinMapTypeDef;

typedef enum{
    ESP_UART_ROUTE_USART3_PB10_PB11 = 0,
    ESP_UART_ROUTE_USART3_PC10_PC11,
    ESP_UART_ROUTE_USART3_PD8_PD9,
    ESP_UART_ROUTE_USART2_PA2_PA3,
    ESP_UART_ROUTE_USART2_PD5_PD6,
    ESP_UART_ROUTE_UART4_PC10_PC11,
} ESP_UART_RouteTypeDef;

#define RX_BUF_MAX_LEN     1024

extern volatile u32 g_usart3_rx_total;
extern volatile u32 g_usart3_idle_total;
extern volatile u32 g_usart3_overflow_total;
extern volatile u8  g_usart3_last_rx;

extern struct STRUCT_USARTx_Fram
{
    char Data_RX_BUF[RX_BUF_MAX_LEN];

    union {
        __IO u16 InfAll;
        struct {
            __IO u16 FramLength     :15;
            __IO u16 FramFinishFlag :1;
        } InfBit;
    };

} strPc_Fram_Record, strEsp8266_Fram_Record;

void WiFi_Config(void);
void WiFi_ESP8266_SetUart3PinMap(ESP_USART3_PinMapTypeDef pinMap);
void WiFi_ESP8266_SelectRoute(ESP_UART_RouteTypeDef route);
USART_TypeDef *WiFi_ESP8266_GetUart(void);
void WiFi_ESP8266_SetEnable(FunctionalState en);
void WiFi_ESP8266_SetReset(FunctionalState highLevel);
void USART3_printf(USART_TypeDef* USARTx, char *Data, ...);

#endif /* __WIFI_CONFIG_H */
