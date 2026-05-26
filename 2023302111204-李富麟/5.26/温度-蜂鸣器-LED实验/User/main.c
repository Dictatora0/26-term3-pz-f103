#include "system.h"
#include "SysTick.h"
#include "led.h"
#include "beep.h"
#include "usart.h"
#include "adc_temp.h"

#define HIGH_TEMP_ALARM_CENTI  5000
#define LOW_TEMP_GREEN_CENTI   5000

/*******************************************************************************
* 函 数 名         : main
* 函数功能         : 主函数
* 输    入         : 无
* 输    出         : 无
*******************************************************************************/
int main()
{
	u16 tick_10ms = 0;
	int temp = 0;
	int abs_temp = 0;
	
	SysTick_Init(72);
	NVIC_PriorityGroupConfig(NVIC_PriorityGroup_2);
	LED_Init();
	BEEP_Init();
	USART1_Init(115200);
	ADC_Temp_Init();

	LED1 = 1;
	LED2 = 1;
	BEEP = 0;
	
	while(1)
	{
		tick_10ms++;
		if(tick_10ms % 20 == 0)
		{
			LED1 = !LED1;
		}
		
		if(tick_10ms % 50 == 0)
		{
			temp = Get_Temperture();
			BEEP = (temp > HIGH_TEMP_ALARM_CENTI) ? 1 : 0;
			LED2 = (temp < LOW_TEMP_GREEN_CENTI) ? 0 : 1;

			if(temp < 0)
			{
				abs_temp = -temp;
				printf("内部温度检测值为：-");
			}
			else
			{
				abs_temp = temp;
				printf("内部温度检测值为：+");
			}
			printf("%.2f°C  高温阈值:%.2f°C  低温阈值:%.2f°C\r\n",
				(float)abs_temp / 100,
				(float)HIGH_TEMP_ALARM_CENTI / 100,
				(float)LOW_TEMP_GREEN_CENTI / 100);
		}
		delay_ms(10);	
	}
}
