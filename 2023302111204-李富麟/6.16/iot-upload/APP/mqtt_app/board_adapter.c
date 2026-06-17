#include "board_adapter.h"
#include "led.h"
#include "adc_temp.h"
#include "iot_config.h"
#include "SysTick.h"
#include <stdio.h>

static board_env_data_t s_env_cache;
static bool s_has_valid_sample = false;
static u32 s_next_sample_ms = 0U;

static float board_generate_mock_humidity(float temperature, u8 light_percent)
{
    static u8 phase = 0U;
    float humidity;
    float light_effect;
    float temp_effect;

    phase = (u8)((phase + 1U) % 20U);
    light_effect = ((float)(100U - light_percent)) * 0.18f;
    temp_effect = (temperature - 25.0f) * 0.35f;
    humidity = 58.0f + light_effect - temp_effect + ((float)phase - 10.0f) * 0.6f;

    if (humidity < 35.0f)
    {
        humidity = 35.0f;
    }
    else if (humidity > 85.0f)
    {
        humidity = 85.0f;
    }

    return humidity;
}

static void board_apply_led(u8 on)
{
    if (on != 0U)
    {
        LED1 = 0;
    }
    else
    {
        LED1 = 1;
    }
}

static void board_buzzer_gpio_init(void)
{
    GPIO_InitTypeDef gpio;

    RCC_APB2PeriphClockCmd(BUZZER_GPIO_RCC, ENABLE);

    gpio.GPIO_Pin = BUZZER_PIN;
    gpio.GPIO_Mode = GPIO_Mode_Out_PP;
    gpio.GPIO_Speed = GPIO_Speed_50MHz;
    GPIO_Init(BUZZER_PORT, &gpio);
}

static void board_apply_buzzer(u8 on)
{
#if BUZZER_ACTIVE_LEVEL
    if (on != 0U)
    {
        GPIO_SetBits(BUZZER_PORT, BUZZER_PIN);
    }
    else
    {
        GPIO_ResetBits(BUZZER_PORT, BUZZER_PIN);
    }
#else
    if (on != 0U)
    {
        GPIO_ResetBits(BUZZER_PORT, BUZZER_PIN);
    }
    else
    {
        GPIO_SetBits(BUZZER_PORT, BUZZER_PIN);
    }
#endif
}

static bool board_sample_sensor(void)
{
    int temp_centi;
    u8 light_percent;
    float temperature;

    temp_centi = Get_Temperture();
    light_percent = Get_Light_Percent();
    temperature = ((float)temp_centi) / 100.0f;

    s_env_cache.temperature = temperature;
    s_env_cache.humidity = board_generate_mock_humidity(temperature, light_percent);
    s_env_cache.light = (float)light_percent;
    s_env_cache.sensor_valid = true;
    s_has_valid_sample = true;

    printf("[SENSOR] temp=%d.%02dC hum(sim)=%.1f%% light=%u%% led=%u\r\n",
           temp_centi / 100,
           (temp_centi >= 0 ? temp_centi : -temp_centi) % 100,
           s_env_cache.humidity,
           (unsigned int)light_percent,
           (unsigned int)BoardAdapter_GetLed());
    BoardAdapter_PrintStatusLine();
    return true;
}

void BoardAdapter_Init(void)
{
    s_env_cache.temperature = 0.0f;
    s_env_cache.humidity = 0.0f;
    s_env_cache.light = 0.0f;
    s_env_cache.led = 0U;
    s_env_cache.buzzer = 0U;
    s_env_cache.sensor_valid = false;
    s_has_valid_sample = false;
    s_next_sample_ms = 0U;

    ADC_Temp_Init();
    ADC_Light_Init();
    board_buzzer_gpio_init();
    printf("[SENSOR] ADC temp/light init done, light_pin=%s buzzer_pin=%s\r\n",
           LIGHT_SENSOR_PIN_DESC,
           BUZZER_PIN_DESC);

    BoardAdapter_SetLed(0U);
    BoardAdapter_SetBuzzer(0U);
    (void)BoardAdapter_ForceSample();
    s_next_sample_ms = SysTick_GetMs() + SENSOR_SAMPLE_PERIOD_MS;
}

void BoardAdapter_Process(void)
{
    u32 now;

    now = SysTick_GetMs();
    if ((s_next_sample_ms == 0U) || (now >= s_next_sample_ms))
    {
        (void)board_sample_sensor();
        s_next_sample_ms = SysTick_GetMs() + SENSOR_SAMPLE_PERIOD_MS;
    }
}

bool BoardAdapter_GetData(board_env_data_t *out)
{
    if (out == 0)
    {
        return false;
    }

    *out = s_env_cache;
    out->led = BoardAdapter_GetLed();
    out->buzzer = BoardAdapter_GetBuzzer();
    return s_env_cache.sensor_valid;
}

bool BoardAdapter_ForceSample(void)
{
    bool ok;

    ok = board_sample_sensor();
    s_next_sample_ms = SysTick_GetMs() + SENSOR_SAMPLE_PERIOD_MS;
    return ok;
}

void BoardAdapter_PrintStatusLine(void)
{
    board_env_data_t data;

    if (!BoardAdapter_GetData(&data))
    {
        return;
    }

    printf("TEMP:%.1f,LIGHT:%.0f,LED:%s,BUZZER:%s\r\n",
           data.temperature,
           data.light,
           (data.led != 0U) ? "ON" : "OFF",
           (data.buzzer != 0U) ? "ON" : "OFF");
}

void BoardAdapter_SetLed(u8 on)
{
    board_apply_led(on != 0U ? 1U : 0U);
    s_env_cache.led = on != 0U ? 1U : 0U;
}

u8 BoardAdapter_GetLed(void)
{
    return (LED1 == 0U) ? 1U : 0U;
}

void BoardAdapter_SetBuzzer(u8 on)
{
    board_apply_buzzer(on != 0U ? 1U : 0U);
    s_env_cache.buzzer = on != 0U ? 1U : 0U;
}

u8 BoardAdapter_GetBuzzer(void)
{
#if BUZZER_ACTIVE_LEVEL
    return (GPIO_ReadOutputDataBit(BUZZER_PORT, BUZZER_PIN) != Bit_RESET) ? 1U : 0U;
#else
    return (GPIO_ReadOutputDataBit(BUZZER_PORT, BUZZER_PIN) == Bit_RESET) ? 1U : 0U;
#endif
}
