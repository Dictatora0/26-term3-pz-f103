#include "mqtt_app.h"
#include "sensor_temp_hum.h"
#include "wifi_config.h"
#include "esp8266_mqtt_config.h"
#include "SysTick.h"
#include "led.h"
#include "dht11.h"
#include <stdio.h>
#include <string.h>

static u32 s_publish_seq = 0;
static bool s_bt_connected = false;
static bool s_bt_state_known = false;
static bool s_bt_baud_locked = false;
static u8 s_bt_baud_index = 0;

#define BT_STATE_POLL_MS 200U
#define BT_BAUD_CANDIDATE_COUNT 6U

static const u32 s_bt_baud_candidates[BT_BAUD_CANDIDATE_COUNT] = {
    9600U,
    38400U,
    115200U,
    57600U,
    19200U,
    4800U
};

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

static bool mqtt_starts_with_token(const char *src, const char *token)
{
    u16 i = 0;

    if ((src == 0) || (token == 0)) {
        return false;
    }

    while (token[i] != '\0') {
        if (mqtt_ascii_upper(src[i]) != mqtt_ascii_upper(token[i])) {
            return false;
        }
        i++;
    }

    return true;
}

static u32 bt_current_baud(void)
{
    return s_bt_baud_candidates[s_bt_baud_index];
}

static void bt_apply_current_baud(const char *reason)
{
    WiFi_SetUsart3Baud(bt_current_baud());
    printf("[BT] uart3 baud=%lu %s\r\n",
           (unsigned long)bt_current_baud(),
           reason != 0 ? reason : "");
}

static void bt_send_prefixed_line(const char *prefix, const char *payload)
{
    char line[260];
    int n;

    n = sprintf(line, "%s%s", prefix != 0 ? prefix : "", payload != 0 ? payload : "");
    if ((n <= 0) || (n >= (int)sizeof(line))) {
        printf("[BT] tx line build failed\r\n");
        return;
    }

    BT_SendLine(line);
    printf("[BT] tx %s\r\n", line);
}

static void bt_send_status(const char *event, const char *result)
{
    char payload[220];
    int n;

    n = sprintf(payload,
                "{\"event\":\"%s\",\"result\":\"%s\",\"device\":\"" MQTT_DEVICE_NAME "\",\"link\":\"bluetooth\"}",
                event != 0 ? event : "status",
                result != 0 ? result : "");
    if ((n <= 0) || (n >= (int)sizeof(payload))) {
        printf("[BT] status payload build failed\r\n");
        return;
    }

    bt_send_prefixed_line("STATUS:", payload);
}

static void bt_send_ack(const char *result)
{
    char payload[220];
    int n;

    n = sprintf(payload,
                "{\"result\":\"%s\",\"device\":\"" MQTT_DEVICE_NAME "\"}",
                result != 0 ? result : "");
    if ((n <= 0) || (n >= (int)sizeof(payload))) {
        printf("[BT] ack payload build failed\r\n");
        return;
    }

    bt_send_prefixed_line("ACK:", payload);
}

static void bt_send_hello(void)
{
    char payload[220];
    int n;

    n = sprintf(payload,
                "{\"device\":\"" MQTT_DEVICE_NAME "\",\"fw\":\"" BT_FW_TAG "\",\"text_tx_topic\":\"" MQTT_TOPIC_TEXT_TX "\",\"text_rx_topic\":\"" MQTT_TOPIC_TEXT_RX "\"}");
    if ((n <= 0) || (n >= (int)sizeof(payload))) {
        printf("[BT] hello payload build failed\r\n");
        return;
    }

    bt_send_prefixed_line("HELLO:", payload);
}

static void bt_send_text_message(const char *text)
{
    if ((text == 0) || (text[0] == '\0')) {
        return;
    }

    bt_send_prefixed_line("TEXT:", text);
}

static void bt_send_baud_probe_status(const char *event)
{
    char result[20];

    sprintf(result, "%lu", (unsigned long)bt_current_baud());
    bt_send_status(event, result);
}

