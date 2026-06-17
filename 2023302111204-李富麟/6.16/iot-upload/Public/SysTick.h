#ifndef _SYSTICK_H
#define _SYSTICK_H

#include "system.h"

void SysTick_Init(u8 SYSCLK);
void delay_ms(u16 nms);
void delay_us(u32 nus);
u32 SysTick_GetMs(void);
u32 SysTick_GetSeconds(void);
void SysTick_ResetMs(void);

#endif
