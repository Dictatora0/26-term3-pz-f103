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

typedef enum
{
    MQTT_TRANSPORT_NONE = 0,
    MQTT_TRANSPORT_ESP_AT,
    MQTT_TRANSPORT_RAW_TCP
} mqtt_transport_t;

static mqtt_state_t s_mqtt_state = MQTT_STATE_DISABLED;
static mqtt_transport_t s_mqtt_transport = MQTT_TRANSPORT_NONE;
static u32 s_next_action_ms = 0U;
static u32 s_next_report_ms = 0U;
static u16 s_mqtt_packet_id = 1U;

static void mqtt_log_text_frame(const char *tag, const char *text);
static bool mqtt_text_has_value(const char *text);
static bool mqtt_raw_connect(void);
static bool mqtt_raw_subscribe_topic(const char *topic, u8 qos);
static bool mqtt_raw_publish_payload(const char *topic, const char *payload);
static bool mqtt_raw_parse_publish(const u8 *packet, u16 packet_len, char *topic, u16 topic_size, char *payload, u16 payload_size);

static void mqtt_dump_hex(const char *tag, const u8 *data, u16 len)
{
    char line[96];
    u16 offset = 0U;
    int n;
    u16 i;

    if ((tag == 0) || (data == 0))
    {
        return;
    }

    n = snprintf(line, sizeof(line), "%s len=%u data=", tag, (unsigned int)len);
    if (n <= 0)
    {
        return;
    }
    if (n >= (int)sizeof(line))
    {
        n = (int)sizeof(line) - 1;
    }
    offset = (u16)n;

    for (i = 0U; i < len; ++i)
    {
        if ((u16)(offset + 4U) >= (u16)sizeof(line))
        {
            printf("%s\r\n", line);
            offset = 0U;
            line[0] = '\0';
        }

        n = snprintf(&line[offset], sizeof(line) - offset, "%02X ", (unsigned int)data[i]);
        if (n <= 0)
        {
            break;
        }
        if (n >= (int)(sizeof(line) - offset))
        {
            offset = (u16)sizeof(line) - 1U;
            line[offset] = '\0';
            break;
        }
        offset = (u16)(offset + n);
    }

    if (offset > 0U)
    {
        printf("%s\r\n", line);
    }
}

static void mqtt_log_text_frame(const char *tag, const char *text)
{
    char preview[160];
    u16 len;

    if ((tag == 0) || (text == 0))
    {
        return;
    }

    len = (u16)strlen(text);
    if (len >= (u16)sizeof(preview))
    {
        len = (u16)sizeof(preview) - 1U;
    }

    memcpy(preview, text, len);
    preview[len] = '\0';
    printf("%s %s\r\n", tag, preview);
}

static u16 mqtt_next_packet_id(void)
{
    s_mqtt_packet_id++;
    if (s_mqtt_packet_id == 0U)
    {
        s_mqtt_packet_id = 1U;
    }
    return s_mqtt_packet_id;
}

static u16 mqtt_write_utf8(u8 *buf, u16 buf_size, u16 offset, const char *text)
{
    u16 len;

    if ((buf == 0) || (text == 0) || ((u16)(offset + 2U) > buf_size))
    {
        return 0U;
    }

    len = (u16)strlen(text);
    if ((u16)(offset + 2U + len) > buf_size)
    {
        return 0U;
    }

    buf[offset++] = (u8)((len >> 8) & 0xFFU);
    buf[offset++] = (u8)(len & 0xFFU);
    memcpy(&buf[offset], text, len);
    return (u16)(offset + len);
}

static u16 mqtt_encode_remaining_length(u8 *buf, u16 value)
{
    u16 offset = 0U;
    u8 encoded;

    do
    {
        encoded = (u8)(value % 128U);
        value = (u16)(value / 128U);
        if (value > 0U)
        {
            encoded |= 0x80U;
        }
        buf[offset++] = encoded;
    } while (value > 0U);

    return offset;
}

static bool mqtt_send_raw_packet(const u8 *packet, u16 packet_len)
{
    if ((packet == 0) || (packet_len == 0U))
    {
        return false;
    }

    mqtt_dump_hex("[MQTT][RAW] TX", packet, packet_len);
    return ESP8266_Send(packet, packet_len);
}

