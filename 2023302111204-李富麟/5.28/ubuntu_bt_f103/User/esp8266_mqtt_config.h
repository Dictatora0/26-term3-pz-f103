#ifndef __ESP8266_MQTT_CONFIG_H
#define __ESP8266_MQTT_CONFIG_H

/*
 * Centralized lab configuration for:
 * STM32F103 <-> HC05 Bluetooth UART <-> Ubuntu bridge script <-> Mosquitto
 *
 * The legacy ESP8266 direct-MQTT source still exists in this repository,
 * so the Wi-Fi related macros are kept for build compatibility. The active
 * firmware path for this experiment is the Bluetooth + Ubuntu MQTT bridge.
 */

/* Legacy Wi-Fi settings retained for the older ESP8266 source path. */
#define WIFI_SSID                 "DESKTOP-6NM70T"
#define WIFI_PASSWORD             "LFL-lab-204"

/*
 * Broker settings used by the Ubuntu bridge script.
 * For native Ubuntu on the same machine use 127.0.0.1.
 */
#define MQTT_HOST                 "127.0.0.1"
#define MQTT_PORT                 1883
#define MQTT_STR_HELPER(x) #x
#define MQTT_STR(x)               MQTT_STR_HELPER(x)
#define MQTT_PORT_STR             MQTT_STR(MQTT_PORT)

/* Bluetooth UART path used by the STM32 firmware. */
#define BT_UART_BAUDRATE          9600
#define BT_UART_SCAN_PERIOD_MS    3000U
#define BT_FW_TAG                 "BT_MQTT_20260528"

#define MQTT_USERNAME             ""
#define MQTT_PASSWORD             ""
#define MQTT_CLIENT_ID            "ubuntu_bt_bridge"
#define MQTT_DEVICE_NAME          "pz103-f103"
#define MQTT_KEEPALIVE_SECONDS    120
#define MQTT_SUB_QOS              0

#define MQTT_TOPIC_TELEMETRY      "pz103/telemetry"
#define MQTT_TOPIC_CONTROL        "pz103/control"
#define MQTT_TOPIC_STATUS         "pz103/status"
#define MQTT_TOPIC_TEXT_TX        "pz103/text/tx"
#define MQTT_TOPIC_TEXT_RX        "pz103/text/rx"
#define MQTT_TOPIC_BT_RAW_TX      "pz103/bluetooth/tx"
#define MQTT_TOPIC_BT_RAW_RX      "pz103/bluetooth/rx"

/* Backward-compatible aliases used by the current source tree. */
#define MQTT_TOPIC_DATA           MQTT_TOPIC_TELEMETRY
#define MQTT_TOPIC_CMD            MQTT_TOPIC_CONTROL
#define MQTT_TOPIC_ACK            MQTT_TOPIC_STATUS

/* publish period in ms */
#define MQTT_PUBLISH_PERIOD_MS    2000

/* Legacy raw TCP demo helpers (not used by the Bluetooth main flow). */
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
