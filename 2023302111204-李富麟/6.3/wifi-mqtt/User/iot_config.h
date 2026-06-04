#ifndef __IOT_CONFIG_H
#define __IOT_CONFIG_H

/*
 * Wi-Fi and MQTT settings for the local hotspot + local broker test setup.
 */
#define DEBUG_USART_BAUD            115200U
#define MAIN_LOOP_DELAY_MS          10U

#define WIFI_SSID                   "DESKTOP-6NM70T"
#define WIFI_PASSWORD               "LFL-lab-204"

#define MQTT_HOST                   "192.168.137.1"
#define MQTT_PORT                   1883U
#define MQTT_USERNAME               ""
#define MQTT_PASSWORD               ""
#define MQTT_CLIENT_ID              "env_led_node_board001"
#define MQTT_KEEPALIVE_SECONDS      60U
#define MQTT_SUB_QOS                0U

#define IBOOT_PRODUCT_CODE          "env_led_node"
#define IBOOT_DEVICE_SN             "board001"
#define MQTT_PUBLISH_TOPIC          IBOOT_PRODUCT_CODE "/" IBOOT_DEVICE_SN "/up"
#define MQTT_SUBSCRIBE_TOPIC        IBOOT_PRODUCT_CODE "/" IBOOT_DEVICE_SN "/down"

#define MQTT_REPORT_PERIOD_MS       5000U
#define SENSOR_SAMPLE_PERIOD_MS     2000U
#define MQTT_RECONNECT_INTERVAL_MS  3000U

#define ESP8266_AT_TIMEOUT_MS       600U
#define ESP8266_JOIN_TIMEOUT_MS     12000U
#define ESP8266_MQTT_CONN_TIMEOUT_MS 10000U

#define SENSOR_USE_DHT11            1U
#define DHT11_GPIO_PORT             GPIOA
#define DHT11_GPIO_PIN              GPIO_Pin_8
#define DHT11_GPIO_RCC              RCC_APB2Periph_GPIOA
#define DHT11_AUTO_RESET_FAIL_THRESHOLD 5U

#endif
