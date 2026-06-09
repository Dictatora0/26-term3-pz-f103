#include "device_control.h"
#include "led.h"
#include "key.h"
#include "tftlcd.h"
#include <stdio.h>
#include <string.h>

static device_snapshot_t s_state;
static bool s_state_dirty = true;
static bool s_display_dirty = true;

static char dc_ascii_upper(char c)
{
    if ((c >= 'a') && (c <= 'z')) {
        return (char)(c - ('a' - 'A'));
    }
    return c;
}

static void dc_copy_string(char *dst, u16 size, const char *src)
{
    u16 i;

    if ((dst == 0) || (size == 0U)) {
        return;
    }

    if (src == 0) {
        dst[0] = '\0';
        return;
    }

    for (i = 0; (i + 1U) < size && src[i] != '\0'; ++i) {
        dst[i] = src[i];
    }
    dst[i] = '\0';
}

static void dc_normalize_command(const char *src, char *dst, u16 size)
{
    const char *start;
    const char *end;
    u16 i;
    u16 copy_len;

    if ((src == 0) || (dst == 0) || (size < 2U)) {
        return;
    }

    start = src;
    while ((*start == ' ') || (*start == '\t') || (*start == '\r') || (*start == '\n')) {
        start++;
    }

    end = start;
    while (*end != '\0') {
        end++;
    }
    while ((end > start) &&
           ((*(end - 1) == ' ') || (*(end - 1) == '\t') || (*(end - 1) == '\r') || (*(end - 1) == '\n'))) {
        end--;
    }

    copy_len = (u16)(end - start);
    if (copy_len >= (u16)(size - 1U)) {
        copy_len = (u16)(size - 1U);
    }

    for (i = 0; i < copy_len; ++i) {
        dst[i] = dc_ascii_upper(start[i]);
    }
    dst[copy_len] = '\0';
}

static void dc_mark_display_dirty(void)
{
    s_display_dirty = true;
}

static void dc_mark_state_dirty(void)
{
    s_state_dirty = true;
    s_display_dirty = true;
}

static void dc_set_led_hw(bool on)
{
    s_state.led_on = on;
    LED1 = on ? 0 : 1;
}

static void dc_apply_event(bool led_on, const char *source, const char *action, bool is_remote)
{
    dc_set_led_hw(led_on);
    dc_copy_string(s_state.last_source, (u16)sizeof(s_state.last_source), source);
    dc_copy_string(s_state.last_action, (u16)sizeof(s_state.last_action), action);
    if (is_remote) {
        s_state.remote_event_count++;
    } else {
        s_state.local_event_count++;
    }
    dc_mark_state_dirty();
}

static void dc_record_no_led_change(const char *source, const char *action, bool is_remote)
{
    dc_copy_string(s_state.last_source, (u16)sizeof(s_state.last_source), source);
    dc_copy_string(s_state.last_action, (u16)sizeof(s_state.last_action), action);
    if (is_remote) {
        s_state.remote_event_count++;
    } else {
        s_state.local_event_count++;
    }
    dc_mark_state_dirty();
}

static void dc_draw_line(u16 y, const char *text, u16 color)
{
    FRONT_COLOR = color;
    BACK_COLOR = WHITE;
    LCD_Fill(12, y, (u16)(tftlcd_data.width - 12U), (u16)(y + 20U), WHITE);
    LCD_ShowString(16, y + 2U, (u16)(tftlcd_data.width - 32U), 20, 16, (u8 *)text);
}

void Device_Control_Init(void)
{
    LED_Init();
    KEY_Init();
    dc_set_led_hw(false);

    memset(&s_state, 0, sizeof(s_state));
    s_state.led_on = false;
    s_state.mqtt_connected = false;
    s_state.sensor_valid = false;
    s_state.humidity_valid = false;
    dc_copy_string(s_state.last_source, (u16)sizeof(s_state.last_source), "BOOT");
    dc_copy_string(s_state.last_action, (u16)sizeof(s_state.last_action), "INIT");
    s_state_dirty = true;
    s_display_dirty = true;

    TFTLCD_Init();
    Device_Control_RefreshDisplay();
}

bool Device_Control_HandleLocalInput(void)
{
    u8 key_value;

    key_value = KEY_Scan(0);
    if (key_value == 0U) {
        return false;
    }

    if ((key_value == KEY0_PRESS) || (key_value == KEY_UP_PRESS)) {
        dc_apply_event(!s_state.led_on, (key_value == KEY0_PRESS) ? "KEY0" : "KEY_UP", "TOGGLE", false);
    } else if (key_value == KEY1_PRESS) {
        dc_apply_event(true, "KEY1", "FORCE_ON", false);
    } else if (key_value == KEY2_PRESS) {
        dc_apply_event(false, "KEY2", "FORCE_OFF", false);
    } else {
        return false;
    }

    return true;
}

