#ifndef __FAN_H
#define __FAN_H

#include "system.h"

#define FAN_PWM_PORT            GPIOA
#define FAN_PWM_PIN             GPIO_Pin_6
#define FAN_PWM_PORT_RCC        RCC_APB2Periph_GPIOA
#define FAN_PWM_TIM_RCC         RCC_APB1Periph_TIM3
#define FAN_PWM_REMAP_RCC       RCC_APB2Periph_AFIO
#define FAN_PWM_TIM             TIM3
#define FAN_PWM_MAX_DUTY        1000U

void FAN_Init(void);
void FAN_SetSpeed(u16 duty);
void FAN_Start(void);
void FAN_Stop(void);

#endif
