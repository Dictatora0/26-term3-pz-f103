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
static bool s_mqtt_connected = false;

#define MQTT_INIT_RETRY_MS 5000U

static char mqtt_ascii_upper(char c)
{
    if ((c >= 'a') && (c <= 'z')) {
        return (char)(c - ('a' - 'A'));
    }
    return c;
}

static void mqtt_normalize_payload(const char *src, char *dst, u16 dst_len)
{
    u16 di = 0;
    u16 si = 0;
    char c;

    if ((src == 0) || (dst == 0) || (dst_len < 2U)) {
        return;
    }

    while (((c = src[si++]) != '\0') && (di + 1U < dst_len)) {
        if ((c == ' ') || (c == '\t') || (c == '\r') || (c == '\n')) {
            continue;
        }
        dst[di++] = mqtt_ascii_upper(c);
    }

    dst[di] = '\0';
}

static void mqtt_publish_status(const char *event, const char *result)
{
    char payload[220];
    int n;

    n = sprintf(payload,
                "{\"event\":\"%s\",\"result\":\"%s\",\"device\":\"" MQTT_DEVICE_NAME "\",\"client_id\":\"" MQTT_CLIENT_ID "\"}",
                event != 0 ? event : "status",
                result != 0 ? result : "");
    if ((n <= 0) || (n >= (int)sizeof(payload))) {
        printf("[MQTT] status payload build failed\r\n");
        return;
    }

    printf("[MQTT] publish topic=%s payload=%s\r\n", MQTT_TOPIC_ACK, payload);
    if (ESP8266_Set_MQTT_Public(MQTT_TOPIC_ACK, payload)) {
        printf("[MQTT] status publish OK: %s\r\n", MQTT_TOPIC_ACK);
    } else {
        printf("[MQTT] status publish failed: %s\r\n", MQTT_TOPIC_ACK);
    }
}

