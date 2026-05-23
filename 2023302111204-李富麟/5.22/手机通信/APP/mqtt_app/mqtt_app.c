#include "mqtt_app.h"
#include "sensor_temp_hum.h"
#include "wifi_function.h"
#include "esp8266_mqtt_config.h"
#include "SysTick.h"
#include "led.h"
#include "dht11.h"
#include <stdio.h>
#include <string.h>

static u32 s_publish_seq = 0;

static void json_escape(const char *src, char *dst, u16 dst_len)
{
    u16 di = 0;
    u16 si = 0;
    char c;

    if ((src == 0) || (dst == 0) || (dst_len < 2U)) {
        return;
    }

    while ((c = src[si++]) != '\0') {
        if ((c == '\"') || (c == '\\')) {
            if (di + 2 >= dst_len) {
                break;
            }
            dst[di++] = '\\';
            dst[di++] = c;
        } else if ((u8)c < 0x20U) {
            if (di + 6 >= dst_len) {
                break;
            }
            dst[di++] = '\\';
            dst[di++] = 'u';
            dst[di++] = '0';
            dst[di++] = '0';
            dst[di++] = '0';
            dst[di++] = '0';
        } else {
            if (di + 1 >= dst_len) {
                break;
            }
            dst[di++] = c;
        }
    }
    dst[di] = '\0';
}

static void mqtt_publish_sensor_data(void)
{
    char payload[220];
    char payload_escaped[440];
    sensor_temp_hum_data_t data;
#if SENSOR_ENABLE_DHT11
    dht11_diag_t dht_diag;
#endif
    int n;

    if (!Sensor_TempHum_Read(&data)) {
        printf("[SENSOR] read failed\r\n");
        return;
    }

    if (data.temperature_valid) {
        if (data.humidity_valid) {
            n = sprintf(payload,
                        "{\"temperature\":%.2f,\"humidity\":%.2f,\"light\":null,\"device\":\"puzhong-f103\",\"ts\":%lu}",
                        data.temperature_c, data.humidity_rh, (unsigned long)s_publish_seq);
        } else {
            n = sprintf(payload,
                        "{\"temperature\":%.2f,\"humidity\":null,\"light\":null,\"device\":\"puzhong-f103\",\"ts\":%lu}",
                        data.temperature_c, (unsigned long)s_publish_seq);
        }
    } else {
        n = sprintf(payload,
                    "{\"temperature\":null,\"humidity\":null,\"light\":null,\"device\":\"puzhong-f103\",\"ts\":%lu}",
                    (unsigned long)s_publish_seq);
    }

    if (n <= 0 || n >= (int)sizeof(payload)) {
        printf("[MQTT] payload build failed\r\n");
        return;
    }

    printf("[SENSOR] temperature=%.2f humidity=%s\r\n",
           data.temperature_valid ? data.temperature_c : -999.0f,
           data.humidity_valid ? "valid" : "null");
    if (data.humidity_valid) {
        printf("[SENSOR] humidity=%.2f\r\n", data.humidity_rh);
    } else {
#if SENSOR_ENABLE_DHT11
        printf("[SENSOR] humidity read failed (DHT11)\r\n");
        DHT11_GetDiag(&dht_diag);
        printf("[DHT11] ok=%lu fail=%lu cons_fail=%lu auto_reset=%lu last_err=%d raw=%d,%d,%d,%d,%d\r\n",
               (unsigned long)dht_diag.read_ok_count,
               (unsigned long)dht_diag.read_fail_count,
               (unsigned long)dht_diag.consecutive_fail_count,
               (unsigned long)dht_diag.auto_reset_count,
               dht_diag.last_error,
               dht_diag.last_raw[0], dht_diag.last_raw[1], dht_diag.last_raw[2],
               dht_diag.last_raw[3], dht_diag.last_raw[4]);
#else
        printf("[SENSOR] humidity disabled (SENSOR_ENABLE_DHT11=0)\r\n");
#endif
    }

    json_escape(payload, payload_escaped, sizeof(payload_escaped));
    if (ESP8266_Set_MQTT_Public(MQTT_TOPIC_DATA, payload_escaped)) {
        printf("[MQTT] publish OK: %s\r\n", MQTT_TOPIC_DATA);
    } else {
        printf("[MQTT] publish failed: %s\r\n", MQTT_TOPIC_DATA);
    }

    s_publish_seq++;
}

static void mqtt_handle_cmd_message(const char *payload)
{
    if (payload == 0) {
        return;
    }

    if (strcmp(payload, "LED_ON") == 0) {
        LED1 = 0;
        ESP8266_Set_MQTT_Public(MQTT_TOPIC_ACK, "LED_ON_OK");
        printf("[MQTT] recv cmd: LED_ON\r\n");
    } else if (strcmp(payload, "LED_OFF") == 0) {
        LED1 = 1;
        ESP8266_Set_MQTT_Public(MQTT_TOPIC_ACK, "LED_OFF_OK");
        printf("[MQTT] recv cmd: LED_OFF\r\n");
    } else if (strcmp(payload, "PING") == 0) {
        ESP8266_Set_MQTT_Public(MQTT_TOPIC_ACK, "PONG");
        printf("[MQTT] recv cmd: PING\r\n");
    } else {
        ESP8266_Set_MQTT_Public(MQTT_TOPIC_ACK, "CMD_UNKNOWN");
        printf("[MQTT] recv cmd: unknown (%s)\r\n", payload);
    }
}

void MQTT_App_Init(void)
{
    printf("[SENSOR] init start\r\n");
    Sensor_TempHum_Init();
    printf("[SENSOR] init done\r\n");
    printf("[MQTT] init start\r\n");
    ESP8266_STA_TCP_Client_MQTT();
    printf("[MQTT_APP] init done\r\n");
}

void MQTT_App_Loop(void)
{
    static u32 tick_ms = 0;
    char *rx;
    char payload[128];

    rx = ESP8266_TryReceiveString(DISABLE);
    if (rx != 0) {
        if (strstr(rx, "+MQTTSUBRECV:") != 0) {
            if (ESP8266_MQTT_ExtractPayload(rx, payload, sizeof(payload))) {
                mqtt_handle_cmd_message(payload);
            } else {
                printf("[MQTT] cmd parse failed\r\n");
            }
        } else if (strstr(rx, "+MQTTDISCONNECTED:") != 0) {
            printf("[MQTT] disconnected, reconnecting...\r\n");
            ESP8266_STA_TCP_Client_MQTT();
        }
    }

    delay_ms(10);
    tick_ms += 10;
    if (tick_ms >= MQTT_PUBLISH_PERIOD_MS) {
        tick_ms = 0;
        mqtt_publish_sensor_data();
    }
}
