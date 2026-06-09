#ifndef __CLOUD_CONFIG_H
#define __CLOUD_CONFIG_H

#define CLOUD_ENABLE                  1
#define LCD_ENABLE                    1

#define DEBUG_USART_BAUD              115200U
#define MAIN_LOOP_DELAY_MS            10U

#define WIFI_SSID                     "DESKTOP-6NM70T"
#define WIFI_PASSWORD                 "LFL-lab-204"

#define MQTT_BROKER_HOST              "YOUR_MQTT_BROKER_HOST"
#define MQTT_BROKER_PORT              1883U
#define MQTT_DEVICE_ID                "device_001"
#define MQTT_CLIENT_ID                MQTT_DEVICE_ID
#define MQTT_USERNAME                 "YOUR_MQTT_USERNAME"
#define MQTT_PASSWORD                 "YOUR_MQTT_PASSWORD"

#define MQTT_TOPIC_TELEMETRY          "devices/device_001/telemetry"
#define MQTT_TOPIC_STATUS             "devices/device_001/status"
#define MQTT_TOPIC_COMMAND            "devices/device_001/command"
#define MQTT_TOPIC_REPLY              "devices/device_001/command_reply"

#define TELEMETRY_INTERVAL_MS         5000U
#define SENSOR_SAMPLE_INTERVAL_MS     5000U
#define CLOUD_RECONNECT_INTERVAL_MS   5000U
#define MQTT_KEEPALIVE_SECONDS        60U
#define MQTT_SUB_QOS                  0U

#define ESP8266_AT_TIMEOUT_MS         600U
#define ESP8266_JOIN_TIMEOUT_MS       8000U
#define ESP8266_MQTT_CONN_TIMEOUT_MS  8000U

#endif
