#include "system.h"
#include "SysTick.h"
#include "led.h"
#include "usart.h"
#include "wifi_config.h"
#include "wifi_function.h"
#include "mqtt_app.h"
#include "device_control.h"
#include <string.h>


void (*pNet_Test)(void);

int main()
{
	SysTick_Init(72);
	NVIC_PriorityGroupConfig(NVIC_PriorityGroup_2);  //中断优先级分组 分2组
	USART1_Init(115200); //初始化串口1,波特率115200
	WiFi_Config();
	Device_Control_Init();
	printf("Hello, world!\r\n");
    printf("\r\n====================================\r\n");
    printf("STM32 + ESP8266 MQTT LED Panel\r\n");
    printf("FW_TAG: MQTT_LCD_KEY_20260525_A\r\n");
    printf("====================================\r\n");

	MQTT_App_Init();

	while(1){
		MQTT_App_Loop();
	}
}

