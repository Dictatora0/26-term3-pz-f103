#include "system.h"
#include "SysTick.h"
#include "led.h"
#include "usart.h"
#include "wifi_config.h"
#include "wifi_function.h"
#include "adc_temp.h"
#include "tftlcd.h"
#include <stdio.h>
#include <string.h>

static void LCD_ShowTempAndLed(float temp, u8 ledOn, u8 mqttReady)
{
	char line[64];

	LCD_ShowString(10, 10, tftlcd_data.width, 24, 16, (u8 *)"PZ103 MQTT Demo");
	sprintf(line, "Temp: %2.2f C   ", temp);
	LCD_ShowString(10, 40, tftlcd_data.width, 24, 16, (u8 *)line);
	sprintf(line, "LED : %s      ", ledOn ? "ON" : "OFF");
	LCD_ShowString(10, 70, tftlcd_data.width, 24, 16, (u8 *)line);
	sprintf(line, "MQTT: %s      ", mqttReady ? "ONLINE" : "OFFLINE");
	LCD_ShowString(10, 100, tftlcd_data.width, 24, 16, (u8 *)line);
	sprintf(line, "Sub: %s", MQTT_TOPIC_LED_CMD);
	LCD_ShowString(10, 130, tftlcd_data.width, 24, 16, (u8 *)line);
	sprintf(line, "Pub: %s", MQTT_TOPIC_TEMP_PUB);
	LCD_ShowString(10, 160, tftlcd_data.width, 24, 16, (u8 *)line);
}

int main(void)
{
	u16 tick10ms = 0;
	u16 mqttRetryTick = 0;
	int temp100 = 0;
	float temp = 0.0f;
	char tempStr[24];
	u8 ledOn = 0;
	u8 mqttReady = 0;

	SysTick_Init(72);
	NVIC_PriorityGroupConfig(NVIC_PriorityGroup_2);
	USART1_Init(115200);
	LED_Init();
	ADC_Temp_Init();
	TFTLCD_Init();
	LCD_LED = 1;
	WiFi_Config();

	FRONT_COLOR = BLACK;
	BACK_COLOR = WHITE;
	LCD_Clear(WHITE);
	LCD_ShowString(10, 10, tftlcd_data.width, 24, 16, (u8 *)"WiFi/MQTT init...");
	printf("LCD id=0x%04X w=%d h=%d\r\n", tftlcd_data.id, tftlcd_data.width, tftlcd_data.height);

	/* AT quick probe before MQTT init */
	ESP8266_Cmd("AT", "OK", NULL, 400);
	ESP8266_Cmd("AT+GMR", "OK", NULL, 1000);
	mqttReady = ESP8266_MQTT_Init_Default() ? 1 : 0;
	if (!mqttReady)
	{
		LCD_ShowString(10, 40, tftlcd_data.width, 24, 16, (u8 *)"MQTT offline, retry");
	}

	LED1 = 1;
	ledOn = 0;

	while (1)
	{
		tick10ms++;
		mqttRetryTick++;

		if (mqttReady)
		{
			if (ESP8266_MQTT_PollLedCommand(&ledOn))
			{
				LED1 = ledOn ? 0 : 1;
			}
		}
		else
		{
			if (mqttRetryTick >= 500)
			{
				mqttRetryTick = 0;
			}
		}

		if (tick10ms >= 100)
		{
			tick10ms = 0;
			temp100 = Get_Temperture();
			temp = (float)temp100 / 100.0f;
			sprintf(tempStr, "%2.2f", temp);
			if (mqttReady)
			{
				if (!ESP8266_MQTT_PublishTemperature(tempStr))
				{
					mqttReady = 0;
				}
			}
			LCD_ShowTempAndLed(temp, ledOn, mqttReady);
			printf("temp=%s, led=%s, mqtt=%s\r\n", tempStr, ledOn ? "ON" : "OFF", mqttReady ? "ON" : "OFF");
		}

		delay_ms(10);
	}
}




