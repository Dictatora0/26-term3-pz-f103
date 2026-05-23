#ifndef _USART_H
#define _USART_H

#include "system.h"
#include "stdio.h"

void USART1_Init(u32 bound);
void USART1_SendByte(u8 data);
void USART1_SendBuffer(const u8 *data, u32 len);
void USART1_SendString(const char *str);
u8 USART1_ReadByte(void);
u8 USART1_ReadByteTimeout(u8 *data, u32 timeout_ms);

#endif
