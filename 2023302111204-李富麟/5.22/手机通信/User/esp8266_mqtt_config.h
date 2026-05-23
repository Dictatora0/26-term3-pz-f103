#ifndef __ESP8266_MQTT_CONFIG_H
#define __ESP8266_MQTT_CONFIG_H

/*
 * Centralized lab configuration:
 * Modify ONLY this file when WiFi/Broker/topic changes.
 */

#define WIFI_SSID        "DESKTOP-6NM70T"
#define WIFI_PASSWORD    "LFL-lab-204"

/* Broker must be LAN reachable IP/domain, never 127.0.0.1 */
#define MQTT_HOST        "192.168.137.1"
#define MQTT_PORT        1883
#define MQTT_STR_HELPER(x) #x
#define MQTT_STR(x) MQTT_STR_HELPER(x)
#define MQTT_PORT_STR    MQTT_STR(MQTT_PORT)

#define MQTT_CLIENT_ID   "puzhong_f103_esp8266"

#define MQTT_TOPIC_DATA  "puzhong/f103/data"
#define MQTT_TOPIC_CMD   "puzhong/f103/cmd"
#define MQTT_TOPIC_ACK   "puzhong/f103/ack"

/* publish period in ms */
#define MQTT_PUBLISH_PERIOD_MS 1000

/* 0: temperature-only mode (ignore DHT11), 1: enable DHT11 humidity */
#define SENSOR_ENABLE_DHT11 0

/* DHT11 pin config (default: PA8) */
#define DHT11_GPIO_PORT GPIOA
#define DHT11_GPIO_PIN  GPIO_Pin_8
#define DHT11_GPIO_RCC  RCC_APB2Periph_GPIOA

/* DHT11 auto reset on consecutive read failures */
#define DHT11_AUTO_RESET_FAIL_THRESHOLD 5U

#endif