static void bt_probe_current_baud(void)
{
    if (!s_bt_connected) {
        return;
    }

    bt_send_hello();
    bt_send_baud_probe_status("baud_probe");
}

static void bt_rotate_baud(void)
{
    if (s_bt_baud_locked) {
        return;
    }

    s_bt_baud_index++;
    if (s_bt_baud_index >= BT_BAUD_CANDIDATE_COUNT) {
        s_bt_baud_index = 0;
    }

    bt_apply_current_baud("scan");
    bt_probe_current_baud();
}

static void bt_lock_current_baud(void)
{
    if (s_bt_baud_locked) {
        return;
    }

    s_bt_baud_locked = true;
    printf("[BT] uart3 baud locked=%lu\r\n", (unsigned long)bt_current_baud());
    bt_send_baud_probe_status("baud_lock");
}

static bool bt_payload_looks_valid(const char *payload)
{
    char normalized[160];

    if ((payload == 0) || (payload[0] == '\0')) {
        return false;
    }

    if (mqtt_starts_with_token(payload, "MSG:") || mqtt_starts_with_token(payload, "TEXT:")) {
        return true;
    }

    mqtt_normalize_payload(payload, normalized, sizeof(normalized));

    if ((strcmp(normalized, "LED_ON") == 0) ||
        (strcmp(normalized, "LED1_ON") == 0) ||
        (strcmp(normalized, "ON") == 0) ||
        (strcmp(normalized, "1") == 0) ||
        (strcmp(normalized, "LED_OFF") == 0) ||
        (strcmp(normalized, "LED1_OFF") == 0) ||
        (strcmp(normalized, "OFF") == 0) ||
        (strcmp(normalized, "0") == 0) ||
        (strcmp(normalized, "PING") == 0) ||
        (strcmp(normalized, "STATUS") == 0)) {
        return true;
    }

    if ((strstr(normalized, "\"CMD\":") != 0) ||
        (strstr(normalized, "\"LED\":") != 0)) {
        return true;
    }

    return false;
}

static void bt_handle_link_state(void)
{
    bool connected = BT_IsConnected();

    if (!s_bt_state_known) {
        s_bt_state_known = true;
        s_bt_connected = connected;
        printf("[BT] link init: %s\r\n", connected ? "connected" : "waiting");
        if (connected) {
            bt_send_hello();
            bt_send_status("link", "connected");
        }
        return;
    }

    if (connected == s_bt_connected) {
        return;
    }

    s_bt_connected = connected;
    printf("[BT] link state change: %s\r\n", connected ? "connected" : "disconnected");

    if (connected) {
        bt_send_hello();
        bt_send_status("link", "connected");
    }
}

