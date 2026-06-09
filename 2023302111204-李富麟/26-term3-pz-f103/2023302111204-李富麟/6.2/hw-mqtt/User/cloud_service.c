#include "cloud_service.h"
#include "cloud_config.h"
#include "bsp_sensor.h"
#include "wifi_config.h"
#include "wifi_function.h"
#include "led.h"
#include "SysTick.h"
#include <stdio.h>
#include <string.h>

typedef enum
{
	CLOUD_STATE_LOCAL = 0,
	CLOUD_STATE_ESP_INIT,
	CLOUD_STATE_WIFI_JOIN,
	CLOUD_STATE_MQTT_USERCFG,
	CLOUD_STATE_MQTT_CONNCFG,
	CLOUD_STATE_MQTT_CONNECT,
	CLOUD_STATE_MQTT_SUBSCRIBE,
	CLOUD_STATE_PUBLISH_ONLINE,
	CLOUD_STATE_READY
} CloudState;

static CloudState s_cloud_state = CLOUD_STATE_LOCAL;
static bool s_cloud_initialized = false;
static u32 s_next_cloud_action_ms = 0U;
static u32 s_next_report_ms = 0U;

static bool cloud_text_has_value(const char *text)
{
	return (text != 0) && (text[0] != '\0');
}

static bool cloud_text_is_placeholder(const char *text)
{
	if (!cloud_text_has_value(text))
	{
		return true;
	}

	return (strncmp(text, "YOUR_", 5) == 0);
}

static bool cloud_wifi_config_ready(void)
{
	return !cloud_text_is_placeholder(WIFI_SSID) &&
	       !cloud_text_is_placeholder(WIFI_PASSWORD);
}

static bool cloud_mqtt_config_ready(void)
{
	return !cloud_text_is_placeholder(MQTT_BROKER_HOST) &&
	       !cloud_text_is_placeholder(MQTT_USERNAME) &&
	       !cloud_text_is_placeholder(MQTT_PASSWORD) &&
	       cloud_text_has_value(MQTT_CLIENT_ID) &&
	       cloud_text_has_value(MQTT_TOPIC_TELEMETRY) &&
	       cloud_text_has_value(MQTT_TOPIC_STATUS) &&
	       cloud_text_has_value(MQTT_TOPIC_COMMAND) &&
	       cloud_text_has_value(MQTT_TOPIC_REPLY);
}

static char cloud_ascii_upper(char c)
{
	if ((c >= 'a') && (c <= 'z'))
	{
		return (char)(c - ('a' - 'A'));
	}

	return c;
}

static bool cloud_streq_icase(const char *a, const char *b)
{
	if ((a == 0) || (b == 0))
	{
		return false;
	}

	while ((*a != '\0') && (*b != '\0'))
	{
		if (cloud_ascii_upper(*a) != cloud_ascii_upper(*b))
		{
			return false;
		}
		++a;
		++b;
	}

	return (*a == '\0') && (*b == '\0');
}

static void cloud_format_temperature_1dp(float temperature, char *buf, u16 buf_size)
{
	long tenths;
	long abs_tenths;

	if ((buf == 0) || (buf_size < 5U))
	{
		return;
	}

	if (temperature >= 0.0f)
	{
		tenths = (long)(temperature * 10.0f + 0.5f);
	}
	else
	{
		tenths = (long)(temperature * 10.0f - 0.5f);
	}

	abs_tenths = (tenths < 0L) ? -tenths : tenths;
	snprintf(buf,
	         buf_size,
	         "%s%ld.%ld",
	         (tenths < 0L) ? "-" : "",
	         abs_tenths / 10L,
	         abs_tenths % 10L);
}

