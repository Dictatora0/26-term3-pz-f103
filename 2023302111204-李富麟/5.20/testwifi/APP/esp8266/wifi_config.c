#include "wifi_config.h"
#include <stdio.h>
#include <stdarg.h>

struct STRUCT_USARTx_Fram strEsp8266_Fram_Record = { 0 };
volatile u32 g_usart3_rx_total = 0;
volatile u32 g_usart3_idle_total = 0;
volatile u32 g_usart3_overflow_total = 0;
volatile u8  g_usart3_last_rx = 0;

static ESP_UART_RouteTypeDef g_esp_route = ESP_UART_ROUTE_USART3_PB10_PB11;
static USART_TypeDef *g_esp_uart = USART3;

static void WiFi_ESP8266_EnableCurrentUartClock(void)
{
    if (g_esp_uart == USART2)
    {
        RCC_APB1PeriphClockCmd(RCC_APB1Periph_USART2, ENABLE);
    }
    else if (g_esp_uart == UART4)
    {
        RCC_APB1PeriphClockCmd(RCC_APB1Periph_UART4, ENABLE);
    }
    else
    {
        RCC_APB1PeriphClockCmd(RCC_APB1Periph_USART3, ENABLE);
    }
}

static void WiFi_ESP8266_DisableAllUartClock(void)
{
    RCC_APB1PeriphClockCmd(RCC_APB1Periph_USART2, DISABLE);
    RCC_APB1PeriphClockCmd(RCC_APB1Periph_USART3, DISABLE);
    RCC_APB1PeriphClockCmd(RCC_APB1Periph_UART4, DISABLE);
}

static void WiFi_ESP8266_ConfigCurrentUartNvic(void)
{
    NVIC_InitTypeDef NVIC_InitStructure;

    NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0;
    NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0;
    NVIC_InitStructure.NVIC_IRQChannelCmd = DISABLE;

    NVIC_InitStructure.NVIC_IRQChannel = USART2_IRQn;
    NVIC_Init(&NVIC_InitStructure);
    NVIC_InitStructure.NVIC_IRQChannel = USART3_IRQn;
    NVIC_Init(&NVIC_InitStructure);
    NVIC_InitStructure.NVIC_IRQChannel = UART4_IRQn;
    NVIC_Init(&NVIC_InitStructure);

    NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;
    if (g_esp_uart == USART2)
    {
        NVIC_InitStructure.NVIC_IRQChannel = USART2_IRQn;
    }
    else if (g_esp_uart == UART4)
    {
        NVIC_InitStructure.NVIC_IRQChannel = UART4_IRQn;
    }
    else
    {
        NVIC_InitStructure.NVIC_IRQChannel = USART3_IRQn;
    }
    NVIC_Init(&NVIC_InitStructure);
}

static void WiFi_ESP8266_ConfigCtrlPins(void)
{
    GPIO_InitTypeDef GPIO_InitStructure;

    RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIOA, ENABLE);
    RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIOB, ENABLE);

    GPIO_InitStructure.GPIO_Pin = GPIO_Pin_4 | GPIO_Pin_15;
    GPIO_InitStructure.GPIO_Mode = GPIO_Mode_Out_PP;
    GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
    GPIO_Init(GPIOA, &GPIO_InitStructure);

    GPIO_InitStructure.GPIO_Pin = GPIO_Pin_8 | GPIO_Pin_9;
    GPIO_Init(GPIOB, &GPIO_InitStructure);

    /* drive both possible EN/RST pins to safe defaults */
    GPIO_ResetBits(GPIOA, GPIO_Pin_4);
    GPIO_SetBits(GPIOA, GPIO_Pin_15);
    GPIO_ResetBits(GPIOB, GPIO_Pin_8);
    GPIO_SetBits(GPIOB, GPIO_Pin_9);
}

void WiFi_ESP8266_SetEnable(FunctionalState en)
{
    if (en == ENABLE)
    {
        GPIO_SetBits(GPIOA, GPIO_Pin_4);
        GPIO_SetBits(GPIOB, GPIO_Pin_8);
    }
    else
    {
        GPIO_ResetBits(GPIOA, GPIO_Pin_4);
        GPIO_ResetBits(GPIOB, GPIO_Pin_8);
    }
}

