#include "mqtt_service.h"
#include "board_adapter.h"
#include "wifi_function.h"
#include "iot_config.h"
#include "SysTick.h"
#include <stdio.h>
#include <string.h>

typedef enum
{
    MQTT_STATE_DISABLED = 0,
    MQTT_STATE_ESP_INIT,
    MQTT_STATE_WIFI_JOIN,
    MQTT_STATE_USERCFG,
    MQTT_STATE_CONNCFG,
    MQTT_STATE_CONNECT,
    MQTT_STATE_SUBSCRIBE,
    MQTT_STATE_READY
} mqtt_state_t;

static mqtt_state_t s_mqtt_state = MQTT_STATE_DISABLED;
static u32 s_next_action_ms = 0U;
static u32 s_next_report_ms = 0U;

static bool mqtt_text_has_value(const char *text)
{
    return (text != 0) && (text[0] != '\0');
}

static bool mqtt_text_is_placeholder(const char *text)
{
    if (!mqtt_text_has_value(text))
    {
        return true;
    }

    if (strncmp(text, "YOUR_", 5) == 0)
    {
        return true;
    }

    if (text[0] == '<')
    {
        return true;
    }

    return false;
}

static bool mqtt_wifi_config_ready(void)
{
    return !mqtt_text_is_placeholder(WIFI_SSID) &&
           !mqtt_text_is_placeholder(WIFI_PASSWORD);
}

static char mqtt_ascii_lower(char c)
{
    if ((c >= 'A') && (c <= 'Z'))
    {
        return (char)(c + ('a' - 'A'));
    }

    return c;
}

static void mqtt_format_1dp(float value, char *buf, u16 buf_size)
{
    long tenths;
    unsigned long abs_tenths;

    if ((buf == 0) || (buf_size < 5U))
    {
        return;
    }

    if (value >= 0.0f)
    {
        tenths = (long)(value * 10.0f + 0.5f);
    }
    else
    {
        tenths = (long)(value * 10.0f - 0.5f);
    }

    abs_tenths = (tenths < 0L) ? (unsigned long)(-tenths) : (unsigned long)tenths;
    snprintf(buf,
             buf_size,
             "%s%lu.%lu",
             (tenths < 0L) ? "-" : "",
             abs_tenths / 10UL,
             abs_tenths % 10UL);
}

static void mqtt_schedule_reconnect(const char *reason)
{
    if (reason != 0)
    {
        printf("%s\r\n", reason);
    }

    s_mqtt_state = MQTT_STATE_ESP_INIT;
    s_next_action_ms = SysTick_GetMs() + MQTT_RECONNECT_INTERVAL_MS;
    printf("[MQTT] reconnect scheduled\r\n");
}

static bool mqtt_publish_current_snapshot(void)
{
    board_env_data_t data;

    if (!BoardAdapter_GetData(&data))
    {
        if (!BoardAdapter_ForceSample())
        {
            printf("[SENSOR] no valid env sample, skip publish\r\n");
            return true;
        }
        if (!BoardAdapter_GetData(&data))
        {
            printf("[SENSOR] env cache unavailable\r\n");
            return true;
        }
    }

    return mqtt_publish_sensor_data(data.temperature, data.humidity, data.led);
}

static void mqtt_normalize_payload(const char *src, char *dst, u16 dst_len)
{
    u16 di = 0U;
    char c;

    if ((src == 0) || (dst == 0) || (dst_len < 2U))
    {
        return;
    }

    while (((c = *src++) != '\0') && (di + 1U < dst_len))
    {
        if ((c == ' ') || (c == '\t') || (c == '\r') || (c == '\n'))
        {
            continue;
        }
        dst[di++] = mqtt_ascii_lower(c);
    }

    dst[di] = '\0';
}

static bool mqtt_extract_led_value(const char *payload, u8 *led)
{
    char normalized[128];

    if ((payload == 0) || (led == 0))
    {
        return false;
    }

    mqtt_normalize_payload(payload, normalized, sizeof(normalized));

    if ((strstr(normalized, "\"led\":1") != 0) || (strcmp(normalized, "1") == 0))
    {
        *led = 1U;
        return true;
    }

    if ((strstr(normalized, "\"led\":0") != 0) || (strcmp(normalized, "0") == 0))
    {
        *led = 0U;
        return true;
    }

    return false;
}