bool Device_Control_HandleCommand(const char *payload, char *ack, u16 ack_size)
{
    char cmd[48];

    if ((ack == 0) || (ack_size < 2U)) {
        return false;
    }

    ack[0] = '\0';
    dc_normalize_command(payload, cmd, (u16)sizeof(cmd));
    if (cmd[0] == '\0') {
        dc_record_no_led_change("MQTT", "EMPTY", true);
        dc_copy_string(ack, ack_size, "CMD_EMPTY");
        return false;
    }

    if ((strcmp(cmd, "LED_ON") == 0) || (strcmp(cmd, "LED1_ON") == 0) ||
        (strcmp(cmd, "ON") == 0) || (strcmp(cmd, "1") == 0)) {
        dc_apply_event(true, "MQTT", "LED_ON", true);
        dc_copy_string(ack, ack_size, "LED_ON_OK");
        return true;
    }

    if ((strcmp(cmd, "LED_OFF") == 0) || (strcmp(cmd, "LED1_OFF") == 0) ||
        (strcmp(cmd, "OFF") == 0) || (strcmp(cmd, "0") == 0)) {
        dc_apply_event(false, "MQTT", "LED_OFF", true);
        dc_copy_string(ack, ack_size, "LED_OFF_OK");
        return true;
    }

    if ((strcmp(cmd, "TOGGLE") == 0) || (strcmp(cmd, "LED_TOGGLE") == 0)) {
        dc_apply_event(!s_state.led_on, "MQTT", "TOGGLE", true);
        dc_copy_string(ack, ack_size, "LED_TOGGLE_OK");
        return true;
    }

    if (strcmp(cmd, "PING") == 0) {
        dc_record_no_led_change("MQTT", "PING", true);
        dc_copy_string(ack, ack_size, "PONG");
        return true;
    }

    if (strcmp(cmd, "STATUS") == 0) {
        dc_record_no_led_change("MQTT", "STATUS", true);
        Device_Control_BuildStatePayload(ack, ack_size);
        return true;
    }

    dc_record_no_led_change("MQTT", "UNKNOWN", true);
    dc_copy_string(ack, ack_size, "CMD_UNKNOWN");
    return false;
}

void Device_Control_SetMqttConnected(bool connected)
{
    if (s_state.mqtt_connected != connected) {
        s_state.mqtt_connected = connected;
        dc_mark_state_dirty();
    } else {
        dc_mark_display_dirty();
    }
}

void Device_Control_UpdateSensor(const sensor_temp_hum_data_t *data)
{
    if (data == 0) {
        s_state.sensor_valid = false;
        s_state.humidity_valid = false;
        dc_mark_display_dirty();
        return;
    }

    s_state.sensor_valid = data->temperature_valid;
    s_state.temperature_c = data->temperature_c;
    s_state.humidity_valid = data->humidity_valid;
    s_state.humidity_rh = data->humidity_rh;
    dc_mark_display_dirty();
}

bool Device_Control_BuildStatePayload(char *payload, u16 payload_size)
{
    char temp_text[16];
    char hum_text[16];
    int n;

    if ((payload == 0) || (payload_size < 2U)) {
        return false;
    }

    if (s_state.sensor_valid) {
        sprintf(temp_text, "%.2f", s_state.temperature_c);
    } else {
        dc_copy_string(temp_text, (u16)sizeof(temp_text), "null");
    }

    if (s_state.sensor_valid && s_state.humidity_valid) {
        sprintf(hum_text, "%.2f", s_state.humidity_rh);
    } else {
        dc_copy_string(hum_text, (u16)sizeof(hum_text), "null");
    }

    n = sprintf(payload,
                "LED=%s;MQTT=%s;SRC=%s;ACT=%s;LOCAL=%lu;REMOTE=%lu;T=%s;H=%s",
                s_state.led_on ? "ON" : "OFF",
                s_state.mqtt_connected ? "ON" : "OFF",
                s_state.last_source,
                s_state.last_action,
                (unsigned long)s_state.local_event_count,
                (unsigned long)s_state.remote_event_count,
                temp_text,
                hum_text);

    if ((n <= 0) || (n >= (int)payload_size)) {
        if (payload_size > 0U) {
            payload[0] = '\0';
        }
        return false;
    }

    return true;
}

void Device_Control_RefreshDisplay(void)
{
    char line[96];

    if (!s_display_dirty) {
        return;
    }

    FRONT_COLOR = WHITE;
    BACK_COLOR = BLUE;
    LCD_Fill(0, 0, (u16)(tftlcd_data.width - 1U), 40, BLUE);
    LCD_ShowString(18, 12, 260, 20, 16, (u8 *)"STM32 MQTT LED PANEL");

    LCD_Fill(0, 42, (u16)(tftlcd_data.width - 1U), (u16)(tftlcd_data.height - 1U), WHITE);

    sprintf(line, "MQTT : %s", s_state.mqtt_connected ? "ONLINE" : "OFFLINE");
    dc_draw_line(56, line, s_state.mqtt_connected ? GREEN : RED);

    sprintf(line, "LED  : %s", s_state.led_on ? "ON" : "OFF");
    dc_draw_line(88, line, s_state.led_on ? GREEN : RED);

    sprintf(line, "LAST : %s / %s", s_state.last_source, s_state.last_action);
    dc_draw_line(120, line, BLACK);

    sprintf(line, "LOCAL: %lu   REMOTE: %lu",
            (unsigned long)s_state.local_event_count,
            (unsigned long)s_state.remote_event_count);
    dc_draw_line(152, line, DARKBLUE);

    if (s_state.sensor_valid) {
        sprintf(line, "TEMP : %.2f C", s_state.temperature_c);
    } else {
        sprintf(line, "TEMP : N/A");
    }
    dc_draw_line(184, line, BLACK);

    if (s_state.sensor_valid && s_state.humidity_valid) {
        sprintf(line, "HUM  : %.2f %%", s_state.humidity_rh);
    } else {
        sprintf(line, "HUM  : N/A");
    }
    dc_draw_line(216, line, BLACK);

    dc_draw_line(264, "KEY0/UP TOGGLE  KEY1 ON  KEY2 OFF", GRAYBLUE);
    dc_draw_line(296, "Phone -> MQTT cmd topic -> board LED/LCD", GRAYBLUE);

    s_display_dirty = false;
}

bool Device_Control_IsStateDirty(void)
{
    return s_state_dirty;
}

void Device_Control_ClearStateDirty(void)
{
    s_state_dirty = false;
}

const device_snapshot_t *Device_Control_GetSnapshot(void)
{
    return &s_state;
}
