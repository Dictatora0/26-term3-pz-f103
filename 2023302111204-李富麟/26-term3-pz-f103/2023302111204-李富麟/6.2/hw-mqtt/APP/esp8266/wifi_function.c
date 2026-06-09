#include "wifi_function.h"
#include "cloud_config.h"
#include "SysTick.h"
#include <string.h>
#include <stdio.h>

#define ESP8266_REPLY_POLL_MS          10U
#define ESP8266_MAX_AT_LOG_LEN         80U
#define ESP8266_PUBLISH_PROMPT_TIMEOUT 2000U
#define ESP8266_PUBLISH_DONE_TIMEOUT   6000U

static u32 s_esp_cmd_seq = 0U;
static u32 s_selected_baud = 0U;
static bool s_driver_initialized = false;
static bool s_wifi_connected = false;
static bool s_tcp_connected = false;
static bool s_mqtt_ready = false;
static bool s_async_frame_ready = false;
static char s_async_frame[RX_BUF_MAX_LEN];

static char esp_ascii_upper(char c)
{
	if ((c >= 'a') && (c <= 'z'))
	{
		return (char)(c - ('a' - 'A'));
	}

	return c;
}

static bool esp_has_text(const char *s)
{
	return (s != 0) && (s[0] != '\0');
}

static bool esp_str_contains_icase(const char *src, const char *token)
{
	const char *scan;
	const char *s;
	const char *t;

	if ((src == 0) || (token == 0) || (*token == '\0'))
	{
		return false;
	}

	for (scan = src; *scan != '\0'; ++scan)
	{
		s = scan;
		t = token;
		while ((*s != '\0') && (*t != '\0'))
		{
			if (esp_ascii_upper(*s) != esp_ascii_upper(*t))
			{
				break;
			}
			++s;
			++t;
		}

		if (*t == '\0')
		{
			return true;
		}
	}

	return false;
}

static bool esp_is_sensitive_command(const char *cmd)
{
	if (cmd == 0)
	{
		return false;
	}

	if (strncmp(cmd, "AT+CWJAP=", 9) == 0)
	{
		return true;
	}

	if (strncmp(cmd, "AT+MQTTUSERCFG=", 15) == 0)
	{
		return true;
	}

	return false;
}

static void esp_log_command(u32 cmd_id, const char *cmd)
{
	char preview[ESP8266_MAX_AT_LOG_LEN];
	u16 copy_len;

	if (cmd == 0)
	{
		return;
	}

	if (esp_is_sensitive_command(cmd))
	{
		if (strncmp(cmd, "AT+CWJAP=", 9) == 0)
		{
			PC_Usart("[ESP_CMD %lu] TX: AT+CWJAP=<hidden>\r\n", (unsigned long)cmd_id);
		}
		else
		{
			PC_Usart("[ESP_CMD %lu] TX: AT+MQTTUSERCFG=<hidden>\r\n", (unsigned long)cmd_id);
		}
		return;
	}

	copy_len = (u16)strlen(cmd);
	if (copy_len >= (u16)sizeof(preview))
	{
		copy_len = (u16)sizeof(preview) - 1U;
	}
	memcpy(preview, cmd, copy_len);
	preview[copy_len] = '\0';
	PC_Usart("[ESP_CMD %lu] TX: %s\r\n", (unsigned long)cmd_id, preview);
}

static void esp_copy_current_frame(char *dst, u16 dst_size)
{
	u16 len;

	if ((dst == 0) || (dst_size < 2U))
	{
		return;
	}

	len = strEsp8266_Fram_Record.InfBit.FramLength;
	if (len >= (dst_size - 1U))
	{
		len = dst_size - 1U;
	}

	memcpy(dst, strEsp8266_Fram_Record.Data_RX_BUF, len);
	dst[len] = '\0';
}

