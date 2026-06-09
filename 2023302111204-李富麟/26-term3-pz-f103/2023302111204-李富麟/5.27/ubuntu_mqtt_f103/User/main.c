#include "system.h"
#include "SysTick.h"
#include "led.h"
#include "usart.h"
#include "wifi_config.h"
#include "wifi_function.h"
#include "mqtt_app.h"
#include <string.h>


void (*pNet_Test)(void);

int main()
{
	SysTick_Init(72);
	NVIC_PriorityGroupConfig(NVIC_PriorityGroup_2);  //中断优先级分组 分2组
	USART1_Init(115200); //初始化串口1,波特率115200
	LED_Init();
	WiFi_Config();
	printf("Hello, world!\r\n");
    printf("\r\n====================================\r\n");
    printf("STM32 + ESP8266 + WSL MQTT Demo\r\n");
    printf("FW_TAG: MQTT_WSL_20260527\r\n");
    printf("====================================\r\n");

	MQTT_App_Init();

	while(1){
		MQTT_App_Loop();
	}
}