static bool mqtt_wait_tcp_packet(u8 expected_type, u8 *packet, u16 packet_size, u16 *packet_len, u32 timeout_ms)
{
    u32 start_ms;
    u16 rx_len;

    if ((packet == 0) || (packet_size == 0U) || (packet_len == 0))
    {
        return false;
    }

    *packet_len = 0U;
    start_ms = SysTick_GetMs();
    while ((SysTick_GetMs() - start_ms) < timeout_ms)
    {
        ESP8266_Process();
        if (ESP8266_TCP_PollPacket(packet, packet_size, &rx_len))
        {
            mqtt_dump_hex("[MQTT][RAW] RX", packet, rx_len);
            if ((rx_len > 0U) && ((packet[0] & 0xF0U) == expected_type))
            {
                *packet_len = rx_len;
                return true;
            }

            printf("[MQTT][RAW] skip packet type=0x%02X len=%u\r\n",
                   (unsigned int)((rx_len > 0U) ? packet[0] : 0U),
                   (unsigned int)rx_len);
        }
        delay_ms(10U);
    }

    return false;
}

static bool mqtt_raw_connect(void)
{
    u8 packet[192];
    u8 payload_buf[128];
    u8 rx[96];
    u16 offset;
    u16 payload_offset;
    u16 variable_len;
    u16 remain_len;
    u16 packet_len;
    u8 connect_flags;
    bool has_user;
    bool has_pass;

    printf("[MQTT][RAW] fallback connect start\r\n");
    if (!ESP8266_Ping(MQTT_HOST))
    {
        printf("[MQTT][RAW] ping failed\r\n");
    }
    else
    {
        printf("[MQTT][RAW] ping ok\r\n");
    }

    if (!ESP8266_CloseTCP())
    {
        printf("[MQTT][RAW] close stale TCP ignored\r\n");
    }
    if (!ESP8266_OpenTCP(MQTT_HOST, MQTT_PORT))
    {
        printf("[MQTT][RAW] TCP open failed\r\n");
        return false;
    }
    printf("[MQTT][RAW] TCP open success\r\n");

    has_user = mqtt_text_has_value(MQTT_USERNAME);
    has_pass = mqtt_text_has_value(MQTT_PASSWORD);
    connect_flags = 0x02U;
    if (has_user)
    {
        connect_flags |= 0x80U;
    }
    if (has_pass)
    {
        connect_flags |= 0x40U;
    }

    variable_len = 10U;
    payload_offset = 0U;
    payload_offset = mqtt_write_utf8(payload_buf, sizeof(payload_buf), payload_offset, MQTT_CLIENT_ID);
    if (payload_offset == 0U)
    {
        return false;
    }
    if (has_user)
    {
        payload_offset = mqtt_write_utf8(payload_buf, sizeof(payload_buf), payload_offset, MQTT_USERNAME);
        if (payload_offset == 0U)
        {
            return false;
        }
    }
    if (has_pass)
    {
        payload_offset = mqtt_write_utf8(payload_buf, sizeof(payload_buf), payload_offset, MQTT_PASSWORD);
        if (payload_offset == 0U)
        {
            return false;
        }
    }

    remain_len = (u16)(variable_len + payload_offset);
    packet[0] = 0x10U;
    offset = 1U;
    offset = (u16)(offset + mqtt_encode_remaining_length(&packet[offset], remain_len));
    packet[offset++] = 0x00U;
    packet[offset++] = 0x04U;
    packet[offset++] = 'M';
    packet[offset++] = 'Q';
    packet[offset++] = 'T';
    packet[offset++] = 'T';
    packet[offset++] = 0x04U;
    packet[offset++] = connect_flags;
    packet[offset++] = (u8)((MQTT_KEEPALIVE_SECONDS >> 8) & 0xFFU);
    packet[offset++] = (u8)(MQTT_KEEPALIVE_SECONDS & 0xFFU);
    memcpy(&packet[offset], payload_buf, payload_offset);
    offset = (u16)(offset + payload_offset);

    if (!mqtt_send_raw_packet(packet, offset))
    {
        printf("[MQTT][RAW] CONNECT send failed\r\n");
        return false;
    }

    if (!mqtt_wait_tcp_packet(0x20U, rx, sizeof(rx), &packet_len, 4000U))
    {
        printf("[MQTT][RAW] CONNACK timeout\r\n");
        return false;
    }

    if ((packet_len < 4U) || (rx[1] != 0x02U) || (rx[3] != 0x00U))
    {
        printf("[MQTT][RAW] CONNACK reject code=%u len=%u\r\n",
               (unsigned int)((packet_len >= 4U) ? rx[3] : 0xFFU),
               (unsigned int)packet_len);
        return false;
    }

    ESP8266_SetTcpConnected(ENABLE);
    ESP8266_SetMqttReady(ENABLE);
    s_mqtt_transport = MQTT_TRANSPORT_RAW_TCP;
    printf("[MQTT][RAW] CONNACK success\r\n");
    return true;
}

