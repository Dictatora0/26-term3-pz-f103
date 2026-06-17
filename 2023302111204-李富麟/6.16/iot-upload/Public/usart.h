#ifndef _USART_H
#define _USART_H

#include "system.h"
#include "stdio.h"

#define USART1_REC_LEN 200U
#define USART1_RX_DONE_FLAG 0x8000U
#define USART1_RX_LEN_MASK 0x3FFFU

extern u8 USART1_RX_BUF[USART1_REC_LEN];
extern volatile u16 USART1_RX_STA;

void USART1_Init(u32 bound);
void USART1_PollReceive(void);
u32 USART1_GetRxDebugCount(void);
u8 USART1_GetRxDebugLastByte(void);

#endif
