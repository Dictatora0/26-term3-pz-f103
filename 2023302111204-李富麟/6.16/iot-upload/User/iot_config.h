#ifndef __IOT_CONFIG_H
#define __IOT_CONFIG_H

/*
 * Wi-Fi and MQTT settings for the local hotspot + local broker test setup.
 */
#define DEBUG_USART_BAUD            115200U
#define MAIN_LOOP_DELAY_MS          10U
#define UART_RX_DEBUG_LOG           1U

/*
 * Python gateway experiment mode:
 * 1 = F103 uses USART1 for telemetry/debug and USART1/USART3 for commands;
 *     ESP8266 MQTT task is skipped.
 * 0 = keep the previous ESP8266/iBOOT MQTT behavior.
 */
#define PYTHON_GATEWAY_MODE         1U

#if PYTHON_GATEWAY_MODE
#define WIFI_SSID                   ""
#define WIFI_PASSWORD               ""
#define MQTT_HOST                   ""
#define MQTT_PORT                   1883U
#define MQTT_USERNAME               ""
#define MQTT_PASSWORD               ""
#define MQTT_CLIENT_ID              "pz_f103_uart_only"
#define IBOOT_PRODUCT_CODE          "pz103"
#define IBOOT_DEVICE_SN             "f103_01"
#else
#define WIFI_SSID                   "replace_with_wifi_ssid"
#define WIFI_PASSWORD               "replace_with_wifi_password"
#define MQTT_HOST                   "replace_with_mqtt_host"
#define MQTT_PORT                   1883U
#define MQTT_USERNAME               ""
#define MQTT_PASSWORD               ""
#define MQTT_CLIENT_ID              "env_led_node_board001"
#define IBOOT_PRODUCT_CODE          "env_led_node"
#define IBOOT_DEVICE_SN             "board001"
#endif

#define MQTT_KEEPALIVE_SECONDS      60U
#define MQTT_SUB_QOS                0U
#define MQTT_PUBLISH_TOPIC          IBOOT_PRODUCT_CODE "/" IBOOT_DEVICE_SN "/up"
#define MQTT_SUBSCRIBE_TOPIC        IBOOT_PRODUCT_CODE "/" IBOOT_DEVICE_SN "/down"

#define MQTT_REPORT_PERIOD_MS       5000U
#define SENSOR_SAMPLE_PERIOD_MS     2000U
#define MQTT_RECONNECT_INTERVAL_MS  3000U

#define ESP8266_AT_TIMEOUT_MS       600U
#define ESP8266_JOIN_TIMEOUT_MS     20000U
#define ESP8266_MQTT_CONN_TIMEOUT_MS 10000U

#define ESP8266_UART_DESC           "USART3 TX=PB10 RX=PB11"
#define ESP8266_CTRL_DESC           "EN/CH_PD=PA4 RST=PA15"
#define ESP8266_UART_TX_PORT        GPIOB
#define ESP8266_UART_TX_PIN         GPIO_Pin_10
#define ESP8266_UART_RX_PORT        GPIOB
#define ESP8266_UART_RX_PIN         GPIO_Pin_11
#define ESP8266_UART_GPIO_RCC       RCC_APB2Periph_GPIOB
#define ESP8266_CTRL_PORT           GPIOA
#define ESP8266_CTRL_EN_PIN         GPIO_Pin_4
#define ESP8266_CTRL_RST_PIN        GPIO_Pin_15
#define ESP8266_CTRL_GPIO_RCC       RCC_APB2Periph_GPIOA

#define LIGHT_SENSOR_PIN_DESC       "PF8 (ADC3_CH6)"

#define BUZZER_PIN_DESC             "PB8"
#define BUZZER_PORT                 GPIOB
#define BUZZER_PIN                  GPIO_Pin_8
#define BUZZER_GPIO_RCC             RCC_APB2Periph_GPIOB
#define BUZZER_ACTIVE_LEVEL         1U

#endif
