#include "mqtt_app.h"
#include "sensor_temp_hum.h"
#include "wifi_function.h"
#include "esp8266_mqtt_config.h"
#include "SysTick.h"
#include "dht11.h"
#include "device_control.h"
#include <stdio.h>
#include <string.h>

static u32 s_publish_seq = 0;

bool MQTT_App_PublishAck(const char *payload)
{
    if ((payload == 0) || (*payload == '\0')) {
        return false;
    }

    if (ESP8266_Set_MQTT_Public(MQTT_TOPIC_ACK, payload)) {
        printf("[MQTT] ack OK: %s\r\n", payload);
        return true;
    }

    printf("[MQTT] ack failed: %s\r\n", payload);
    return false;
}

static void mqtt_publish_sensor_data(void)
{
    char payload[220];
    sensor_temp_hum_data_t data;
#if SENSOR_ENABLE_DHT11
    dht11_diag_t dht_diag;
#endif
    int n;

    if (!Sensor_TempHum_Read(&data)) {
        printf("[SENSOR] read failed\r\n");
        Device_Control_UpdateSensor(0);
        return;
    }

    Device_Control_UpdateSensor(&data);

    if (data.temperature_valid) {
        if (data.humidity_valid) {
            n = sprintf(payload,
                        "T=%.2f;H=%.2f;L=%s;DEV=puzhong-f103;TS=%lu",
                        data.temperature_c, data.humidity_rh,
                        Device_Control_GetSnapshot()->led_on ? "ON" : "OFF",
                        (unsigned long)s_publish_seq);
        } else {
            n = sprintf(payload,
                        "T=%.2f;H=null;L=%s;DEV=puzhong-f103;TS=%lu",
                        data.temperature_c,
                        Device_Control_GetSnapshot()->led_on ? "ON" : "OFF",
                        (unsigned long)s_publish_seq);
        }
    } else {
        n = sprintf(payload,
                    "T=null;H=null;L=%s;DEV=puzhong-f103;TS=%lu",
                    Device_Control_GetSnapshot()->led_on ? "ON" : "OFF",
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

    if (ESP8266_Set_MQTT_Public(MQTT_TOPIC_DATA, payload)) {
        printf("[MQTT] publish OK: %s\r\n", MQTT_TOPIC_DATA);
    } else {
        printf("[MQTT] publish failed: %s\r\n", MQTT_TOPIC_DATA);
    }

    s_publish_seq++;
}

static void mqtt_handle_cmd_message(const char *payload)
{
    char ack[220];
    bool recognized;

    if (payload == 0) {
        return;
    }

    recognized = Device_Control_HandleCommand(payload, ack, (u16)sizeof(ack));
    if (ack[0] != '\0') {
        MQTT_App_PublishAck(ack);
    }

    printf("[MQTT] recv cmd: %s (%s)\r\n", payload, recognized ? "handled" : "unknown");
}

void MQTT_App_Init(void)
{
    printf("[SENSOR] init start\r\n");
    Sensor_TempHum_Init();
    printf("[SENSOR] init done\r\n");
    printf("[MQTT] init start\r\n");
    Device_Control_SetMqttConnected(ESP8266_STA_TCP_Client_MQTT());
    printf("[MQTT_APP] init done\r\n");
}

void MQTT_App_Loop(void)
{
    const device_snapshot_t *snapshot;
    static u32 reconnect_tick_ms = 0;
    static u32 tick_ms = 0;
    char *rx;
    char ack[220];
    char payload[128];

    if (Device_Control_HandleLocalInput()) {
        if (Device_Control_BuildStatePayload(ack, (u16)sizeof(ack))) {
            MQTT_App_PublishAck(ack);
        }
    }

    snapshot = Device_Control_GetSnapshot();

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
            Device_Control_SetMqttConnected(false);
            if (ESP8266_STA_TCP_Client_MQTT()) {
                Device_Control_SetMqttConnected(true);
                if (Device_Control_BuildStatePayload(ack, (u16)sizeof(ack))) {
                    MQTT_App_PublishAck(ack);
                }
            }
            reconnect_tick_ms = 0;
        }
    }

    Device_Control_RefreshDisplay();

    delay_ms(10);
    tick_ms += 10;
    reconnect_tick_ms += 10;

    if ((!snapshot->mqtt_connected) && (reconnect_tick_ms >= 5000U)) {
        reconnect_tick_ms = 0;
        printf("[MQTT] offline, auto reconnect...\r\n");
        if (ESP8266_STA_TCP_Client_MQTT()) {
            Device_Control_SetMqttConnected(true);
            if (Device_Control_BuildStatePayload(ack, (u16)sizeof(ack))) {
                MQTT_App_PublishAck(ack);
            }
        }
    }

    if (tick_ms >= MQTT_PUBLISH_PERIOD_MS) {
        tick_ms = 0;
        mqtt_publish_sensor_data();
    }
}