static void mqtt_run_connect_step(void)
{
    switch (s_mqtt_state)
    {
    case MQTT_STATE_ESP_INIT:
        printf("[ESP] init start\r\n");
        if (ESP8266_Init())
        {
            printf("[ESP] init success\r\n");
            s_mqtt_state = MQTT_STATE_WIFI_JOIN;
            s_next_action_ms = SysTick_GetMs();
        }
        else
        {
            mqtt_schedule_reconnect("[ESP] init failed");
        }
        break;

    case MQTT_STATE_WIFI_JOIN:
        printf("[WIFI] joining SSID=%s\r\n", WIFI_SSID);
        if (ESP8266_JoinAP(WIFI_SSID, WIFI_PASSWORD))
        {
            printf("[WIFI] join success\r\n");
            s_mqtt_state = MQTT_STATE_USERCFG;
            s_next_action_ms = SysTick_GetMs();
        }
        else
        {
            mqtt_schedule_reconnect("[WIFI] join failed");
        }
        break;

    case MQTT_STATE_USERCFG:
        if (ESP8266_Set_MQTT_UserCfg(MQTT_CLIENT_ID, MQTT_USERNAME, MQTT_PASSWORD))
        {
            printf("[MQTT] user cfg success\r\n");
            s_mqtt_state = MQTT_STATE_CONNCFG;
            s_next_action_ms = SysTick_GetMs();
        }
        else
        {
            mqtt_schedule_reconnect("[MQTT] user cfg failed");
        }
        break;

    case MQTT_STATE_CONNCFG:
        if (ESP8266_Set_MQTT_ConnCfg(MQTT_KEEPALIVE_SECONDS))
        {
            printf("[MQTT] conn cfg success\r\n");
            s_mqtt_state = MQTT_STATE_CONNECT;
            s_next_action_ms = SysTick_GetMs();
        }
        else
        {
            mqtt_schedule_reconnect("[MQTT] conn cfg failed");
        }
        break;

    case MQTT_STATE_CONNECT:
        if (mqtt_connect())
        {
            printf("[MQTT] connect success\r\n");
            s_mqtt_state = MQTT_STATE_SUBSCRIBE;
            s_next_action_ms = SysTick_GetMs();
        }
        else
        {
            mqtt_schedule_reconnect("[MQTT] connect failed");
        }
        break;

    case MQTT_STATE_SUBSCRIBE:
        if (mqtt_subscribe_control_topic())
        {
            printf("[MQTT] subscribe success: %s\r\n", MQTT_SUBSCRIBE_TOPIC);
            s_mqtt_state = MQTT_STATE_READY;
            if (mqtt_publish_current_snapshot())
            {
                s_next_report_ms = SysTick_GetMs() + MQTT_REPORT_PERIOD_MS;
            }
            else
            {
                mqtt_schedule_reconnect("[MQTT] initial publish failed");
            }
        }
        else
        {
            mqtt_schedule_reconnect("[MQTT] subscribe failed");
        }
        break;

    default:
        break;
    }
}

void MQTT_Service_Init(void)
{
    printf("[CFG] topic up=%s\r\n", MQTT_PUBLISH_TOPIC);
    printf("[CFG] topic down=%s\r\n", MQTT_SUBSCRIBE_TOPIC);
    printf("[CFG] mqtt=%s:%u\r\n", MQTT_HOST, (unsigned int)MQTT_PORT);

    if (!mqtt_wifi_config_ready())
    {
        printf("[CFG] WIFI_SSID/WIFI_PASSWORD not configured, mqtt disabled\r\n");
        s_mqtt_state = MQTT_STATE_DISABLED;
        return;
    }

    s_mqtt_state = MQTT_STATE_ESP_INIT;
    s_next_action_ms = SysTick_GetMs();
    s_next_report_ms = 0U;
}

void MQTT_Service_Task(void)
{
    u32 now;
    char topic[96];
    char payload[128];

    if (s_mqtt_state == MQTT_STATE_DISABLED)
    {
        return;
    }

    now = SysTick_GetMs();

    if ((s_mqtt_state == MQTT_STATE_READY) && !ESP8266_IsConnected())
    {
        mqtt_schedule_reconnect("[MQTT] disconnected");
        return;
    }

    if (s_mqtt_state == MQTT_STATE_READY)
    {
        if (ESP8266_MQTT_PollMessage(topic, sizeof(topic), payload, sizeof(payload)))
        {
            printf("[MQTT] recv topic=%s payload=%s\r\n", topic, payload);
            if (strcmp(topic, MQTT_SUBSCRIBE_TOPIC) == 0)
            {
                if (!mqtt_process_downlink(payload))
                {
                    printf("[MQTT] invalid downlink payload\r\n");
                }
            }
        }

        if ((s_next_report_ms == 0U) || (now >= s_next_report_ms))
        {
            if (mqtt_publish_current_snapshot())
            {
                s_next_report_ms = SysTick_GetMs() + MQTT_REPORT_PERIOD_MS;
            }
            else
            {
                mqtt_schedule_reconnect("[MQTT] periodic publish failed");
            }
        }
        return;
    }

    if (now < s_next_action_ms)
    {
        return;
    }

    mqtt_run_connect_step();
}

bool mqtt_connect(void)
{
    return ESP8266_Link_MQTT(MQTT_HOST, MQTT_PORT);
}

bool mqtt_subscribe_control_topic(void)
{
    return ESP8266_MQTT_Subscribe(MQTT_SUBSCRIBE_TOPIC, MQTT_SUB_QOS);
}

bool mqtt_publish_sensor_data(float temp, float humi, u8 led)
{
    char temp_buf[16];
    char humi_buf[16];
    char payload[96];
    int n;

    mqtt_format_1dp(temp, temp_buf, sizeof(temp_buf));
    mqtt_format_1dp(humi, humi_buf, sizeof(humi_buf));

    n = snprintf(payload,
                 sizeof(payload),
                 "{\"temperature\":%s,\"humidity\":%s,\"led\":%u}",
                 temp_buf,
                 humi_buf,
                 (unsigned int)(led != 0U ? 1U : 0U));
    if ((n <= 0) || (n >= (int)sizeof(payload)))
    {
        return false;
    }

    printf("[MQTT] publish topic=%s payload=%s\r\n", MQTT_PUBLISH_TOPIC, payload);
    if (!ESP8266_MQTT_Publish(MQTT_PUBLISH_TOPIC, payload))
    {
        printf("[MQTT] publish failed\r\n");
        return false;
    }

    printf("[MQTT] publish success\r\n");
    return true;
}

bool mqtt_process_downlink(const char *payload)
{
    u8 led_value;

    if (!mqtt_extract_led_value(payload, &led_value))
    {
        return false;
    }

    BoardAdapter_SetLed(led_value);
    printf("[LED] control result=%u\r\n", (unsigned int)led_value);

    if (!mqtt_publish_current_snapshot())
    {
        printf("[MQTT] state republish failed after LED control\r\n");
        return false;
    }

    return true;
}