void WiFi_ESP8266_SetReset(FunctionalState highLevel)
{
    if (highLevel == ENABLE)
    {
        GPIO_SetBits(GPIOA, GPIO_Pin_15);
        GPIO_SetBits(GPIOB, GPIO_Pin_9);
    }
    else
    {
        GPIO_ResetBits(GPIOA, GPIO_Pin_15);
        GPIO_ResetBits(GPIOB, GPIO_Pin_9);
    }
}

static void WiFi_ESP8266_InitUartPin(GPIO_TypeDef *txPort, u16 txPin, GPIO_TypeDef *rxPort, u16 rxPin)
{
    GPIO_InitTypeDef GPIO_InitStructure;

    GPIO_InitStructure.GPIO_Pin = txPin;
    GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
    GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF_PP;
    GPIO_Init(txPort, &GPIO_InitStructure);

    GPIO_InitStructure.GPIO_Pin = rxPin;
    GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IN_FLOATING;
    GPIO_Init(rxPort, &GPIO_InitStructure);
}

void WiFi_ESP8266_SetUart3PinMap(ESP_USART3_PinMapTypeDef pinMap)
{
    if (pinMap == ESP_USART3_PINMAP_PC10_PC11)
    {
        WiFi_ESP8266_SelectRoute(ESP_UART_ROUTE_USART3_PC10_PC11);
    }
    else
    {
        WiFi_ESP8266_SelectRoute(ESP_UART_ROUTE_USART3_PB10_PB11);
    }
}

void WiFi_ESP8266_SelectRoute(ESP_UART_RouteTypeDef route)
{
    g_esp_route = route;

    RCC_APB2PeriphClockCmd(RCC_APB2Periph_AFIO, ENABLE);
    RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIOA, ENABLE);
    RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIOB, ENABLE);
    RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIOC, ENABLE);
    RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIOD, ENABLE);

    GPIO_PinRemapConfig(GPIO_PartialRemap_USART3, DISABLE);
    GPIO_PinRemapConfig(GPIO_FullRemap_USART3, DISABLE);
    GPIO_PinRemapConfig(GPIO_Remap_USART2, DISABLE);

    if (route == ESP_UART_ROUTE_USART3_PC10_PC11)
    {
        g_esp_uart = USART3;
        GPIO_PinRemapConfig(GPIO_PartialRemap_USART3, ENABLE);
        WiFi_ESP8266_InitUartPin(GPIOC, GPIO_Pin_10, GPIOC, GPIO_Pin_11);
    }
    else if (route == ESP_UART_ROUTE_USART3_PD8_PD9)
    {
        g_esp_uart = USART3;
        GPIO_PinRemapConfig(GPIO_FullRemap_USART3, ENABLE);
        WiFi_ESP8266_InitUartPin(GPIOD, GPIO_Pin_8, GPIOD, GPIO_Pin_9);
    }
    else if (route == ESP_UART_ROUTE_USART2_PA2_PA3)
    {
        g_esp_uart = USART2;
        WiFi_ESP8266_InitUartPin(GPIOA, GPIO_Pin_2, GPIOA, GPIO_Pin_3);
    }
    else if (route == ESP_UART_ROUTE_USART2_PD5_PD6)
    {
        g_esp_uart = USART2;
        GPIO_PinRemapConfig(GPIO_Remap_USART2, ENABLE);
        WiFi_ESP8266_InitUartPin(GPIOD, GPIO_Pin_5, GPIOD, GPIO_Pin_6);
    }
    else if (route == ESP_UART_ROUTE_UART4_PC10_PC11)
    {
        g_esp_uart = UART4;
        WiFi_ESP8266_InitUartPin(GPIOC, GPIO_Pin_10, GPIOC, GPIO_Pin_11);
    }
    else
    {
        g_esp_uart = USART3;
        WiFi_ESP8266_InitUartPin(GPIOB, GPIO_Pin_10, GPIOB, GPIO_Pin_11);
    }

    WiFi_ESP8266_DisableAllUartClock();
    WiFi_ESP8266_EnableCurrentUartClock();
    WiFi_ESP8266_ConfigCurrentUartNvic();
}

USART_TypeDef *WiFi_ESP8266_GetUart(void)
{
    return g_esp_uart;
}

