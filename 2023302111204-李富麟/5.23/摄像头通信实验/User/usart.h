#ifndef _USART_H
#define _USART_H

#include "system.h"
#include "stdio.h"

#define UART_PORT_NONE     0U
#define UART_PORT_USART1   1U
#define UART_PORT_USART3   3U

void USART1_Init(u32 bound);
void USART3_Init(u32 bound);
void USART_All_Init(u32 bound);
void USART1_SendByte(u8 data);
void USART3_SendByte(u8 data);
void USART1_SendBuffer(const u8 *data, u32 len);
void USART3_SendBuffer(const u8 *data, u32 len);
void USART1_SendString(const char *str);
void USART3_SendString(const char *str);
void USART_SendByteOn(u8 uart_port, u8 data);
void USART_SendBufferOn(u8 uart_port, const u8 *data, u32 len);
void USART_SendStringOn(u8 uart_port, const char *str);
void USART_SendStringAll(const char *str);
u8 USART1_ReadByte(void);
u8 USART1_ReadByteTimeout(u8 *data, u32 timeout_ms);
u8 USART3_ReadByteTimeout(u8 *data, u32 timeout_ms);
u8 USART_ReadByteTimeoutOn(u8 uart_port, u8 *data, u32 timeout_ms);
u8 USART_ReadByteAnyTimeout(u8 *data, u32 timeout_ms, u8 *uart_port);

#endif
