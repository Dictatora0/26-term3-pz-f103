#ifndef _USART_H
#define _USART_H

#include "system.h"
#include "stdio.h"

#define USART1_REC_LEN 200U

extern u8 USART1_RX_BUF[USART1_REC_LEN];
extern u16 USART1_RX_STA;

void USART1_Init(u32 bound);

#endif
