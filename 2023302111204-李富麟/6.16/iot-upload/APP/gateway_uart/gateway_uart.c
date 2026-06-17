#include "gateway_uart.h"
#include "iot_config.h"
#include "stm32f10x_gpio.h"
#include "stm32f10x_rcc.h"
#include "stm32f10x_usart.h"
#include "misc.h"

#if PYTHON_GATEWAY_MODE

#define GATEWAY_UART_RX_DONE_FLAG  0x8000U
#define GATEWAY_UART_RX_LEN_MASK   0x3FFFU

static u8 s_gateway_uart_rx_buf[GATEWAY_UART_REC_LEN];
static volatile u16 s_gateway_uart_rx_sta = 0U;
static volatile u32 s_gateway_uart_rx_debug_count = 0U;
static volatile u8 s_gateway_uart_rx_debug_last_byte = 0U;

static void GatewayUart_ReceiveByte(u8 rx)
{
    u16 len;

    s_gateway_uart_rx_debug_count++;
    s_gateway_uart_rx_debug_last_byte = rx;

    if ((s_gateway_uart_rx_sta & GATEWAY_UART_RX_DONE_FLAG) != 0U)
    {
        return;
    }

    len = s_gateway_uart_rx_sta & GATEWAY_UART_RX_LEN_MASK;

    if ((rx == '\r') || (rx == '\n'))
    {
        if (len > 0U)
        {
            s_gateway_uart_rx_sta = GATEWAY_UART_RX_DONE_FLAG | len;
        }
        return;
    }

    if (len < (GATEWAY_UART_REC_LEN - 1U))
    {
        s_gateway_uart_rx_buf[len] = rx;
        s_gateway_uart_rx_sta = (u16)(len + 1U);
    }
    else
    {
        s_gateway_uart_rx_sta = 0U;
    }
}

void GatewayUart_Init(u32 bound)
{
    GPIO_InitTypeDef GPIO_InitStructure;
    USART_InitTypeDef USART_InitStructure;
    NVIC_InitTypeDef NVIC_InitStructure;

    RCC_APB1PeriphClockCmd(RCC_APB1Periph_USART3, ENABLE);
    RCC_APB2PeriphClockCmd(ESP8266_UART_GPIO_RCC | RCC_APB2Periph_AFIO, ENABLE);

    GPIO_InitStructure.GPIO_Pin = ESP8266_UART_TX_PIN;
    GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF_PP;
    GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
    GPIO_Init(ESP8266_UART_TX_PORT, &GPIO_InitStructure);

    GPIO_InitStructure.GPIO_Pin = ESP8266_UART_RX_PIN;
    GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IPU;
    GPIO_Init(ESP8266_UART_RX_PORT, &GPIO_InitStructure);

    USART_InitStructure.USART_BaudRate = bound;
    USART_InitStructure.USART_WordLength = USART_WordLength_8b;
    USART_InitStructure.USART_StopBits = USART_StopBits_1;
    USART_InitStructure.USART_Parity = USART_Parity_No;
    USART_InitStructure.USART_HardwareFlowControl = USART_HardwareFlowControl_None;
    USART_InitStructure.USART_Mode = USART_Mode_Rx | USART_Mode_Tx;
    USART_Init(USART3, &USART_InitStructure);

    USART_Cmd(USART3, ENABLE);
    USART_ClearFlag(USART3, USART_FLAG_TC);
    USART_ITConfig(USART3, USART_IT_RXNE, ENABLE);

    NVIC_InitStructure.NVIC_IRQChannel = USART3_IRQn;
    NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 3;
    NVIC_InitStructure.NVIC_IRQChannelSubPriority = 2;
    NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;
    NVIC_Init(&NVIC_InitStructure);
}

void GatewayUart_PollReceive(void)
{
    while (USART_GetFlagStatus(USART3, USART_FLAG_RXNE) != RESET)
    {
        GatewayUart_ReceiveByte((u8)USART_ReceiveData(USART3));
    }
}

u8 GatewayUart_GetCommand(char *out, u16 out_size)
{
    u16 len;
    u16 index;
    u16 sta;

    if ((out == 0) || (out_size == 0U))
    {
        return 0U;
    }

    INTX_DISABLE();
    sta = s_gateway_uart_rx_sta;
    if ((sta & GATEWAY_UART_RX_DONE_FLAG) == 0U)
    {
        INTX_ENABLE();
        return 0U;
    }

    len = sta & GATEWAY_UART_RX_LEN_MASK;
    if (len >= out_size)
    {
        len = (u16)(out_size - 1U);
    }

    for (index = 0U; index < len; index++)
    {
        out[index] = (char)s_gateway_uart_rx_buf[index];
    }
    out[len] = '\0';
    s_gateway_uart_rx_sta = 0U;
    INTX_ENABLE();

    return (len > 0U) ? 1U : 0U;
}

u32 GatewayUart_GetRxDebugCount(void)
{
    return s_gateway_uart_rx_debug_count;
}

u8 GatewayUart_GetRxDebugLastByte(void)
{
    return s_gateway_uart_rx_debug_last_byte;
}

void USART3_IRQHandler(void)
{
    u8 rx;

    if (USART_GetITStatus(USART3, USART_IT_RXNE) != RESET)
    {
        rx = (u8)USART_ReceiveData(USART3);
        GatewayUart_ReceiveByte(rx);
        USART_ClearITPendingBit(USART3, USART_IT_RXNE);
    }
}

#endif