static bool cloud_build_telemetry_json(const SensorData *data, char *payload, u16 payload_size)
{
	char temp_buf[24];
	char light_buf[16];
	int n;

	if ((data == 0) || (payload == 0) || (payload_size < 32U))
	{
		return false;
	}

	if (data->temperature_valid)
	{
		cloud_format_temperature_1dp(data->temperature, temp_buf, sizeof(temp_buf));
	}
	else
	{
		strcpy(temp_buf, "null");
	}

	if (data->light_valid)
	{
		snprintf(light_buf, sizeof(light_buf), "%lu", (unsigned long)data->light);
	}
	else
	{
		strcpy(light_buf, "null");
	}

	n = snprintf(payload,
	             payload_size,
	             "{\"device_id\":\"%s\",\"temperature\":%s,\"light\":%s,\"uptime\":%lu,\"status\":\"online\"}",
	             MQTT_DEVICE_ID,
	             temp_buf,
	             light_buf,
	             (unsigned long)data->uptime);

	return (n > 0) && (n < (int)payload_size);
}

static bool cloud_build_online_json(char *payload, u16 payload_size)
{
	int n;

	if ((payload == 0) || (payload_size < 32U))
	{
		return false;
	}

	n = snprintf(payload,
	             payload_size,
	             "{\"device_id\":\"%s\",\"status\":\"online\"}",
	             MQTT_DEVICE_ID);
	return (n > 0) && (n < (int)payload_size);
}

static bool cloud_build_reply_json(const char *action,
                                   const char *value,
                                   const char *result,
                                   bool include_action_value,
                                   char *payload,
                                   u16 payload_size)
{
	int n;

	if ((payload == 0) || (payload_size < 48U) || (result == 0))
	{
		return false;
	}

	if (include_action_value && (action != 0) && (value != 0))
	{
		n = snprintf(payload,
		             payload_size,
		             "{\"device_id\":\"%s\",\"action\":\"%s\",\"value\":\"%s\",\"result\":\"%s\"}",
		             MQTT_DEVICE_ID,
		             action,
		             value,
		             result);
	}
	else
	{
		n = snprintf(payload,
		             payload_size,
		             "{\"device_id\":\"%s\",\"result\":\"%s\"}",
		             MQTT_DEVICE_ID,
		             result);
	}

	return (n > 0) && (n < (int)payload_size);
}

static bool cloud_publish_json(const char *topic, const char *payload)
{
	if (!ESP8266_MQTT_Publish(topic, payload))
	{
		printf("[MQTT] Publish failed: %s\r\n", topic);
		return false;
	}

	return true;
}

static void cloud_schedule_reconnect(const char *reason)
{
	if (reason != 0)
	{
		printf("%s\r\n", reason);
	}

	s_cloud_state = CLOUD_STATE_ESP_INIT;
	s_next_cloud_action_ms = SysTick_GetMs() + CLOUD_RECONNECT_INTERVAL_MS;
	printf("[MQTT] Reconnecting...\r\n");
}

static bool cloud_extract_json_string(const char *json, const char *key, char *out, u16 out_size)
{
	char pattern[24];
	const char *key_pos;
	const char *colon_pos;
	const char *value_begin;
	const char *value_end;
	u16 copy_len;
	int n;

	if ((json == 0) || (key == 0) || (out == 0) || (out_size < 2U))
	{
		return false;
	}

	n = snprintf(pattern, sizeof(pattern), "\"%s\"", key);
	if ((n <= 0) || (n >= (int)sizeof(pattern)))
	{
		return false;
	}

	key_pos = strstr(json, pattern);
	if (key_pos == 0)
	{
		return false;
	}

	colon_pos = strchr(key_pos + n, ':');
	if (colon_pos == 0)
	{
		return false;
	}

	value_begin = colon_pos + 1;
	while ((*value_begin == ' ') || (*value_begin == '\t') || (*value_begin == '\r') || (*value_begin == '\n'))
	{
		value_begin++;
	}

	if (*value_begin != '"')
	{
		return false;
	}
	value_begin++;

	value_end = strchr(value_begin, '"');
	if (value_end == 0)
	{
		return false;
	}

	copy_len = (u16)(value_end - value_begin);
	if (copy_len >= out_size)
	{
		return false;
	}

	memcpy(out, value_begin, copy_len);
	out[copy_len] = '\0';
	return true;
}

