#include "wifi_config.h"
#include <stdio.h>
#include <stdarg.h>

struct STRUCT_USARTx_Fram strPc_Fram_Record = {0};
struct STRUCT_USARTx_Fram strEsp8266_Fram_Record = {0};
volatile u8 g_esp8266_rx_overflow = 0U;

static char *itoa10(int value, char *string)
{
    int i;
    int d;
    int flag = 0;
    char *ptr = string;

    if (value == 0)
    {
        *ptr++ = '0';
        *ptr = '\0';
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
        if ((d != 0) || flag)
        {
            *ptr++ = (char)(d + '0');
            value -= d * i;
            flag = 1;
        }
    }

    *ptr = '\0';
    return string;
}

void WiFi_Config(void)
{
    GPIO_InitTypeDef GPIO_InitStructure;
    USART_InitTypeDef USART_InitStructure;
    NVIC_InitTypeDef NVIC_InitStructure;

    RCC_APB1PeriphClockCmd(RCC_APB1Periph_USART3, ENABLE);
    RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIOB, ENABLE);
    RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIOA, ENABLE);
    RCC_APB2PeriphClockCmd(RCC_APB2Periph_AFIO, ENABLE);
    GPIO_PinRemapConfig(GPIO_Remap_SWJ_JTAGDisable, ENABLE);

    GPIO_InitStructure.GPIO_Pin = GPIO_Pin_4 | GPIO_Pin_15;
    GPIO_InitStructure.GPIO_Mode = GPIO_Mode_Out_PP;
    GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
    GPIO_Init(GPIOA, &GPIO_InitStructure);
    GPIO_ResetBits(GPIOA, GPIO_Pin_4);
    GPIO_SetBits(GPIOA, GPIO_Pin_15);

    GPIO_InitStructure.GPIO_Pin = GPIO_Pin_10;
    GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF_PP;
    GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
    GPIO_Init(GPIOB, &GPIO_InitStructure);

    GPIO_InitStructure.GPIO_Pin = GPIO_Pin_11;
    GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IN_FLOATING;
    GPIO_Init(GPIOB, &GPIO_InitStructure);

    USART_InitStructure.USART_BaudRate = 9600U;
    USART_InitStructure.USART_WordLength = USART_WordLength_8b;
    USART_InitStructure.USART_StopBits = USART_StopBits_1;
    USART_InitStructure.USART_Parity = USART_Parity_No;
    USART_InitStructure.USART_HardwareFlowControl = USART_HardwareFlowControl_None;
    USART_InitStructure.USART_Mode = USART_Mode_Rx | USART_Mode_Tx;
    USART_Init(USART3, &USART_InitStructure);

    USART_ITConfig(USART3, USART_IT_RXNE, ENABLE);
    USART_ITConfig(USART3, USART_IT_IDLE, ENABLE);
    USART_Cmd(USART3, ENABLE);

    strEsp8266_Fram_Record.InfAll = 0U;
    strEsp8266_Fram_Record.Data_RX_BUF[0] = '\0';
    g_esp8266_rx_overflow = 0U;

    NVIC_InitStructure.NVIC_IRQChannel = USART3_IRQn;
    NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0;
    NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0;
    NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;
    NVIC_Init(&NVIC_InitStructure);
}

void USART3_IRQHandler(void)
{
    char ch;
    u16 len;
    volatile u32 dummy;

    if (USART_GetITStatus(USART3, USART_IT_RXNE) != RESET)
    {
        ch = (char)USART_ReceiveData(USART3);
        if (strEsp8266_Fram_Record.InfBit.FramLength < (RX_BUF_MAX_LEN - 1U))
        {
            strEsp8266_Fram_Record.Data_RX_BUF[strEsp8266_Fram_Record.InfBit.FramLength++] = ch;
            strEsp8266_Fram_Record.Data_RX_BUF[strEsp8266_Fram_Record.InfBit.FramLength] = '\0';
        }
        else
        {
            g_esp8266_rx_overflow = 1U;
        }
    }

    if (USART_GetITStatus(USART3, USART_IT_IDLE) == SET)
    {
        len = strEsp8266_Fram_Record.InfBit.FramLength;
        if (len >= RX_BUF_MAX_LEN)
        {
            len = RX_BUF_MAX_LEN - 1U;
        }
        strEsp8266_Fram_Record.Data_RX_BUF[len] = '\0';
        strEsp8266_Fram_Record.InfBit.FramFinishFlag = 1U;
        dummy = USART3->SR;
        dummy = USART3->DR;
        (void)dummy;
    }
}

void USART3_printf(USART_TypeDef *USARTx, char *Data, ...)
{
    const char *s;
    int d;
    char buf[16];
    va_list ap;

    va_start(ap, Data);

    while (*Data != '\0')
    {
        if (*Data == '\\')
        {
            switch (*++Data)
            {
            case 'r':
                USART_SendData(USARTx, 0x0DU);
                Data++;
                break;

            case 'n':
                USART_SendData(USARTx, 0x0AU);
                Data++;
                break;

            case '"':
                USART_SendData(USARTx, 0x22U);
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
                for (; *s != '\0'; s++)
                {
                    USART_SendData(USARTx, *s);
                    while (USART_GetFlagStatus(USARTx, USART_FLAG_TXE) == RESET)
                    {
                    }
                }
                Data++;
                break;

            case 'd':
                d = va_arg(ap, int);
                itoa10(d, buf);
                for (s = buf; *s != '\0'; s++)
                {
                    USART_SendData(USARTx, *s);
                    while (USART_GetFlagStatus(USARTx, USART_FLAG_TXE) == RESET)
                    {
                    }
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

        while (USART_GetFlagStatus(USARTx, USART_FLAG_TXE) == RESET)
        {
        }
    }

    va_end(ap);
}

void WiFi_SetUsart3Baud(u32 baud)
{
    USART_InitTypeDef USART_InitStructure;

    USART_Cmd(USART3, DISABLE);
    USART_InitStructure.USART_BaudRate = baud;
    USART_InitStructure.USART_WordLength = USART_WordLength_8b;
    USART_InitStructure.USART_StopBits = USART_StopBits_1;
    USART_InitStructure.USART_Parity = USART_Parity_No;
    USART_InitStructure.USART_HardwareFlowControl = USART_HardwareFlowControl_None;
    USART_InitStructure.USART_Mode = USART_Mode_Rx | USART_Mode_Tx;
    USART_Init(USART3, &USART_InitStructure);
    USART_Cmd(USART3, ENABLE);
}