static bool mqtt_raw_subscribe_topic(const char *topic, u8 qos)
{
    u8 packet[192];
    u8 rx[96];
    u16 offset;
    u16 remain_len;
    u16 packet_id;
    u16 packet_len;

    if (topic == 0)
    {
        return false;
    }

    packet_id = mqtt_next_packet_id();
    packet[0] = 0x82U;
    offset = 1U;
    remain_len = (u16)(2U + 2U + strlen(topic) + 1U);
    offset = (u16)(offset + mqtt_encode_remaining_length(&packet[offset], remain_len));
    packet[offset++] = (u8)((packet_id >> 8) & 0xFFU);
    packet[offset++] = (u8)(packet_id & 0xFFU);
    offset = mqtt_write_utf8(packet, sizeof(packet), offset, topic);
    if (offset == 0U)
    {
        return false;
    }
    packet[offset++] = qos;

    if (!mqtt_send_raw_packet(packet, offset))
    {
        printf("[MQTT][RAW] SUBSCRIBE send failed\r\n");
        return false;
    }

    if (!mqtt_wait_tcp_packet(0x90U, rx, sizeof(rx), &packet_len, 3000U))
    {
        printf("[MQTT][RAW] SUBACK timeout\r\n");
        return false;
    }

    if ((packet_len < 5U) || (rx[4] == 0x80U))
    {
        printf("[MQTT][RAW] SUBACK failed code=%u\r\n",
               (unsigned int)((packet_len >= 5U) ? rx[4] : 0xFFU));
        return false;
    }

    printf("[MQTT][RAW] SUBACK success pid=%u qos=%u\r\n",
           (unsigned int)packet_id,
           (unsigned int)qos);
    return true;
}

static bool mqtt_raw_publish_payload(const char *topic, const char *payload)
{
    u8 packet[256];
    u16 offset;
    u16 remain_len;
    u16 topic_len;
    u16 payload_len;

    if ((topic == 0) || (payload == 0))
    {
        return false;
    }

    topic_len = (u16)strlen(topic);
    payload_len = (u16)strlen(payload);
    remain_len = (u16)(2U + topic_len + payload_len);

    packet[0] = 0x30U;
    offset = 1U;
    offset = (u16)(offset + mqtt_encode_remaining_length(&packet[offset], remain_len));
    offset = mqtt_write_utf8(packet, sizeof(packet), offset, topic);
    if (offset == 0U)
    {
        return false;
    }
    if ((u16)(offset + payload_len) > (u16)sizeof(packet))
    {
        return false;
    }
    memcpy(&packet[offset], payload, payload_len);
    offset = (u16)(offset + payload_len);
    if (!mqtt_send_raw_packet(packet, offset))
    {
        printf("[MQTT][RAW] PUBLISH send failed\r\n");
        return false;
    }

    return true;
}