static void cloud_handle_command(const char *topic, const char *payload)
{
	char action[24];
	char value[24];
	char reply[160];

	if ((payload == 0) || (topic == 0))
	{
		return;
	}

	if (strcmp(topic, MQTT_TOPIC_COMMAND) != 0)
	{
		return;
	}

	if (cloud_extract_json_string(payload, "action", action, sizeof(action)) &&
	    cloud_extract_json_string(payload, "value", value, sizeof(value)) &&
	    cloud_streq_icase(action, "led"))
	{
		if (cloud_streq_icase(value, "on"))
		{
			LED1 = 0;
			printf("[MQTT] Command received: led=on\r\n");
			if (cloud_build_reply_json("led", "on", "success", true, reply, sizeof(reply)))
			{
				(void)cloud_publish_json(MQTT_TOPIC_REPLY, reply);
			}
			return;
		}

		if (cloud_streq_icase(value, "off"))
		{
			LED1 = 1;
			printf("[MQTT] Command received: led=off\r\n");
			if (cloud_build_reply_json("led", "off", "success", true, reply, sizeof(reply)))
			{
				(void)cloud_publish_json(MQTT_TOPIC_REPLY, reply);
			}
			return;
		}
	}

	printf("[MQTT] Command received: unsupported\r\n");
	if (cloud_build_reply_json(0, 0, "unsupported_command", false, reply, sizeof(reply)))
	{
		(void)cloud_publish_json(MQTT_TOPIC_REPLY, reply);
	}
}

static void cloud_report_telemetry_if_due(void)
{
	SensorData data;
	char payload[192];
	u32 now = SysTick_GetMs();

	if (now < s_next_report_ms)
	{
		return;
	}

	s_next_report_ms = now + TELEMETRY_INTERVAL_MS;

	if (!Sensor_Read(&data))
	{
		printf("[ERROR] Sensor cache unavailable\r\n");
		return;
	}

	if (!cloud_build_telemetry_json(&data, payload, sizeof(payload)))
	{
		printf("[ERROR] Telemetry JSON build failed\r\n");
		return;
	}

	printf("%s\r\n", payload);

#if CLOUD_ENABLE
	if ((s_cloud_state == CLOUD_STATE_READY) && ESP8266_Is_MQTT_Ready())
	{
		if (cloud_publish_json(MQTT_TOPIC_TELEMETRY, payload))
		{
			printf("[MQTT] Publish telemetry success\r\n");
		}
		else
		{
			cloud_schedule_reconnect("[MQTT] Connection lost");
		}
	}
#endif
}