static void WiFi_ESP8266_CommonIrqHandler(USART_TypeDef *uart)
{
    char ch;

    if (USART_GetITStatus(uart, USART_IT_RXNE) != RESET)
    {
        ch = USART_ReceiveData(uart);
        g_usart3_last_rx = (u8)ch;
        g_usart3_rx_total++;

        if (strEsp8266_Fram_Record.InfBit.FramLength < (RX_BUF_MAX_LEN - 1))
        {
            strEsp8266_Fram_Record.Data_RX_BUF[strEsp8266_Fram_Record.InfBit.FramLength++] = ch;
        }
        else
        {
            g_usart3_overflow_total++;
        }
    }

    if (USART_GetITStatus(uart, USART_IT_IDLE) == SET)
    {
        strEsp8266_Fram_Record.InfBit.FramFinishFlag = 1;
        g_usart3_idle_total++;
        ch = USART_ReceiveData(uart);
        (void)ch;
    }
}

void WiFi_Config(void)
{
    USART_InitTypeDef USART_InitStructure;

    GPIO_PinRemapConfig(GPIO_Remap_SWJ_JTAGDisable, ENABLE);

    WiFi_ESP8266_ConfigCtrlPins();
    WiFi_ESP8266_SelectRoute(g_esp_route);

    USART_InitStructure.USART_BaudRate = 115200;
    USART_InitStructure.USART_WordLength = USART_WordLength_8b;
    USART_InitStructure.USART_StopBits = USART_StopBits_1;
    USART_InitStructure.USART_Parity = USART_Parity_No;
    USART_InitStructure.USART_HardwareFlowControl = USART_HardwareFlowControl_None;
    USART_InitStructure.USART_Mode = USART_Mode_Rx | USART_Mode_Tx;
    USART_Init(g_esp_uart, &USART_InitStructure);

    USART_ITConfig(g_esp_uart, USART_IT_RXNE, ENABLE);
    USART_ITConfig(g_esp_uart, USART_IT_IDLE, ENABLE);

    USART_Cmd(g_esp_uart, ENABLE);
}

void USART2_IRQHandler(void)
{
    if (g_esp_uart == USART2)
    {
        WiFi_ESP8266_CommonIrqHandler(USART2);
    }
}

void USART3_IRQHandler(void)
{
    if (g_esp_uart == USART3)
    {
        WiFi_ESP8266_CommonIrqHandler(USART3);
    }
}

void UART4_IRQHandler(void)
{
    if (g_esp_uart == UART4)
    {
        WiFi_ESP8266_CommonIrqHandler(UART4);
    }
}

static char *itoa(int value, char *string, int radix)
{
    int i, d;
    int flag = 0;
    char *ptr = string;

    if (radix != 10)
    {
        *ptr = 0;
        return string;
    }

    if (!value)
    {
        *ptr++ = 0x30;
        *ptr = 0;
        return string;
    }

    if (value < 0)
    {
        *ptr++ = '-';
        value *= -1;
    }

    for (i = 10000; i > 0; i /= 10)
    {
        d = value / i;

        if (d || flag)
        {
            *ptr++ = (char)(d + 0x30);
            value -= (d * i);
            flag = 1;
        }
    }

    *ptr = 0;
    return string;
}

void USART3_printf(USART_TypeDef* USARTx, char *Data, ...)
{
    const char *s;
    int d;
    char buf[16];
    va_list ap;

    va_start(ap, Data);

    while (*Data != 0)
    {
        if (*Data == 0x5c)
        {
            switch (*++Data)
            {
                case 'r':
                    USART_SendData(USARTx, 0x0d);
                    Data++;
                    break;

                case 'n':
                    USART_SendData(USARTx, 0x0a);
                    Data++;
                    break;

                case '"':
                    USART_SendData(USARTx, 0x22);
                    Data++;
                    break;

                default:
                    Data++;
                    break;
            }
        }
        else if (*Data == '%')
        {
            switch (*++Data)
            {
                case 's':
                    s = va_arg(ap, const char *);
                    for (; *s; s++)
                    {
                        USART_SendData(USARTx, *s);
                        while (USART_GetFlagStatus(USARTx, USART_FLAG_TXE) == RESET);
                    }
                    Data++;
                    break;

                case 'd':
                    d = va_arg(ap, int);
                    itoa(d, buf, 10);
                    for (s = buf; *s; s++)
                    {
                        USART_SendData(USARTx, *s);
                        while (USART_GetFlagStatus(USARTx, USART_FLAG_TXE) == RESET);
                    }
                    Data++;
                    break;

                default:
                    Data++;
                    break;
            }
        }
        else
        {
            USART_SendData(USARTx, *Data++);
        }

        while (USART_GetFlagStatus(USARTx, USART_FLAG_TXE) == RESET);
    }
}