static bool esp_wait_reply(u32 cmd_id, const char *reply1, const char *reply2, u32 waittime, bool sensitive)
{
	u32 elapsed = 0U;
	char frame[RX_BUF_MAX_LEN];

	while (elapsed < waittime)
	{
		delay_ms(ESP8266_REPLY_POLL_MS);
		elapsed += ESP8266_REPLY_POLL_MS;

		if (g_esp8266_rx_overflow != 0U)
		{
			g_esp8266_rx_overflow = 0U;
			PC_Usart("[ERROR] ESP8266 RX buffer overflow\r\n");
		}

		if (strEsp8266_Fram_Record.InfBit.FramFinishFlag == 0U)
		{
			continue;
		}

		esp_copy_current_frame(frame, sizeof(frame));
		ESP8266_ClearRxBuffer();

		if (!sensitive)
		{
			PC_Usart("[ESP_CMD %lu] RX(%lu ms): %s\r\n",
			         (unsigned long)cmd_id,
			         (unsigned long)elapsed,
			         frame);
		}
		else
		{
			PC_Usart("[ESP_CMD %lu] RX(%lu ms,len=%u)\r\n",
			         (unsigned long)cmd_id,
			         (unsigned long)elapsed,
			         (unsigned int)strlen(frame));
		}

		if (((reply1 != 0) && (strstr(frame, reply1) != 0)) ||
		    ((reply2 != 0) && (strstr(frame, reply2) != 0)) ||
		    ((reply1 != 0) && esp_str_contains_icase(frame, reply1)) ||
		    ((reply2 != 0) && esp_str_contains_icase(frame, reply2)))
		{
			return true;
		}

		if (esp_str_contains_icase(frame, "ERROR") ||
		    esp_str_contains_icase(frame, "FAIL"))
		{
			return false;
		}
	}

	PC_Usart("[ERROR] ESP8266 timeout\r\n");
	return false;
}

static void esp_send_raw_line(const char *cmd)
{
	if (cmd == 0)
	{
		return;
	}

	while (*cmd != '\0')
	{
		USART_SendData(USART3, (u8)*cmd++);
		while (USART_GetFlagStatus(USART3, USART_FLAG_TXE) == RESET)
		{
		}
	}

	USART_SendData(USART3, (u8)'\r');
	while (USART_GetFlagStatus(USART3, USART_FLAG_TXE) == RESET)
	{
	}
	USART_SendData(USART3, (u8)'\n');
	while (USART_GetFlagStatus(USART3, USART_FLAG_TXE) == RESET)
	{
	}
}

static void esp_send_raw_bytes(const u8 *data, u16 length)
{
	u16 index;

	if ((data == 0) || (length == 0U))
	{
		return;
	}

	for (index = 0U; index < length; ++index)
	{
		USART_SendData(USART3, data[index]);
		while (USART_GetFlagStatus(USART3, USART_FLAG_TXE) == RESET)
		{
		}
	}
}

static bool esp_probe_baud(u32 baud)
{
	WiFi_SetUsart3Baud(baud);
	return ESP8266_Cmd("AT", "OK", 0, ESP8266_AT_TIMEOUT_MS);
}

static void esp_mark_disconnected(void)
{
	s_wifi_connected = false;
	s_tcp_connected = false;
	s_mqtt_ready = false;
}

void ESP8266_ClearRxBuffer(void)
{
	strEsp8266_Fram_Record.InfAll = 0U;
	strEsp8266_Fram_Record.Data_RX_BUF[0] = '\0';
}

void ESP8266_Process(void)
{
	if (!s_driver_initialized)
	{
		return;
	}

	if (g_esp8266_rx_overflow != 0U)
	{
		g_esp8266_rx_overflow = 0U;
		PC_Usart("[ERROR] ESP8266 RX buffer overflow\r\n");
	}

	if (strEsp8266_Fram_Record.InfBit.FramFinishFlag == 0U)
	{
		return;
	}

	esp_copy_current_frame(s_async_frame, sizeof(s_async_frame));
	s_async_frame_ready = true;
	ESP8266_ClearRxBuffer();

	if (esp_str_contains_icase(s_async_frame, "WIFI DISCONNECT") ||
	    esp_str_contains_icase(s_async_frame, "CLOSED") ||
	    esp_str_contains_icase(s_async_frame, "+MQTTDISCONNECTED:"))
	{
		esp_mark_disconnected();
	}
}

void ESP8266_Choose(FunctionalState enumChoose)
{
	if (enumChoose == ENABLE)
	{
		ESP8266_CH_HIGH_LEVEL();
	}
	else
	{
		ESP8266_CH_LOW_LEVEL();
	}
}

