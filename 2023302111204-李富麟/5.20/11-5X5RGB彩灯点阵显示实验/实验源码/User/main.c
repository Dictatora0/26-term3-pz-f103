#include "system.h"
#include "SysTick.h"
#include "led.h"
#include "usart.h"
#include "ws2812.h"


void RGB_ShowDemo(void)
{
	while(1)
	{
		RGB_LED_Red();
		delay_ms(1000);
		RGB_LED_Green();
		delay_ms(1000);
		RGB_LED_Blue();
		delay_ms(1000);
		break;
	}
	RGB_LED_Clear();
}

/*******************************************************************************
* 函 数 名         : main
* 函数功能		   : 主函数
* 输    入         : 无
* 输    出         : 无
*******************************************************************************/
int main()
{	
	u8 num=0;
	u8 i=0;
	u32 color[]={RGB_COLOR_RED,RGB_COLOR_GREEN,RGB_COLOR_BLUE,RGB_COLOR_WHITE,RGB_COLOR_YELLOW};
	
	SysTick_Init(72);
	NVIC_PriorityGroupConfig(NVIC_PriorityGroup_2);  //中断优先级分组 分2组
	LED_Init();
	USART1_Init(115200);
	RGB_LED_Init();
	RGB_ShowDemo();
	while(1)
	{
		i++;
		if(i==100)
		{
			i=0;
			RGB_ShowCharNum(num%16,color[num%5]);
			num++;
		}
		delay_ms(5);
	}
}
