#include "usart.h"

#define UART_TIMEOUT_LOOPS_PER_MS  24000UL

static void usart_init_common(USART_TypeDef *usartx, u32 bound)
{
	USART_InitTypeDef usart_init;

	usart_init.USART_BaudRate = bound;
	usart_init.USART_WordLength = USART_WordLength_8b;
	usart_init.USART_StopBits = USART_StopBits_1;
	usart_init.USART_Parity = USART_Parity_No;
	usart_init.USART_HardwareFlowControl = USART_HardwareFlowControl_None;
	usart_init.USART_Mode = USART_Mode_Rx | USART_Mode_Tx;
	USART_Init(usartx, &usart_init);
	USART_Cmd(usartx, ENABLE);
}

static void usart_send_byte(USART_TypeDef *usartx, u8 data)
{
	while (USART_GetFlagStatus(usartx, USART_FLAG_TXE) == RESET)
	{
	}
	USART_SendData(usartx, data);
}

static void usart_send_buffer(USART_TypeDef *usartx, const u8 *data, u32 len)
{
	while (len--)
	{
		usart_send_byte(usartx, *data++);
	}
}

static void usart_send_string(USART_TypeDef *usartx, const char *str)
{
	while (*str != '\0')
	{
		usart_send_byte(usartx, (u8)*str++);
	}
}

static u8 usart_read_byte_timeout(USART_TypeDef *usartx, u8 *data, u32 timeout_loops)
{
	while (timeout_loops--)
	{
		if (USART_GetFlagStatus(usartx, USART_FLAG_RXNE) != RESET)
		{
			*data = (u8)USART_ReceiveData(usartx);
			return 1U;
		}
	}
	return 0U;
}

int fputc(int ch, FILE *p)
{
	(void)p;
	USART1_SendByte((u8)ch);
	USART3_SendByte((u8)ch);
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

	RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIOA | RCC_APB2Periph_USART1, ENABLE);

	gpio_init.GPIO_Pin = GPIO_Pin_9;
	gpio_init.GPIO_Speed = GPIO_Speed_50MHz;
	gpio_init.GPIO_Mode = GPIO_Mode_AF_PP;
	GPIO_Init(GPIOA, &gpio_init);

	gpio_init.GPIO_Pin = GPIO_Pin_10;
	gpio_init.GPIO_Mode = GPIO_Mode_IN_FLOATING;
	GPIO_Init(GPIOA, &gpio_init);

	usart_init_common(USART1, bound);
}

void USART3_Init(u32 bound)
{
	GPIO_InitTypeDef gpio_init;

	RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIOB, ENABLE);
	RCC_APB1PeriphClockCmd(RCC_APB1Periph_USART3, ENABLE);

	gpio_init.GPIO_Pin = GPIO_Pin_10;
	gpio_init.GPIO_Speed = GPIO_Speed_50MHz;
	gpio_init.GPIO_Mode = GPIO_Mode_AF_PP;
	GPIO_Init(GPIOB, &gpio_init);

	gpio_init.GPIO_Pin = GPIO_Pin_11;
	gpio_init.GPIO_Mode = GPIO_Mode_IN_FLOATING;
	GPIO_Init(GPIOB, &gpio_init);

	usart_init_common(USART3, bound);
}

void USART_All_Init(u32 bound)
{
	USART1_Init(bound);
	USART3_Init(bound);
}

void USART1_SendByte(u8 data)
{
	usart_send_byte(USART1, data);
}

void USART3_SendByte(u8 data)
{
	usart_send_byte(USART3, data);
}

void USART1_SendBuffer(const u8 *data, u32 len)
{
	usart_send_buffer(USART1, data, len);
}

void USART3_SendBuffer(const u8 *data, u32 len)
{
	usart_send_buffer(USART3, data, len);
}

void USART1_SendString(const char *str)
{
	usart_send_string(USART1, str);
}

void USART3_SendString(const char *str)
{
	usart_send_string(USART3, str);
}

void USART_SendByteOn(u8 uart_port, u8 data)
{
	if (uart_port == UART_PORT_USART3)
	{
		USART3_SendByte(data);
	}
	else
	{
		USART1_SendByte(data);
	}
}

void USART_SendBufferOn(u8 uart_port, const u8 *data, u32 len)
{
	if (uart_port == UART_PORT_USART3)
	{
		USART3_SendBuffer(data, len);
	}
	else
	{
		USART1_SendBuffer(data, len);
	}
}

void USART_SendStringOn(u8 uart_port, const char *str)
{
	if (uart_port == UART_PORT_USART3)
	{
		USART3_SendString(str);
	}
	else
	{
		USART1_SendString(str);
	}
}

void USART_SendStringAll(const char *str)
{
	USART1_SendString(str);
	USART3_SendString(str);
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
	return usart_read_byte_timeout(USART1, data, timeout_ms * UART_TIMEOUT_LOOPS_PER_MS);
}

u8 USART3_ReadByteTimeout(u8 *data, u32 timeout_ms)
{
	return usart_read_byte_timeout(USART3, data, timeout_ms * UART_TIMEOUT_LOOPS_PER_MS);
}

u8 USART_ReadByteTimeoutOn(u8 uart_port, u8 *data, u32 timeout_ms)
{
	if (uart_port == UART_PORT_USART3)
	{
		return USART3_ReadByteTimeout(data, timeout_ms);
	}
	return USART1_ReadByteTimeout(data, timeout_ms);
}

u8 USART_ReadByteAnyTimeout(u8 *data, u32 timeout_ms, u8 *uart_port)
{
	u32 timeout_loops = timeout_ms * UART_TIMEOUT_LOOPS_PER_MS;

	while (timeout_loops--)
	{
		if (USART_GetFlagStatus(USART1, USART_FLAG_RXNE) != RESET)
		{
			*data = (u8)USART_ReceiveData(USART1);
			*uart_port = UART_PORT_USART1;
			return 1U;
		}
		if (USART_GetFlagStatus(USART3, USART_FLAG_RXNE) != RESET)
		{
			*data = (u8)USART_ReceiveData(USART3);
			*uart_port = UART_PORT_USART3;
			return 1U;
		}
	}

	*uart_port = UART_PORT_NONE;
	return 0U;
}
