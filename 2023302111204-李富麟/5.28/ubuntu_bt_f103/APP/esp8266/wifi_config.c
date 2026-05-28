#include "wifi_config.h"
#include "esp8266_mqtt_config.h"
#include <stdio.h>
#include <stdarg.h>
#include <string.h>

struct STRUCT_USARTx_Fram strEsp8266_Fram_Record = {0};
static char s_bt_line_buf[RX_BUF_MAX_LEN];

static void usart3_send_bytes(const char *buf, u16 len)
{
    u16 i;

    if ((buf == 0) || (len == 0U)) {
        return;
    }

    for (i = 0; i < len; ++i) {
        USART_SendData(USART3, (u8)buf[i]);
        while (USART_GetFlagStatus(USART3, USART_FLAG_TXE) == RESET) {
        }
    }
}

void BT_Config(void)
{
    GPIO_InitTypeDef GPIO_InitStructure;
    USART_InitTypeDef USART_InitStructure;
    NVIC_InitTypeDef NVIC_InitStructure;

    RCC_APB1PeriphClockCmd(RCC_APB1Periph_USART3, ENABLE);
    RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIOB, ENABLE);
    RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIOA, ENABLE);
    RCC_APB2PeriphClockCmd(RCC_APB2Periph_AFIO, ENABLE);
    GPIO_PinRemapConfig(GPIO_Remap_SWJ_JTAGDisable, ENABLE);

    GPIO_InitStructure.GPIO_Pin = GPIO_Pin_4;
    GPIO_InitStructure.GPIO_Mode = GPIO_Mode_Out_PP;
    GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
    GPIO_Init(GPIOA, &GPIO_InitStructure);
    GPIO_ResetBits(GPIOA, GPIO_Pin_4);

    GPIO_InitStructure.GPIO_Pin = GPIO_Pin_15;
    GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IPU;
    GPIO_Init(GPIOA, &GPIO_InitStructure);

    GPIO_InitStructure.GPIO_Pin = GPIO_Pin_10;
    GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
    GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF_PP;
    GPIO_Init(GPIOB, &GPIO_InitStructure);

    GPIO_InitStructure.GPIO_Pin = GPIO_Pin_11;
    GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IN_FLOATING;
    GPIO_Init(GPIOB, &GPIO_InitStructure);

    USART_InitStructure.USART_BaudRate = BT_UART_BAUDRATE;
    USART_InitStructure.USART_WordLength = USART_WordLength_8b;
    USART_InitStructure.USART_StopBits = USART_StopBits_1;
    USART_InitStructure.USART_Parity = USART_Parity_No;
    USART_InitStructure.USART_HardwareFlowControl = USART_HardwareFlowControl_None;
    USART_InitStructure.USART_Mode = USART_Mode_Rx | USART_Mode_Tx;
    USART_Init(USART3, &USART_InitStructure);

    USART_ITConfig(USART3, USART_IT_RXNE, ENABLE);
    USART_ITConfig(USART3, USART_IT_IDLE, ENABLE);
    USART_Cmd(USART3, ENABLE);

    NVIC_InitStructure.NVIC_IRQChannel = USART3_IRQn;
    NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0;
    NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0;
    NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;
    NVIC_Init(&NVIC_InitStructure);

    strEsp8266_Fram_Record.InfBit.FramLength = 0;
    strEsp8266_Fram_Record.InfBit.FramFinishFlag = 0;
    memset(strEsp8266_Fram_Record.Data_RX_BUF, 0, sizeof(strEsp8266_Fram_Record.Data_RX_BUF));
}

void WiFi_Config(void)
{
    BT_Config();
}

bool BT_IsConnected(void)
{
    return GPIO_ReadInputDataBit(GPIOA, GPIO_Pin_15) == Bit_SET;
}

void BT_SendRaw(const char *buf, u16 len)
{
    usart3_send_bytes(buf, len);
}

void BT_SendLine(const char *line)
{
    static const char crlf[] = "\r\n";

    if (line == 0) {
        return;
    }

    usart3_send_bytes(line, (u16)strlen(line));
    usart3_send_bytes(crlf, (u16)(sizeof(crlf) - 1U));
}