static void mqtt_publish_sensor_data(void)
{
    char payload[220];
    sensor_temp_hum_data_t data;
#if SENSOR_ENABLE_DHT11
    dht11_diag_t dht_diag;
#endif
    int n;

    if (!ESP8266_Is_MQTT_Ready()) {
        printf("[MQTT] not ready, skip telemetry publish\r\n");
        return;
    }

    if (!Sensor_TempHum_Read(&data)) {
        printf("[SENSOR] read failed\r\n");
        return;
    }

    if (data.temperature_valid) {
        if (data.humidity_valid) {
            n = sprintf(payload,
                        "{\"device\":\"" MQTT_DEVICE_NAME "\",\"seq\":%lu,\"temperature_c\":%.2f,\"humidity_rh\":%.2f}",
                        (unsigned long)s_publish_seq,
                        data.temperature_c,
                        data.humidity_rh);
        } else {
            n = sprintf(payload,
                        "{\"device\":\"" MQTT_DEVICE_NAME "\",\"seq\":%lu,\"temperature_c\":%.2f}",
                        (unsigned long)s_publish_seq,
                        data.temperature_c);
        }
    } else {
        n = sprintf(payload,
                    "{\"device\":\"" MQTT_DEVICE_NAME "\",\"seq\":%lu,\"temperature_c\":null}",
                    (unsigned long)s_publish_seq);
    }

    if ((n <= 0) || (n >= (int)sizeof(payload))) {
        printf("[MQTT] payload build failed\r\n");
        return;
    }

    printf("[SENSOR] temperature=%s humidity=%s\r\n",
           data.temperature_valid ? "valid" : "null",
           data.humidity_valid ? "valid" : "null");
    if (data.temperature_valid) {
        printf("[SENSOR] temperature_c=%.2f\r\n", data.temperature_c);
    }
    if (data.humidity_valid) {
        printf("[SENSOR] humidity_rh=%.2f\r\n", data.humidity_rh);
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

    printf("[MQTT] publish topic=%s payload=%s\r\n", MQTT_TOPIC_DATA, payload);
    if (ESP8266_Set_MQTT_Public(MQTT_TOPIC_DATA, payload)) {
        printf("[MQTT] publish OK: %s\r\n", MQTT_TOPIC_DATA);
    } else {
        printf("[MQTT] publish failed: %s\r\n", MQTT_TOPIC_DATA);
        s_mqtt_connected = false;
    }

    s_publish_seq++;
}

static void mqtt_handle_cmd_message(const char *topic, const char *payload)
{
    char normalized[160];

    if (payload == 0) {
        return;
    }

    mqtt_normalize_payload(payload, normalized, sizeof(normalized));
    printf("[MQTT] recv topic=%s payload=%s\r\n", topic != 0 ? topic : "<null>", payload);

    if ((strcmp(normalized, "LED_ON") == 0) ||
        (strcmp(normalized, "LED1_ON") == 0) ||
        (strcmp(normalized, "ON") == 0) ||
        (strcmp(normalized, "1") == 0) ||
        (strstr(normalized, "\"LED\":1") != 0) ||
        (strstr(normalized, "\"LED\":TRUE") != 0) ||
        (strstr(normalized, "\"CMD\":\"LED_ON\"") != 0)) {
        LED1 = 0;
        mqtt_publish_status("control", "LED_ON_OK");
        printf("[MQTT] control result: LED ON\r\n");
    } else if ((strcmp(normalized, "LED_OFF") == 0) ||
               (strcmp(normalized, "LED1_OFF") == 0) ||
               (strcmp(normalized, "OFF") == 0) ||
               (strcmp(normalized, "0") == 0) ||
               (strstr(normalized, "\"LED\":0") != 0) ||
               (strstr(normalized, "\"LED\":FALSE") != 0) ||
               (strstr(normalized, "\"CMD\":\"LED_OFF\"") != 0)) {
        LED1 = 1;
        mqtt_publish_status("control", "LED_OFF_OK");
        printf("[MQTT] control result: LED OFF\r\n");
    } else if ((strcmp(normalized, "PING") == 0) ||
               (strstr(normalized, "\"CMD\":\"PING\"") != 0)) {
        mqtt_publish_status("control", "PONG");
        printf("[MQTT] control result: PING -> PONG\r\n");
    } else {
        mqtt_publish_status("control", "CMD_UNKNOWN");
        printf("[MQTT] control result: unknown command\r\n");
    }
}

void MQTT_App_Init(void)
{
    printf("[SENSOR] init start\r\n");
    Sensor_TempHum_Init();
    printf("[SENSOR] init done\r\n");
    printf("[MQTT] broker target=%s:%s\r\n", MQTT_HOST, MQTT_PORT_STR);
    printf("[MQTT] phone/ESP must use the Windows LAN IP forwarded to WSL, not localhost\r\n");
    printf("[MQTT] init start\r\n");
    s_mqtt_connected = ESP8266_STA_TCP_Client_MQTT();
    if (!s_mqtt_connected) {
        printf("[MQTT] init failed, will retry every %lu ms\r\n", (unsigned long)MQTT_INIT_RETRY_MS);
    }
    printf("[MQTT_APP] init done\r\n");
}

void MQTT_App_Loop(void)
{
    static u32 publish_tick_ms = 0;
    static u32 reconnect_tick_ms = 0;
    char *rx;
    char topic[96];
    char payload[160];

    if (!ESP8266_Is_MQTT_Ready() || !s_mqtt_connected) {
        reconnect_tick_ms += 10;
        if (reconnect_tick_ms >= MQTT_INIT_RETRY_MS) {
            reconnect_tick_ms = 0;
            printf("[MQTT] retry init...\r\n");
            s_mqtt_connected = ESP8266_STA_TCP_Client_MQTT();
            if (s_mqtt_connected) {
                printf("[MQTT] retry init OK\r\n");
                publish_tick_ms = 0;
            } else {
                printf("[MQTT] retry init failed\r\n");
            }
        }
        delay_ms(10);
        return;
    }

    reconnect_tick_ms = 0;

    rx = ESP8266_TryReceiveString(DISABLE);
    if (rx != 0) {
        if (strstr(rx, "+MQTTSUBRECV:") != 0) {
            if (ESP8266_MQTT_ParseSubFrame(rx, topic, sizeof(topic), payload, sizeof(payload))) {
                mqtt_handle_cmd_message(topic, payload);
            } else {
                printf("[MQTT] cmd parse failed\r\n");
            }
        } else if (strstr(rx, "+MQTTDISCONNECTED:") != 0) {
            printf("[MQTT] disconnected, reconnecting...\r\n");
            s_mqtt_connected = false;
        }
    }

    delay_ms(10);
    publish_tick_ms += 10;
    if (publish_tick_ms >= MQTT_PUBLISH_PERIOD_MS) {
        publish_tick_ms = 0;
        mqtt_publish_sensor_data();
    }
}