void ESP8266_Rst(void)
{
	ESP8266_RST_LOW_LEVEL();
	delay_ms(200U);
	ESP8266_RST_HIGH_LEVEL();
	delay_ms(800U);
	ESP8266_ClearRxBuffer();
	s_async_frame_ready = false;
}

bool ESP8266_Init(void)
{
	static const u32 baud_candidates[] = {9600U, 115200U, 74880U, 57600U};
	u8 index;

	ESP8266_Choose(ENABLE);
	ESP8266_Rst();
	s_driver_initialized = false;
	s_selected_baud = 0U;
	esp_mark_disconnected();

	for (index = 0U; index < (u8)(sizeof(baud_candidates) / sizeof(baud_candidates[0])); ++index)
	{
		if (esp_probe_baud(baud_candidates[index]))
		{
			s_selected_baud = baud_candidates[index];
			s_driver_initialized = true;
			break;
		}
	}

	if (!s_driver_initialized)
	{
		PC_Usart("[ERROR] ESP8266 no AT response on 9600/115200/74880/57600\r\n");
		return false;
	}

	PC_Usart("[WIFI] ESP8266 detected\r\n");
	PC_Usart("[WIFI] ESP8266 baud=%lu\r\n", (unsigned long)s_selected_baud);
	if (!ESP8266_Cmd("ATE0", "OK", 0, 600U))
	{
		PC_Usart("[WIFI] ATE0 failed, continue\r\n");
	}

	return true;
}

void ESP8266_AT_Test(void)
{
	(void)ESP8266_Cmd("AT", "OK", 0, ESP8266_AT_TIMEOUT_MS);
}

bool ESP8266_Cmd(const char *cmd, const char *reply1, const char *reply2, u32 waittime)
{
	u32 cmd_id;
	bool sensitive;

	if (cmd == 0)
	{
		return false;
	}

	cmd_id = ++s_esp_cmd_seq;
	sensitive = esp_is_sensitive_command(cmd);
	ESP8266_ClearRxBuffer();
	esp_log_command(cmd_id, cmd);
	esp_send_raw_line(cmd);

	if ((reply1 == 0) && (reply2 == 0))
	{
		return true;
	}

	return esp_wait_reply(cmd_id, reply1, reply2, waittime, sensitive);
}

bool ESP8266_Net_Mode_Choose(ENUM_Net_ModeTypeDef enumMode)
{
	switch (enumMode)
	{
	case STA:
		return ESP8266_Cmd("AT+CWMODE=1", "OK", "no change", 1000U);

	case AP:
		return ESP8266_Cmd("AT+CWMODE=2", "OK", "no change", 1000U);

	case STA_AP:
		return ESP8266_Cmd("AT+CWMODE=3", "OK", "no change", 1000U);

	default:
		return false;
	}
}

bool ESP8266_JoinAP(const char *pSSID, const char *pPassWord)
{
	char cCmd[160];
	int n;

	if (!esp_has_text(pSSID) || (pPassWord == 0))
	{
		return false;
	}

	if (!ESP8266_Net_Mode_Choose(STA))
	{
		return false;
	}

	n = snprintf(cCmd, sizeof(cCmd), "AT+CWJAP=\"%s\",\"%s\"", pSSID, pPassWord);
	if ((n <= 0) || (n >= (int)sizeof(cCmd)))
	{
		return false;
	}

	if (!ESP8266_Cmd(cCmd, "WIFI GOT IP", "OK", ESP8266_JOIN_TIMEOUT_MS))
	{
		s_wifi_connected = false;
		return false;
	}

	s_wifi_connected = true;
	return true;
}

bool ESP8266_Enable_MultipleId(FunctionalState enumEnUnvarnishTx)
{
	char cCmd[32];
	int n;

	n = snprintf(cCmd, sizeof(cCmd), "AT+CIPMUX=%d", enumEnUnvarnishTx ? 1 : 0);
	if ((n <= 0) || (n >= (int)sizeof(cCmd)))
	{
		return false;
	}

	return ESP8266_Cmd(cCmd, "OK", "link is builded", 1000U);
}