static bool mqtt_raw_parse_publish(const u8 *packet, u16 packet_len, char *topic, u16 topic_size, char *payload, u16 payload_size)
{
    u16 index;
    u32 multiplier;
    u32 remain_len;
    u16 topic_len;
    u16 topic_len_all;
    u16 payload_len;
    u8 qos;

    if ((packet == 0) || (packet_len < 5U) || (topic == 0) || (payload == 0))
    {
        return false;
    }

    if ((packet[0] & 0xF0U) != 0x30U)
    {
        printf("[MQTT][RAW] non-publish packet type=0x%02X\r\n", (unsigned int)packet[0]);
        return false;
    }

    index = 1U;
    multiplier = 1U;
    remain_len = 0U;
    while (index < packet_len)
    {
        remain_len += (u32)(packet[index] & 0x7FU) * multiplier;
        if ((packet[index] & 0x80U) == 0U)
        {
            index++;
            break;
        }
        multiplier *= 128U;
        index++;
    }

    if ((u32)(packet_len - index) < remain_len)
    {
        return false;
    }

    if ((u16)(index + 2U) > packet_len)
    {
        return false;
    }

    topic_len_all = (u16)(((u16)packet[index] << 8) | packet[index + 1U]);
    index = (u16)(index + 2U);
    if ((u16)(index + topic_len_all) > packet_len)
    {
        return false;
    }

    topic_len = topic_len_all;
    if (topic_len >= topic_size)
    {
        topic_len = topic_size - 1U;
    }
    memcpy(topic, &packet[index], topic_len);
    topic[topic_len] = '\0';
    index = (u16)(index + topic_len_all);

    qos = (u8)((packet[0] >> 1) & 0x03U);
    if (qos > 0U)
    {
        if ((u16)(index + 2U) > packet_len)
        {
            return false;
        }
        index = (u16)(index + 2U);
    }

    if (packet_len < index)
    {
        return false;
    }

    payload_len = (u16)(packet_len - index);
    if (payload_len >= payload_size)
    {
        payload_len = payload_size - 1U;
    }
    memcpy(payload, &packet[index], payload_len);
    payload[payload_len] = '\0';
    return true;
}

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
    s_mqtt_transport = MQTT_TRANSPORT_NONE;
    ESP8266_SetMqttReady(DISABLE);
    ESP8266_SetTcpConnected(DISABLE);
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
    printf("[MQTT] normalized downlink=%s\r\n", normalized);

      if ((strstr(normalized, "\"led\":1") != 0) ||
          (strstr(normalized, "\"led\":\"1\"") != 0) ||
          (strstr(normalized, "\"led\":true") != 0) ||
          (strstr(normalized, "\"led\":\"true\"") != 0) ||
          (strstr(normalized, "{led:1}") != 0) ||
          (strstr(normalized, "{led:\"1\"}") != 0) ||
          (strstr(normalized, "{led:true}") != 0) ||
          (strstr(normalized, "{led:\"true\"}") != 0) ||
          (strstr(normalized, "led:1") != 0) ||
          (strstr(normalized, "led:\"1\"") != 0) ||
          (strstr(normalized, "led:true") != 0) ||
          (strstr(normalized, "led:\"true\"") != 0) ||
          (strcmp(normalized, "1") == 0))
    {
        *led = 1U;
        return true;
    }

      if ((strstr(normalized, "\"led\":0") != 0) ||
          (strstr(normalized, "\"led\":\"0\"") != 0) ||
          (strstr(normalized, "\"led\":false") != 0) ||
          (strstr(normalized, "\"led\":\"false\"") != 0) ||
          (strstr(normalized, "{led:0}") != 0) ||
          (strstr(normalized, "{led:\"0\"}") != 0) ||
          (strstr(normalized, "{led:false}") != 0) ||
          (strstr(normalized, "{led:\"false\"}") != 0) ||
          (strstr(normalized, "led:0") != 0) ||
          (strstr(normalized, "led:\"0\"") != 0) ||
          (strstr(normalized, "led:false") != 0) ||
          (strstr(normalized, "led:\"false\"") != 0) ||
          (strcmp(normalized, "0") == 0))
    {
        *led = 0U;
        return true;
    }

    return false;
}

