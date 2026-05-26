#include "system.h"
#include "SysTick.h"
#include "led.h"
#include "usart.h"
#include "tftlcd.h"
#include "key.h"
#include "touch.h"
#include "time.h"
#include "lvgl.h"
#include "lv_port_disp.h"
#include "lv_port_indev.h"
#include "lv_mbox_test.h"




int main()
{
	SysTick_Init(72);
	NVIC_PriorityGroupConfig(NVIC_PriorityGroup_2);  //中断优先级分组 分2组
	LED_Init();
	USART1_Init(115200);
	TFTLCD_Init();			//LCD初始化
	KEY_Init();
	TP_Init();
	
	TIM4_Init(999,35);
	
	lv_init();	//lvgl系统初始化
	lv_port_disp_init();	//lvgl显示接口初始化,放在lv_init()的后面
	lv_port_indev_init();	//lvgl输入接口初始化,放在lv_init()的后面
	
	lv_mbox_test_start();	//运行例程
	
	while(1)
	{
		tp_dev.scan(0);
		lv_task_handler();
	}
}
