#include "system.h"
#include "SysTick.h"
#include "led.h"
#include "usart.h"
#include "adc_temp.h"
#include "wifi_config.h"
#include "wifi_function.h"
#include "board_adapter.h"
#include "mqtt_service.h"
#include "iot_config.h"

int main(void)
{
    SysTick_Init(72U);
    NVIC_PriorityGroupConfig(NVIC_PriorityGroup_2);
    USART1_Init(DEBUG_USART_BAUD);
    LED_Init();
    WiFi_Config();
    BoardAdapter_Init();

    printf("\r\n====================================\r\n");
    printf("STM32F103 + ESP8266 + iBOOT MQTT\r\n");
    printf("MQTT broker: %s:%u\r\n", MQTT_HOST, (unsigned int)MQTT_PORT);
    printf("Device: %s / %s\r\n", IBOOT_PRODUCT_CODE, IBOOT_DEVICE_SN);
    printf("ESP8266 UART: %s\r\n", ESP8266_UART_DESC);
    printf("ESP8266 CTRL: %s\r\n", ESP8266_CTRL_DESC);
    printf("Sensors: internal temp + light\r\n");
    printf("Light PIN: %s\r\n", LIGHT_SENSOR_PIN_DESC);
    printf("====================================\r\n");

    MQTT_Service_Init();

    while (1)
    {
        BoardAdapter_Process();
        ESP8266_Process();
        MQTT_Service_Task();
        delay_ms(MAIN_LOOP_DELAY_MS);
    }
}
