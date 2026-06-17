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
#include "lcd_status.h"
#include "gateway_uart.h"
#include <string.h>

#if PYTHON_GATEWAY_MODE
static u8 GatewayCommand_Handle(const char *cmd)
{
    if (strcmp(cmd, "LED_ON") == 0)
    {
        BoardAdapter_SetLed(1U);
        printf("OK:LED_ON\r\n");
        BoardAdapter_PrintStatusLine();
        return 1U;
    }
    else if (strcmp(cmd, "LED_OFF") == 0)
    {
        BoardAdapter_SetLed(0U);
        printf("OK:LED_OFF\r\n");
        BoardAdapter_PrintStatusLine();
        return 1U;
    }
    else if (strcmp(cmd, "BUZZER_ON") == 0)
    {
        BoardAdapter_SetBuzzer(1U);
        printf("OK:BUZZER_ON\r\n");
        BoardAdapter_PrintStatusLine();
        return 1U;
    }
    else if (strcmp(cmd, "BUZZER_OFF") == 0)
    {
        BoardAdapter_SetBuzzer(0U);
        printf("OK:BUZZER_OFF\r\n");
        BoardAdapter_PrintStatusLine();
        return 1U;
    }
    else
    {
        printf("ERR:UNKNOWN_CMD,%s\r\n", cmd);
        return 0U;
    }
}

static void GatewayCommand_Run(const char *source, const char *cmd)
{
    u8 accepted;

    printf("[UART-CMD][%s] %s\r\n", source, cmd);
    accepted = GatewayCommand_Handle(cmd);
    LCD_Status_SetLastCommand(cmd, accepted);
}

static void GatewayCommand_ProcessUsart1(void)
{
    char cmd[USART1_REC_LEN];
    u16 len;
#if UART_RX_DEBUG_LOG
    static u32 last_rx_debug_count = 0U;
    u32 rx_debug_count;
#endif

    USART1_PollReceive();

#if UART_RX_DEBUG_LOG
    rx_debug_count = USART1_GetRxDebugCount();
    if (rx_debug_count != last_rx_debug_count)
    {
        last_rx_debug_count = rx_debug_count;
        printf("[UART-RX] count=%lu last=0x%02X\r\n",
               (unsigned long)rx_debug_count,
               (unsigned int)USART1_GetRxDebugLastByte());
    }
#endif

    if ((USART1_RX_STA & USART1_RX_DONE_FLAG) == 0U)
    {
        return;
    }

    len = USART1_RX_STA & USART1_RX_LEN_MASK;
    if (len >= USART1_REC_LEN)
    {
        len = USART1_REC_LEN - 1U;
    }

    INTX_DISABLE();
    memcpy(cmd, USART1_RX_BUF, len);
    cmd[len] = '\0';
    USART1_RX_STA = 0U;
    INTX_ENABLE();

    if (len == 0U)
    {
        return;
    }

    GatewayCommand_Run("USART1", cmd);
}

static void GatewayCommand_ProcessUsart3(void)
{
    char cmd[GATEWAY_UART_REC_LEN];
#if UART_RX_DEBUG_LOG
    static u32 last_rx_debug_count = 0U;
    u32 rx_debug_count;
#endif

    GatewayUart_PollReceive();

#if UART_RX_DEBUG_LOG
    rx_debug_count = GatewayUart_GetRxDebugCount();
    if (rx_debug_count != last_rx_debug_count)
    {
        last_rx_debug_count = rx_debug_count;
        printf("[UART3-RX] count=%lu last=0x%02X\r\n",
               (unsigned long)rx_debug_count,
               (unsigned int)GatewayUart_GetRxDebugLastByte());
    }
#endif

    if (GatewayUart_GetCommand(cmd, sizeof(cmd)) == 0U)
    {
        return;
    }

    GatewayCommand_Run("USART3", cmd);
}

static void GatewayCommand_Process(void)
{
    GatewayCommand_ProcessUsart1();
    GatewayCommand_ProcessUsart3();
}
#endif

int main(void)
{
    SysTick_Init(72U);
    NVIC_PriorityGroupConfig(NVIC_PriorityGroup_2);
    USART1_Init(DEBUG_USART_BAUD);
#if PYTHON_GATEWAY_MODE
    GatewayUart_Init(DEBUG_USART_BAUD);
#endif
    LED_Init();
#if PYTHON_GATEWAY_MODE == 0U
    WiFi_Config();
#endif
    BoardAdapter_Init();
    LCD_Status_Init();

    printf("\r\n====================================\r\n");
#if PYTHON_GATEWAY_MODE
    printf("STM32F103 UART + Python MQTT Gateway\r\n");
    printf("FW: uart1-uplink-usart3-control\r\n");
    printf("UART protocol: TEMP/LIGHT/LED/BUZZER status + LED/BUZZER commands\r\n");
    printf("USART1 debug/status baud: %u\r\n", (unsigned int)DEBUG_USART_BAUD);
    printf("USART3 command RX: %s baud=%u\r\n", ESP8266_UART_DESC, (unsigned int)DEBUG_USART_BAUD);
    printf("Light PIN: %s\r\n", LIGHT_SENSOR_PIN_DESC);
    printf("Buzzer PIN: %s\r\n", BUZZER_PIN_DESC);
#else
    printf("STM32F103 + ESP8266 + iBOOT MQTT\r\n");
    printf("MQTT broker: %s:%u\r\n", MQTT_HOST, (unsigned int)MQTT_PORT);
    printf("Device: %s / %s\r\n", IBOOT_PRODUCT_CODE, IBOOT_DEVICE_SN);
    printf("ESP8266 UART: %s\r\n", ESP8266_UART_DESC);
    printf("ESP8266 CTRL: %s\r\n", ESP8266_CTRL_DESC);
    printf("Sensors: internal temp + light\r\n");
    printf("Light PIN: %s\r\n", LIGHT_SENSOR_PIN_DESC);
#endif
    printf("====================================\r\n");

#if PYTHON_GATEWAY_MODE == 0U
    MQTT_Service_Init();
#endif

    while (1)
    {
        BoardAdapter_Process();
        LCD_Status_Process();
#if PYTHON_GATEWAY_MODE
        GatewayCommand_Process();
#else
        ESP8266_Process();
        MQTT_Service_Task();
#endif
        delay_ms(MAIN_LOOP_DELAY_MS);
    }
}
