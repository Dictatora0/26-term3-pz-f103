#include "usart.h"
#include "SysTick.h"

int fputc(int ch, FILE *p)
{
	(void)p;
	USART1_SendByte((u8)ch);
	return ch;
}

int fgetc(FILE *f)
{
	(void)f;
	return (int)USART1_ReadByte();
}

void USART1_Init(u32 bound)
{
	GPIO_InitTypeDef gpio_init;
	USART_InitTypeDef usart_init;

	RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIOA | RCC_APB2Periph_USART1, ENABLE);

	gpio_init.GPIO_Pin = GPIO_Pin_9;
	gpio_init.GPIO_Speed = GPIO_Speed_50MHz;
	gpio_init.GPIO_Mode = GPIO_Mode_AF_PP;
	GPIO_Init(GPIOA, &gpio_init);

	gpio_init.GPIO_Pin = GPIO_Pin_10;
	gpio_init.GPIO_Mode = GPIO_Mode_IN_FLOATING;
	GPIO_Init(GPIOA, &gpio_init);

	usart_init.USART_BaudRate = bound;
	usart_init.USART_WordLength = USART_WordLength_8b;
	usart_init.USART_StopBits = USART_StopBits_1;
	usart_init.USART_Parity = USART_Parity_No;
	usart_init.USART_HardwareFlowControl = USART_HardwareFlowControl_None;
	usart_init.USART_Mode = USART_Mode_Rx | USART_Mode_Tx;
	USART_Init(USART1, &usart_init);

	USART_Cmd(USART1, ENABLE);
}

void USART1_SendByte(u8 data)
{
	while (USART_GetFlagStatus(USART1, USART_FLAG_TXE) == RESET)
	{
	}
	USART_SendData(USART1, data);
}

void USART1_SendBuffer(const u8 *data, u32 len)
{
	while (len--)
	{
		USART1_SendByte(*data++);
	}
}

void USART1_SendString(const char *str)
{
	while (*str != '\0')
	{
		USART1_SendByte((u8)*str++);
	}
}

u8 USART1_ReadByte(void)
{
	while (USART_GetFlagStatus(USART1, USART_FLAG_RXNE) == RESET)
	{
	}
	return (u8)USART_ReceiveData(USART1);
}

u8 USART1_ReadByteTimeout(u8 *data, u32 timeout_ms)
{
	while (timeout_ms--)
	{
		if (USART_GetFlagStatus(USART1, USART_FLAG_RXNE) != RESET)
		{
			*data = (u8)USART_ReceiveData(USART1);
			return 1U;
		}
		delay_ms(1);
	}
	return 0U;
}