static void bt_publish_sensor_data(void)
{
    char payload[220];
    sensor_temp_hum_data_t data;
#if SENSOR_ENABLE_DHT11
    dht11_diag_t dht_diag;
#endif
    int n;

    if (!s_bt_connected) {
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
        printf("[SENSOR] payload build failed\r\n");
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

    bt_send_prefixed_line("TELEMETRY:", payload);
    s_publish_seq++;
}

static void bt_handle_cmd_message(const char *payload)
{
    char normalized[160];
    const char *text_payload = 0;

    if (payload == 0) {
        return;
    }

    mqtt_normalize_payload(payload, normalized, sizeof(normalized));
    printf("[BT] rx payload=%s\r\n", payload);

    if ((strcmp(normalized, "LED_ON") == 0) ||
        (strcmp(normalized, "LED1_ON") == 0) ||
        (strcmp(normalized, "ON") == 0) ||
        (strcmp(normalized, "1") == 0) ||
        (strstr(normalized, "\"LED\":1") != 0) ||
        (strstr(normalized, "\"LED\":TRUE") != 0) ||
        (strstr(normalized, "\"CMD\":\"LED_ON\"") != 0)) {
        LED1 = 0;
        bt_send_ack("LED_ON_OK");
        printf("[BT] control result: LED ON\r\n");
    } else if ((strcmp(normalized, "LED_OFF") == 0) ||
               (strcmp(normalized, "LED1_OFF") == 0) ||
               (strcmp(normalized, "OFF") == 0) ||
               (strcmp(normalized, "0") == 0) ||
               (strstr(normalized, "\"LED\":0") != 0) ||
               (strstr(normalized, "\"LED\":FALSE") != 0) ||
               (strstr(normalized, "\"CMD\":\"LED_OFF\"") != 0)) {
        LED1 = 1;
        bt_send_ack("LED_OFF_OK");
        printf("[BT] control result: LED OFF\r\n");
    } else if ((strcmp(normalized, "PING") == 0) ||
               (strstr(normalized, "\"CMD\":\"PING\"") != 0)) {
        bt_send_ack("PONG");
        printf("[BT] control result: PING -> PONG\r\n");
    } else if ((strcmp(normalized, "STATUS") == 0) ||
               (strstr(normalized, "\"CMD\":\"STATUS\"") != 0)) {
        bt_send_status("status", s_bt_connected ? "connected" : "waiting");
        printf("[BT] control result: STATUS\r\n");
    } else {
        if (mqtt_starts_with_token(payload, "MSG:") || mqtt_starts_with_token(payload, "TEXT:")) {
            text_payload = strchr(payload, ':');
            if (text_payload != 0) {
                text_payload++;
            }
        }

        if ((text_payload == 0) || (*text_payload == '\0')) {
            text_payload = payload;
        }

        bt_send_text_message(text_payload);
        bt_send_ack("TEXT_RX");
        printf("[BT] text rx=%s\r\n", text_payload);
    }
}

void MQTT_App_Init(void)
{
    printf("[SENSOR] init start\r\n");
    Sensor_TempHum_Init();
    printf("[SENSOR] init done\r\n");
    printf("[APP] transport=USART3 Bluetooth -> Ubuntu bridge -> MQTT\r\n");
    printf("[APP] mqtt host=%s:%s\r\n", MQTT_HOST, MQTT_PORT_STR);
    printf("[APP] topics control=%s telemetry=%s status=%s text_tx=%s text_rx=%s\r\n",
           MQTT_TOPIC_CONTROL,
           MQTT_TOPIC_TELEMETRY,
           MQTT_TOPIC_STATUS,
           MQTT_TOPIC_TEXT_TX,
           MQTT_TOPIC_TEXT_RX);
    s_bt_baud_index = 0;
    s_bt_baud_locked = false;
    bt_apply_current_baud("scan_start");
    bt_handle_link_state();
    printf("[MQTT_APP] init done\r\n");
}

void MQTT_App_Loop(void)
{
    static u32 publish_tick_ms = 0;
    static u32 state_tick_ms = 0;
    static u32 baud_scan_tick_ms = 0;
    char *rx;

    rx = BT_TryReceiveLine();
    if (rx != 0) {
        if ((!s_bt_baud_locked) && (!bt_payload_looks_valid(rx))) {
            printf("[BT] rx ignored at baud=%lu payload=%s\r\n",
                   (unsigned long)bt_current_baud(),
                   rx);
        } else {
            bt_lock_current_baud();
            bt_handle_cmd_message(rx);
        }
    }

    delay_ms(10);
    publish_tick_ms += 10;
    state_tick_ms += 10;
    if (!s_bt_baud_locked) {
        baud_scan_tick_ms += 10;
    }

    if (state_tick_ms >= BT_STATE_POLL_MS) {
        state_tick_ms = 0;
        bt_handle_link_state();
    }

    if ((!s_bt_baud_locked) && (baud_scan_tick_ms >= BT_UART_SCAN_PERIOD_MS)) {
        baud_scan_tick_ms = 0;
        bt_rotate_baud();
    }

    if (publish_tick_ms >= MQTT_PUBLISH_PERIOD_MS) {
        publish_tick_ms = 0;
        bt_publish_sensor_data();
    }
}
