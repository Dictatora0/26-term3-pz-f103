#include "usart.h"
#include "stm32f10x.h"
#include "stm32f10x_usart.h"
#include "stm32f10x_gpio.h"

u8 USART1_RX_BUF[USART1_REC_LEN];
volatile u16 USART1_RX_STA = 0U;
static volatile u32 s_usart1_rx_debug_count = 0U;
static volatile u8 s_usart1_rx_debug_last_byte = 0U;

static void USART1_ReceiveByte(u8 rx)
{
    u16 len;

    s_usart1_rx_debug_count++;
    s_usart1_rx_debug_last_byte = rx;

    if ((USART1_RX_STA & USART1_RX_DONE_FLAG) != 0U)
    {
        return;
    }

    len = USART1_RX_STA & USART1_RX_LEN_MASK;

    if ((rx == '\r') || (rx == '\n'))
    {
        if (len > 0U)
        {
            USART1_RX_STA = USART1_RX_DONE_FLAG | len;
        }
        return;
    }

    if (len < (USART1_REC_LEN - 1U))
    {
        USART1_RX_BUF[len] = rx;
        USART1_RX_STA = (u16)(len + 1U);
    }
    else
    {
        USART1_RX_STA = 0U;
    }
}

int fputc(int ch, FILE *p)
{
    (void)p;
    USART_SendData(USART1, (u8)ch);
    while (USART_GetFlagStatus(USART1, USART_FLAG_TXE) == RESET)
    {
    }
    return ch;
}

int fgetc(FILE *f)
{
    (void)f;
    while (USART_GetFlagStatus(USART1, USART_FLAG_RXNE) == RESET)
    {
    }

    return (int)USART_ReceiveData(USART1);
}

void USART1_Init(u32 bound)
{
    GPIO_InitTypeDef GPIO_InitStructure;
    USART_InitTypeDef USART_InitStructure;
    NVIC_InitTypeDef NVIC_InitStructure;

    RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIOA, ENABLE);
    RCC_APB2PeriphClockCmd(RCC_APB2Periph_USART1, ENABLE);

    GPIO_InitStructure.GPIO_Pin = GPIO_Pin_9;
    GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
    GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF_PP;
    GPIO_Init(GPIOA, &GPIO_InitStructure);

    GPIO_InitStructure.GPIO_Pin = GPIO_Pin_10;
    GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IPU;
    GPIO_Init(GPIOA, &GPIO_InitStructure);

    USART_InitStructure.USART_BaudRate = bound;
    USART_InitStructure.USART_WordLength = USART_WordLength_8b;
    USART_InitStructure.USART_StopBits = USART_StopBits_1;
    USART_InitStructure.USART_Parity = USART_Parity_No;
    USART_InitStructure.USART_HardwareFlowControl = USART_HardwareFlowControl_None;
    USART_InitStructure.USART_Mode = USART_Mode_Rx | USART_Mode_Tx;
    USART_Init(USART1, &USART_InitStructure);

    USART_Cmd(USART1, ENABLE);
    USART_ClearFlag(USART1, USART_FLAG_TC);
    USART_ITConfig(USART1, USART_IT_RXNE, ENABLE);

    NVIC_InitStructure.NVIC_IRQChannel = USART1_IRQn;
    NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 3;
    NVIC_InitStructure.NVIC_IRQChannelSubPriority = 3;
    NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;
    NVIC_Init(&NVIC_InitStructure);
}

void USART1_PollReceive(void)
{
    while (USART_GetFlagStatus(USART1, USART_FLAG_RXNE) != RESET)
    {
        USART1_ReceiveByte((u8)USART_ReceiveData(USART1));
    }
}

u32 USART1_GetRxDebugCount(void)
{
    return s_usart1_rx_debug_count;
}

u8 USART1_GetRxDebugLastByte(void)
{
    return s_usart1_rx_debug_last_byte;
}

void USART1_IRQHandler(void)
{
    u8 rx;

    if (USART_GetITStatus(USART1, USART_IT_RXNE) != RESET)
    {
        rx = (u8)USART_ReceiveData(USART1);
        USART1_ReceiveByte(rx);
        USART_ClearITPendingBit(USART1, USART_IT_RXNE);
    }
}