bool ESP8266_OpenTCP(const char *host, u16 port)
{
	char cCmd[160];
	int n;

	if (!esp_has_text(host))
	{
		return false;
	}

	if (!ESP8266_Enable_MultipleId(DISABLE))
	{
		return false;
	}

	n = snprintf(cCmd, sizeof(cCmd), "AT+CIPSTART=\"TCP\",\"%s\",%u", host, (unsigned int)port);
	if ((n <= 0) || (n >= (int)sizeof(cCmd)))
	{
		return false;
	}

	if (!ESP8266_Cmd(cCmd, "CONNECT", "OK", ESP8266_MQTT_CONN_TIMEOUT_MS))
	{
		s_tcp_connected = false;
		return false;
	}

	s_tcp_connected = true;
	return true;
}

bool ESP8266_Send(const u8 *data, u16 length)
{
	char cCmd[32];
	int n;
	u32 cmd_id;

	if ((data == 0) || (length == 0U))
	{
		return false;
	}

	n = snprintf(cCmd, sizeof(cCmd), "AT+CIPSEND=%u", (unsigned int)length);
	if ((n <= 0) || (n >= (int)sizeof(cCmd)))
	{
		return false;
	}

	cmd_id = ++s_esp_cmd_seq;
	ESP8266_ClearRxBuffer();
	esp_log_command(cmd_id, cCmd);
	esp_send_raw_line(cCmd);

	if (!esp_wait_reply(cmd_id, ">", 0, ESP8266_PUBLISH_PROMPT_TIMEOUT, false))
	{
		return false;
	}

	esp_send_raw_bytes(data, length);
	return esp_wait_reply(cmd_id, "SEND OK", "OK", ESP8266_PUBLISH_DONE_TIMEOUT, false);
}

bool ESP8266_Link_MQTT(const char *host, u16 port)
{
	char cCmd[160];
	int n;

	if (!esp_has_text(host))
	{
		return false;
	}

	(void)ESP8266_Cmd("AT+MQTTCLEAN=0", "OK", "ERR", 1500U);
	n = snprintf(cCmd, sizeof(cCmd), "AT+MQTTCONN=0,\"%s\",%u,0", host, (unsigned int)port);
	if ((n <= 0) || (n >= (int)sizeof(cCmd)))
	{
		return false;
	}

	if (!ESP8266_Cmd(cCmd, "+MQTTCONNECTED", "OK", ESP8266_MQTT_CONN_TIMEOUT_MS))
	{
		s_mqtt_ready = false;
		return false;
	}

	s_tcp_connected = true;
	s_mqtt_ready = true;
	return true;
}

bool ESP8266_Set_MQTT_UserCfg(const char *client_id, const char *username, const char *password)
{
	char cCmd[200];
	int n;

	if (!esp_has_text(client_id) || (username == 0) || (password == 0))
	{
		return false;
	}

	n = snprintf(cCmd,
	             sizeof(cCmd),
	             "AT+MQTTUSERCFG=0,1,\"%s\",\"%s\",\"%s\",0,0,\"\"",
	             client_id,
	             username,
	             password);
	if ((n <= 0) || (n >= (int)sizeof(cCmd)))
	{
		return false;
	}

	return ESP8266_Cmd(cCmd, "OK", "ALREAY CONNECT", 1500U);
}

bool ESP8266_Set_MQTT_ConnCfg(u16 keepalive_seconds)
{
	char cCmd[80];
	int n;

	n = snprintf(cCmd,
	             sizeof(cCmd),
	             "AT+MQTTCONNCFG=0,%u,0,\"\",\"\",0,0",
	             (unsigned int)keepalive_seconds);
	if ((n <= 0) || (n >= (int)sizeof(cCmd)))
	{
		return false;
	}

	return ESP8266_Cmd(cCmd, "OK", 0, 1000U);
}

bool ESP8266_MQTT_Publish(const char *topicId, const char *val)
{
	char cCmd[180];
	u16 payload_len;
	int n;
	u32 cmd_id;

	if (!esp_has_text(topicId) || (val == 0))
	{
		return false;
	}

	payload_len = (u16)strlen(val);
	n = snprintf(cCmd,
	             sizeof(cCmd),
	             "AT+MQTTPUBRAW=0,\"%s\",%u,0,0",
	             topicId,
	             (unsigned int)payload_len);
	if ((n <= 0) || (n >= (int)sizeof(cCmd)))
	{
		return false;
	}

	cmd_id = ++s_esp_cmd_seq;
	ESP8266_ClearRxBuffer();
	esp_log_command(cmd_id, cCmd);
	esp_send_raw_line(cCmd);

	if (!esp_wait_reply(cmd_id, ">", 0, ESP8266_PUBLISH_PROMPT_TIMEOUT, false))
	{
		return false;
	}

	esp_send_raw_bytes((const u8 *)val, payload_len);
	return esp_wait_reply(cmd_id, "+MQTTPUB:OK", "OK", ESP8266_PUBLISH_DONE_TIMEOUT, false);
}

