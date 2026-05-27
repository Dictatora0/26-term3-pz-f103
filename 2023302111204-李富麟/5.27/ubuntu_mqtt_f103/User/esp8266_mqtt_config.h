#ifndef __ESP8266_MQTT_CONFIG_H
#define __ESP8266_MQTT_CONFIG_H

/*
 * Centralized lab configuration for:
 * phone MQTT app -> Windows LAN IP:1883 -> WSL Mosquitto -> ESP8266 -> STM32F103
 *
 * Keep real WiFi credentials out of version control.
 * Modify ONLY this file when WiFi/Broker/topic changes.
 */

#define WIFI_SSID                 "DESKTOP-6NM70T"
#define WIFI_PASSWORD             "LFL-lab-204"

/*
 * Broker must be the Windows host LAN IP that forwards TCP 1883 to WSL Mosquitto.
 * Never use 127.0.0.1 and do not use the private WSL2 NAT IP here.
 */
#define MQTT_HOST                 "192.168.137.1"
#define MQTT_PORT                 1883
#define MQTT_STR_HELPER(x) #x
#define MQTT_STR(x)               MQTT_STR_HELPER(x)
#define MQTT_PORT_STR             MQTT_STR(MQTT_PORT)

#define MQTT_USERNAME             ""
#define MQTT_PASSWORD             ""
#define MQTT_CLIENT_ID            "pz103_f103_esp8266"
#define MQTT_DEVICE_NAME          "pz103-f103"
#define MQTT_KEEPALIVE_SECONDS    120
#define MQTT_SUB_QOS              0

#define MQTT_TOPIC_TELEMETRY      "pz103/telemetry"
#define MQTT_TOPIC_CONTROL        "pz103/control"
#define MQTT_TOPIC_STATUS         "pz103/status"

/* Backward-compatible aliases used by the current source tree. */
#define MQTT_TOPIC_DATA           MQTT_TOPIC_TELEMETRY
#define MQTT_TOPIC_CMD            MQTT_TOPIC_CONTROL
#define MQTT_TOPIC_ACK            MQTT_TOPIC_STATUS

/* publish period in ms */
#define MQTT_PUBLISH_PERIOD_MS    2000

/* Legacy raw TCP demo helpers (not used by the MQTT main flow). */
#define TCP_DEMO_SERVER_HOST      MQTT_HOST
#define TCP_DEMO_SERVER_PORT      MQTT_PORT_STR

/* 0: temperature-only mode (ignore DHT11), 1: enable DHT11 humidity */
#define SENSOR_ENABLE_DHT11       0

/* DHT11 pin config (default: PA8) */
#define DHT11_GPIO_PORT           GPIOA
#define DHT11_GPIO_PIN            GPIO_Pin_8
#define DHT11_GPIO_RCC            RCC_APB2Periph_GPIOA

/* DHT11 auto reset on consecutive read failures */
#define DHT11_AUTO_RESET_FAIL_THRESHOLD 5U

#endif