static void cloud_run_connect_step(void)
{
	char payload[96];

	switch (s_cloud_state)
	{
	case CLOUD_STATE_ESP_INIT:
		if (ESP8266_Init())
		{
			s_cloud_state = CLOUD_STATE_WIFI_JOIN;
			s_next_cloud_action_ms = SysTick_GetMs();
		}
		else
		{
			cloud_schedule_reconnect("[ERROR] ESP8266 timeout");
		}
		break;

	case CLOUD_STATE_WIFI_JOIN:
		printf("[WIFI] Joining AP...\r\n");
		if (ESP8266_JoinAP(WIFI_SSID, WIFI_PASSWORD))
		{
			printf("[WIFI] Connected\r\n");
			s_cloud_state = CLOUD_STATE_MQTT_USERCFG;
			s_next_cloud_action_ms = SysTick_GetMs();
		}
		else
		{
			cloud_schedule_reconnect("[WIFI] Join AP failed");
		}
		break;

	case CLOUD_STATE_MQTT_USERCFG:
		if (ESP8266_Set_MQTT_UserCfg(MQTT_CLIENT_ID, MQTT_USERNAME, MQTT_PASSWORD))
		{
			s_cloud_state = CLOUD_STATE_MQTT_CONNCFG;
			s_next_cloud_action_ms = SysTick_GetMs();
		}
		else
		{
			cloud_schedule_reconnect("[MQTT] User config failed");
		}
		break;

	case CLOUD_STATE_MQTT_CONNCFG:
		if (ESP8266_Set_MQTT_ConnCfg(MQTT_KEEPALIVE_SECONDS))
		{
			s_cloud_state = CLOUD_STATE_MQTT_CONNECT;
			s_next_cloud_action_ms = SysTick_GetMs();
		}
		else
		{
			cloud_schedule_reconnect("[MQTT] Conn config failed");
		}
		break;

	case CLOUD_STATE_MQTT_CONNECT:
		printf("[MQTT] Connecting to broker...\r\n");
		if (ESP8266_Link_MQTT(MQTT_BROKER_HOST, MQTT_BROKER_PORT))
		{
			printf("[MQTT] Connected\r\n");
			s_cloud_state = CLOUD_STATE_MQTT_SUBSCRIBE;
			s_next_cloud_action_ms = SysTick_GetMs();
		}
		else
		{
			cloud_schedule_reconnect("[MQTT] Broker connect failed");
		}
		break;

	case CLOUD_STATE_MQTT_SUBSCRIBE:
		if (ESP8266_MQTT_Subscribe(MQTT_TOPIC_COMMAND, MQTT_SUB_QOS))
		{
			printf("[MQTT] Subscribed: %s\r\n", MQTT_TOPIC_COMMAND);
			s_cloud_state = CLOUD_STATE_PUBLISH_ONLINE;
			s_next_cloud_action_ms = SysTick_GetMs();
		}
		else
		{
			cloud_schedule_reconnect("[MQTT] Subscribe failed");
		}
		break;

	case CLOUD_STATE_PUBLISH_ONLINE:
		if (!cloud_build_online_json(payload, sizeof(payload)))
		{
			cloud_schedule_reconnect("[MQTT] Online payload build failed");
			break;
		}

		if (cloud_publish_json(MQTT_TOPIC_STATUS, payload))
		{
			printf("[MQTT] Online status published\r\n");
			s_cloud_state = CLOUD_STATE_READY;
		}
		else
		{
			cloud_schedule_reconnect("[MQTT] Online status publish failed");
		}
		break;

	default:
		break;
	}
}

bool CloudService_Init(void)
{
	s_next_report_ms = SysTick_GetMs() + TELEMETRY_INTERVAL_MS;
	s_cloud_initialized = true;

#if CLOUD_ENABLE
	if (!cloud_wifi_config_ready())
	{
		printf("[BOOT] Wi-Fi config incomplete, sensor-only mode active\r\n");
		s_cloud_state = CLOUD_STATE_LOCAL;
		return true;
	}

	if (!cloud_mqtt_config_ready())
	{
		printf("[BOOT] MQTT config incomplete, sensor-only mode active\r\n");
		s_cloud_state = CLOUD_STATE_LOCAL;
		return true;
	}

	WiFi_Config();
	printf("[WIFI] ESP8266 driver ready (USART3)\r\n");
	s_cloud_state = CLOUD_STATE_ESP_INIT;
	s_next_cloud_action_ms = SysTick_GetMs();
#else
	printf("[BOOT] Local sensor validation mode active\r\n");
	s_cloud_state = CLOUD_STATE_LOCAL;
#endif

	return true;
}

void CloudService_Process(void)
{
	u32 now;
	char topic[96];
	char payload[160];

	if (!s_cloud_initialized)
	{
		return;
	}

	cloud_report_telemetry_if_due();

#if !CLOUD_ENABLE
	return;
#endif

	now = SysTick_GetMs();

	if ((s_cloud_state == CLOUD_STATE_READY) && !ESP8266_IsConnected())
	{
		cloud_schedule_reconnect("[MQTT] Connection lost");
		return;
	}

	if (s_cloud_state == CLOUD_STATE_READY)
	{
		if (ESP8266_MQTT_PollMessage(topic, sizeof(topic), payload, sizeof(payload)))
		{
			cloud_handle_command(topic, payload);
		}
		return;
	}

	if (now < s_next_cloud_action_ms)
	{
		return;
	}

	cloud_run_connect_step();
}