bool ESP8266_MQTT_Subscribe(const char *topicId, u8 qos)
{
	char cCmd[160];
	int n;

	if (!esp_has_text(topicId))
	{
		return false;
	}

	n = snprintf(cCmd, sizeof(cCmd), "AT+MQTTSUB=0,\"%s\",%u", topicId, (unsigned int)qos);
	if ((n <= 0) || (n >= (int)sizeof(cCmd)))
	{
		return false;
	}

	return ESP8266_Cmd(cCmd, "OK", "ALREADY SUBSCRIBE", 1500U);
}

bool ESP8266_IsConnected(void)
{
	return (s_driver_initialized && s_wifi_connected && s_mqtt_ready);
}

u32 ESP8266_GetSelectedBaud(void)
{
	return s_selected_baud;
}

bool ESP8266_Is_MQTT_Ready(void)
{
	return s_mqtt_ready;
}

bool ESP8266_MQTT_ParseSubFrame(const char *src, char *topic, u16 topic_size, char *payload, u16 payload_size)
{
	const char *topic_begin;
	const char *topic_end;
	const char *comma_after_topic;
	const char *comma_after_length;
	const char *payload_begin;
	const char *payload_end;
	u16 topic_copy_len;
	u16 payload_copy_len;

	if ((src == 0) || (topic == 0) || (payload == 0) || (topic_size < 2U) || (payload_size < 2U))
	{
		return false;
	}

	topic[0] = '\0';
	payload[0] = '\0';

	topic_begin = strchr(src, '"');
	if (topic_begin == 0)
	{
		return false;
	}
	topic_begin++;

	topic_end = strchr(topic_begin, '"');
	if (topic_end == 0)
	{
		return false;
	}

	comma_after_topic = strchr(topic_end, ',');
	if (comma_after_topic == 0)
	{
		return false;
	}

	comma_after_length = strchr(comma_after_topic + 1, ',');
	if (comma_after_length == 0)
	{
		return false;
	}

	topic_copy_len = (u16)(topic_end - topic_begin);
	if (topic_copy_len >= topic_size)
	{
		topic_copy_len = topic_size - 1U;
	}
	memcpy(topic, topic_begin, topic_copy_len);
	topic[topic_copy_len] = '\0';

	payload_begin = comma_after_length + 1;
	while ((*payload_begin == ' ') || (*payload_begin == '\t'))
	{
		payload_begin++;
	}

	payload_end = payload_begin;
	while ((*payload_end != '\0') && (*payload_end != '\r') && (*payload_end != '\n'))
	{
		payload_end++;
	}

	while ((payload_end > payload_begin) &&
	       ((*(payload_end - 1) == ' ') || (*(payload_end - 1) == '\t')))
	{
		payload_end--;
	}

	if ((*payload_begin == '"') && (payload_end > payload_begin) && (*(payload_end - 1) == '"'))
	{
		payload_begin++;
		payload_end--;
	}

	payload_copy_len = (u16)(payload_end - payload_begin);
	if (payload_copy_len >= payload_size)
	{
		payload_copy_len = payload_size - 1U;
	}

	memcpy(payload, payload_begin, payload_copy_len);
	payload[payload_copy_len] = '\0';
	return true;
}

bool ESP8266_MQTT_PollMessage(char *topic, u16 topic_size, char *payload, u16 payload_size)
{
	if ((topic == 0) || (payload == 0) || (topic_size < 2U) || (payload_size < 2U))
	{
		return false;
	}

	if (!s_async_frame_ready)
	{
		return false;
	}

	s_async_frame_ready = false;
	if (!esp_str_contains_icase(s_async_frame, "+MQTTSUBRECV:"))
	{
		return false;
	}

	return ESP8266_MQTT_ParseSubFrame(s_async_frame, topic, topic_size, payload, payload_size);
}
