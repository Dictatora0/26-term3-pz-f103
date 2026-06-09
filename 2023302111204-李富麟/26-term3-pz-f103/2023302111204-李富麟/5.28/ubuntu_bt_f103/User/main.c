#include "system.h"
#include "SysTick.h"
#include "led.h"
#include "usart.h"
#include "wifi_config.h"
#include "mqtt_app.h"
#include "esp8266_mqtt_config.h"
#include <string.h>

void (*pNet_Test)(void);

int main()
{
    SysTick_Init(72);
    NVIC_PriorityGroupConfig(NVIC_PriorityGroup_2);
    USART1_Init(115200);
    LED_Init();
    BT_Config();

    printf("Hello, world!\r\n");
    printf("\r\n====================================\r\n");
    printf("STM32 + HC05 + Ubuntu MQTT Bridge Demo\r\n");
    printf("FW_TAG: %s\r\n", BT_FW_TAG);
    printf("====================================\r\n");

    MQTT_App_Init();

    while (1) {
        MQTT_App_Loop();
    }
}
