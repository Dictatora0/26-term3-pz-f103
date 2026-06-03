#include "bsp_sensor.h"
#include "adc_temp.h"
#include "SysTick.h"
#include "cloud_config.h"
#include <stdio.h>
#include <string.h>

#if LCD_ENABLE
#include "tftlcd.h"
#endif

static SensorData s_latest_data;
static bool s_sensor_initialized = false;
static bool s_lcd_ready = false;
static u32 s_next_sample_ms = 0U;

static void sensor_clear_data(SensorData *data)
{
	memset(data, 0, sizeof(SensorData));
}

static void sensor_format_temperature_1dp(float temperature, char *buf, u16 buf_size)
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

#if LCD_ENABLE
static void sensor_lcd_show_line(u16 y, const char *text, u16 color)
{
	FRONT_COLOR = color;
	BACK_COLOR = WHITE;
	LCD_Fill(0, y, tftlcd_data.width - 1U, y + 18U, WHITE);
	LCD_ShowString(10U, y, tftlcd_data.width - 20U, 18U, 16U, (u8 *)text);
}

static void sensor_lcd_refresh(const SensorData *data)
{
	char line[64];
	char temp_buf[20];

	if (!s_lcd_ready || (data == 0))
	{
		return;
	}

	sensor_lcd_show_line(10U, "DEVICE SENSOR DASHBOARD", BLACK);

	if (data->temperature_valid)
	{
		sensor_format_temperature_1dp(data->temperature, temp_buf, sizeof(temp_buf));
		snprintf(line, sizeof(line), "TEMP : %s C", temp_buf);
	}
	else
	{
		snprintf(line, sizeof(line), "TEMP : ERR");
	}
	sensor_lcd_show_line(40U, line, data->temperature_valid ? BLUE : RED);

	if (data->light_valid)
	{
		snprintf(line, sizeof(line), "LIGHT: %lu", (unsigned long)data->light);
	}
	else
	{
		snprintf(line, sizeof(line), "LIGHT: ERR");
	}
	sensor_lcd_show_line(70U, line, data->light_valid ? BLUE : RED);

	snprintf(line, sizeof(line), "UPTIME: %lus", (unsigned long)data->uptime);
	sensor_lcd_show_line(100U, line, BLACK);

#if CLOUD_ENABLE
	sensor_lcd_show_line(130U, "MODE : CLOUD", GREEN);
#else
	sensor_lcd_show_line(130U, "MODE : LOCAL", GREEN);
#endif
	sensor_lcd_show_line(160U, "LED CMD: MQTT /command", BLACK);
}
#endif

static void sensor_update_cache(void)
{
	int temp_centi;
	u16 light_raw;
	char temp_buf[20];

	sensor_clear_data(&s_latest_data);
	s_latest_data.uptime = SysTick_GetSeconds();

	temp_centi = Get_Temperture();
	if (temp_centi == ADC_TEMP_INVALID_CENTI)
	{
		s_latest_data.temperature_valid = false;
		printf("[ERROR] Temperature read failed\r\n");
	}
	else
	{
		s_latest_data.temperature_valid = true;
		s_latest_data.temperature = ((float)temp_centi) / 100.0f;
	}

	light_raw = Get_Light_Raw();
	if (light_raw == ADC_SENSOR_TIMEOUT_VALUE)
	{
		s_latest_data.light_valid = false;
		printf("[ERROR] Light read failed\r\n");
	}
	else
	{
		s_latest_data.light_valid = true;
		s_latest_data.light = light_raw;
	}

	if (s_latest_data.temperature_valid)
	{
		sensor_format_temperature_1dp(s_latest_data.temperature, temp_buf, sizeof(temp_buf));
	}
	else
	{
		strcpy(temp_buf, "null");
	}

	if (s_latest_data.light_valid)
	{
		printf("[SENSOR] temperature=%s, light=%lu\r\n",
		       temp_buf,
		       (unsigned long)s_latest_data.light);
	}
	else
	{
		printf("[SENSOR] temperature=%s, light=null\r\n", temp_buf);
	}

#if LCD_ENABLE
	sensor_lcd_refresh(&s_latest_data);
#endif
}

bool Sensor_Init(void)
{
	ADC_Temp_Init();
	printf("[SENSOR] Temperature driver ready\r\n");
	ADC_Light_Init();
	printf("[SENSOR] Light driver ready\r\n");

#if LCD_ENABLE
	TFTLCD_Init();
	LCD_Clear(WHITE);
	s_lcd_ready = true;
	printf("[LCD] Display ready\r\n");
#else
	s_lcd_ready = false;
#endif

	s_sensor_initialized = true;
	s_next_sample_ms = 0U;
	sensor_update_cache();
	s_next_sample_ms = SysTick_GetMs() + SENSOR_SAMPLE_INTERVAL_MS;
	return true;
}

bool Sensor_Read(SensorData *data)
{
	if ((!s_sensor_initialized) || (data == 0))
	{
		return false;
	}

	*data = s_latest_data;
	data->uptime = SysTick_GetSeconds();
	return true;
}

void Sensor_Process(void)
{
	u32 now;

	if (!s_sensor_initialized)
	{
		return;
	}

	now = SysTick_GetMs();
	if ((s_next_sample_ms == 0U) || (now >= s_next_sample_ms))
	{
		sensor_update_cache();
		s_next_sample_ms = now + SENSOR_SAMPLE_INTERVAL_MS;
	}
}
