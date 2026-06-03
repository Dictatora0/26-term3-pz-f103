#include "system.h"
#include "SysTick.h"
#include "led.h"
#include "usart.h"
#include "wifi_function.h"
#include "bsp_sensor.h"
#include "cloud_service.h"
#include "cloud_config.h"

int main(void)
{
	SysTick_Init(72U);
	NVIC_PriorityGroupConfig(NVIC_PriorityGroup_2);
	USART1_Init(DEBUG_USART_BAUD);
	LED_Init();

	printf("\r\n[BOOT] System initialized\r\n");
	printf("[BOOT] MCU: STM32F103ZE\r\n");
	printf("[BOOT] Debug UART: USART1 @ %lu\r\n", (unsigned long)DEBUG_USART_BAUD);
#if CLOUD_ENABLE
	printf("[BOOT] Mode: cloud\r\n");
#else
	printf("[BOOT] Mode: local\r\n");
#endif

	Sensor_Init();
	CloudService_Init();

	while (1)
	{
		ESP8266_Process();
		Sensor_Process();
		CloudService_Process();
		delay_ms(MAIN_LOOP_DELAY_MS);
	}
}
