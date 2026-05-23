#include "system.h"
#include "SysTick.h"
#include "led.h"
#include "usart.h"
#include "fan.h"
#include <stdio.h>

#define FAN_BAUDRATE        115200UL
#define FAN_START_DUTY      700U
#define FAN_STEP_DELAY_MS   1500U

static void Board_Init(void)
{
	SysTick_Init(72);
	NVIC_PriorityGroupConfig(NVIC_PriorityGroup_2);
	USART1_Init(FAN_BAUDRATE);
	LED_Init();
	FAN_Init();
}

static void Fan_Ramp_Demo(void)
{
	static const u16 fan_duty_table[] = {400U, 550U, 700U, 850U, 1000U};
	u8 index;

	for (index = 0; index < (u8)(sizeof(fan_duty_table) / sizeof(fan_duty_table[0])); index++)
	{
		FAN_SetSpeed(fan_duty_table[index]);
		LED1 = !LED1;
		delay_ms(FAN_STEP_DELAY_MS);
	}
}

int main(void)
{
	Board_Init();

	LED1 = 1;
	LED2 = 1;
	FAN_SetSpeed(FAN_START_DUTY);

	printf("\r\nPZ STM32F103 fan motor demo\r\n");
	printf("PWM output: PA6 (TIM3_CH1)\r\n");
	printf("Default duty: %u/1000\r\n", FAN_START_DUTY);

	while (1)
	{
		Fan_Ramp_Demo();
		LED2 = !LED2;
	}
}