static bool mqtt_poll_downlink_frame(char *topic, u16 topic_size, char *payload, u16 payload_size)
{
    u8 packet[192];
    u16 packet_len;

    if (s_mqtt_transport == MQTT_TRANSPORT_ESP_AT)
    {
        return ESP8266_MQTT_PollMessage(topic, topic_size, payload, payload_size);
    }

    if (s_mqtt_transport != MQTT_TRANSPORT_RAW_TCP)
    {
        return false;
    }

    if (!ESP8266_TCP_PollPacket(packet, sizeof(packet), &packet_len))
    {
        return false;
    }

    mqtt_dump_hex("[MQTT][RAW] RX", packet, packet_len);
    if (!mqtt_raw_parse_publish(packet, packet_len, topic, topic_size, payload, payload_size))
    {
        printf("[MQTT][RAW] downlink parse skipped len=%u type=0x%02X\r\n",
               (unsigned int)packet_len,
               (unsigned int)((packet_len > 0U) ? packet[0] : 0U));
        return false;
    }

    printf("[MQTT][RAW] publish topic=%s payload=%s\r\n", topic, payload);
    return true;
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
    s_mqtt_transport = MQTT_TRANSPORT_NONE;
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

    ESP8266_Process();
    now = SysTick_GetMs();

    if ((s_mqtt_state == MQTT_STATE_READY) && !ESP8266_IsConnected())
    {
        mqtt_schedule_reconnect("[MQTT] disconnected");
        return;
    }

    if (s_mqtt_state == MQTT_STATE_READY)
    {
        if (mqtt_poll_downlink_frame(topic, sizeof(topic), payload, sizeof(payload)))
        {
            printf("[MQTT] recv topic=%s payload=%s\r\n", topic, payload);
            if (strcmp(topic, MQTT_SUBSCRIBE_TOPIC) == 0)
            {
                printf("[MQTT] downlink matched control topic\r\n");
                if (!mqtt_process_downlink(payload))
                {
                    printf("[MQTT] invalid downlink payload\r\n");
                }
            }
            else
            {
                printf("[MQTT] downlink ignored topic=%s\r\n", topic);
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
    printf("[MQTT] connect try: ESP-AT MQTT\r\n");
    if (ESP8266_Link_MQTT(MQTT_HOST, MQTT_PORT))
    {
        s_mqtt_transport = MQTT_TRANSPORT_ESP_AT;
        return true;
    }

    printf("[MQTT] ESP-AT MQTT connect failed, fallback to raw TCP MQTT\r\n");
    return mqtt_raw_connect();
}

bool mqtt_subscribe_control_topic(void)
{
    if (s_mqtt_transport == MQTT_TRANSPORT_ESP_AT)
    {
        return ESP8266_MQTT_Subscribe(MQTT_SUBSCRIBE_TOPIC, MQTT_SUB_QOS);
    }

    if (s_mqtt_transport == MQTT_TRANSPORT_RAW_TCP)
    {
        printf("[MQTT][RAW] subscribe topic=%s qos=%u\r\n",
               MQTT_SUBSCRIBE_TOPIC,
               (unsigned int)MQTT_SUB_QOS);
        return mqtt_raw_subscribe_topic(MQTT_SUBSCRIBE_TOPIC, MQTT_SUB_QOS);
    }

    return false;
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
    if ((s_mqtt_transport == MQTT_TRANSPORT_ESP_AT) &&
        !ESP8266_MQTT_Publish(MQTT_PUBLISH_TOPIC, payload))
    {
        printf("[MQTT] publish failed\r\n");
        return false;
    }
    if ((s_mqtt_transport == MQTT_TRANSPORT_RAW_TCP) &&
        !mqtt_raw_publish_payload(MQTT_PUBLISH_TOPIC, payload))
    {
        printf("[MQTT][RAW] publish failed\r\n");
        return false;
    }

    printf("[MQTT] publish success\r\n");
    return true;
}

bool mqtt_process_downlink(const char *payload)
{
    u8 led_value;
    board_env_data_t data;

    if (!mqtt_extract_led_value(payload, &led_value))
    {
        mqtt_log_text_frame("[MQTT] unsupported downlink payload=", payload);
        return false;
    }

    printf("[LED] request=%u\r\n", (unsigned int)led_value);
    BoardAdapter_SetLed(led_value);
    printf("[LED] control result=%u actual=%u\r\n",
           (unsigned int)led_value,
           (unsigned int)BoardAdapter_GetLed());

    if (BoardAdapter_GetData(&data))
    {
        printf("[MQTT] republish after downlink temp=%.1f hum=%.1f led=%u\r\n",
               data.temperature,
               data.humidity,
               (unsigned int)data.led);
    }
    else
    {
        printf("[MQTT] republish after downlink using fresh sample\r\n");
    }

    if (!mqtt_publish_current_snapshot())
    {
        printf("[MQTT] state republish failed after LED control\r\n");
        return false;
    }

    printf("[MQTT] state republish success after LED control\r\n");
    return true;
}