char *BT_TryReceiveLine(void)
{
    u16 rx_len;
    u16 start = 0;

    if (!strEsp8266_Fram_Record.InfBit.FramFinishFlag) {
        return 0;
    }

    rx_len = strEsp8266_Fram_Record.InfBit.FramLength;
    if (rx_len >= (sizeof(s_bt_line_buf) - 1U)) {
        rx_len = (u16)(sizeof(s_bt_line_buf) - 1U);
    }

    memcpy(s_bt_line_buf, strEsp8266_Fram_Record.Data_RX_BUF, rx_len);
    s_bt_line_buf[rx_len] = '\0';

    strEsp8266_Fram_Record.InfBit.FramLength = 0;
    strEsp8266_Fram_Record.InfBit.FramFinishFlag = 0;
    memset(strEsp8266_Fram_Record.Data_RX_BUF, 0, sizeof(strEsp8266_Fram_Record.Data_RX_BUF));

    while (rx_len > 0U) {
        char c = s_bt_line_buf[rx_len - 1U];
        if ((c != '\r') && (c != '\n') && (c != '\0')) {
            break;
        }
        s_bt_line_buf[--rx_len] = '\0';
    }

    while ((s_bt_line_buf[start] == '\r') || (s_bt_line_buf[start] == '\n')) {
        start++;
    }

    if (start > 0U) {
        memmove(s_bt_line_buf, s_bt_line_buf + start, strlen(s_bt_line_buf + start) + 1U);
    }

    if (s_bt_line_buf[0] == '\0') {
        return 0;
    }

    return s_bt_line_buf;
}

void USART3_IRQHandler(void)
{
    char ch;

    if (USART_GetITStatus(USART3, USART_IT_RXNE) != RESET) {
        ch = USART_ReceiveData(USART3);
        if ((strEsp8266_Fram_Record.InfBit.FramFinishFlag == 0U) &&
            (strEsp8266_Fram_Record.InfBit.FramLength < (RX_BUF_MAX_LEN - 1U))) {
            strEsp8266_Fram_Record.Data_RX_BUF[strEsp8266_Fram_Record.InfBit.FramLength++] = ch;
            if (ch == '\n') {
                strEsp8266_Fram_Record.InfBit.FramFinishFlag = 1U;
            }
        }
    }

    if (USART_GetITStatus(USART3, USART_IT_IDLE) == SET) {
        if (strEsp8266_Fram_Record.InfBit.FramLength > 0U) {
            strEsp8266_Fram_Record.InfBit.FramFinishFlag = 1U;
        }
        ch = USART_ReceiveData(USART3);
        (void)ch;
    }
}

static char *itoa_local(int value, char *string, int radix)
{
    int i;
    int d;
    int flag = 0;
    char *ptr = string;

    if (radix != 10) {
        *ptr = 0;
        return string;
    }

    if (!value) {
        *ptr++ = 0x30;
        *ptr = 0;
        return string;
    }

    if (value < 0) {
        *ptr++ = '-';
        value *= -1;
    }

    for (i = 10000; i > 0; i /= 10) {
        d = value / i;
        if (d || flag) {
            *ptr++ = (char)(d + 0x30);
            value -= (d * i);
            flag = 1;
        }
    }

    *ptr = 0;
    return string;
}

void USART3_printf(USART_TypeDef *USARTx, char *Data, ...)
{
    const char *s;
    int d;
    char buf[16];
    va_list ap;

    va_start(ap, Data);

    while (*Data != 0) {
        if (*Data == 0x5c) {
            switch (*++Data) {
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
        } else if (*Data == '%') {
            switch (*++Data) {
            case 's':
                s = va_arg(ap, const char *);
                for (; *s; s++) {
                    USART_SendData(USARTx, *s);
                    while (USART_GetFlagStatus(USARTx, USART_FLAG_TXE) == RESET) {
                    }
                }
                Data++;
                break;
            case 'd':
                d = va_arg(ap, int);
                itoa_local(d, buf, 10);
                for (s = buf; *s; s++) {
                    USART_SendData(USARTx, *s);
                    while (USART_GetFlagStatus(USARTx, USART_FLAG_TXE) == RESET) {
                    }
                }
                Data++;
                break;
            default:
                Data++;
                break;
            }
        } else {
            USART_SendData(USARTx, *Data++);
        }

        while (USART_GetFlagStatus(USARTx, USART_FLAG_TXE) == RESET) {
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

    strEsp8266_Fram_Record.InfBit.FramLength = 0;
    strEsp8266_Fram_Record.InfBit.FramFinishFlag = 0;
    memset(strEsp8266_Fram_Record.Data_RX_BUF, 0, sizeof(strEsp8266_Fram_Record.Data_RX_BUF));
}
