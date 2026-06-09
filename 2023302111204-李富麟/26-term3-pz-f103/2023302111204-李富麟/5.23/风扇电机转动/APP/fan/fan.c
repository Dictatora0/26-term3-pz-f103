#include "fan.h"

static u16 g_fan_duty = 0U;

void FAN_Init(void)
{
	GPIO_InitTypeDef gpio_init;
	TIM_TimeBaseInitTypeDef tim_base_init;
	TIM_OCInitTypeDef tim_oc_init;

	RCC_APB2PeriphClockCmd(FAN_PWM_PORT_RCC | FAN_PWM_REMAP_RCC, ENABLE);
	RCC_APB1PeriphClockCmd(FAN_PWM_TIM_RCC, ENABLE);

	gpio_init.GPIO_Pin = FAN_PWM_PIN;
	gpio_init.GPIO_Mode = GPIO_Mode_AF_PP;
	gpio_init.GPIO_Speed = GPIO_Speed_50MHz;
	GPIO_Init(FAN_PWM_PORT, &gpio_init);

	tim_base_init.TIM_Prescaler = 71U;
	tim_base_init.TIM_CounterMode = TIM_CounterMode_Up;
	tim_base_init.TIM_Period = FAN_PWM_MAX_DUTY - 1U;
	tim_base_init.TIM_ClockDivision = TIM_CKD_DIV1;
	tim_base_init.TIM_RepetitionCounter = 0U;
	TIM_TimeBaseInit(FAN_PWM_TIM, &tim_base_init);

	tim_oc_init.TIM_OCMode = TIM_OCMode_PWM1;
	tim_oc_init.TIM_OutputState = TIM_OutputState_Enable;
	tim_oc_init.TIM_Pulse = 0U;
	tim_oc_init.TIM_OCPolarity = TIM_OCPolarity_High;
	TIM_OC1Init(FAN_PWM_TIM, &tim_oc_init);
	TIM_OC1PreloadConfig(FAN_PWM_TIM, TIM_OCPreload_Enable);

	TIM_ARRPreloadConfig(FAN_PWM_TIM, ENABLE);
	TIM_Cmd(FAN_PWM_TIM, ENABLE);

	FAN_Stop();
}

void FAN_SetSpeed(u16 duty)
{
	if (duty > FAN_PWM_MAX_DUTY)
	{
		duty = FAN_PWM_MAX_DUTY;
	}

	g_fan_duty = duty;
	TIM_SetCompare1(FAN_PWM_TIM, duty);
}

void FAN_Start(void)
{
	if (g_fan_duty == 0U)
	{
		FAN_SetSpeed(700U);
	}
}

void FAN_Stop(void)
{
	FAN_SetSpeed(0U);
}
