#include "board_adapter.h"
#include "led.h"
#include "dht11.h"
#include "iot_config.h"
#include "SysTick.h"
#include <stdio.h>

static board_env_data_t s_env_cache;
static bool s_has_valid_sample = false;
static u32 s_next_sample_ms = 0U;

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

static bool board_sample_sensor(void)
{
#if SENSOR_USE_DHT11
    float temperature = 0.0f;
    float humidity = 0.0f;
    dht11_diag_t diag;

    if (DHT11_Read(&temperature, &humidity))
    {
        s_env_cache.temperature = temperature;
        s_env_cache.humidity = humidity;
        s_env_cache.sensor_valid = true;
        s_has_valid_sample = true;
        return true;
    }

    DHT11_GetDiag(&diag);
    printf("[SENSOR] DHT11 read failed, err=%u fail=%lu auto_reset=%lu\r\n",
           (unsigned int)diag.last_error,
           (unsigned long)diag.read_fail_count,
           (unsigned long)diag.auto_reset_count);

    if (s_has_valid_sample)
    {
        s_env_cache.sensor_valid = true;
        printf("[SENSOR] keep last valid sample: T/H cached\r\n");
    }
    else
    {
        s_env_cache.sensor_valid = false;
    }
    return false;
#else
    s_env_cache.sensor_valid = false;
    return false;
#endif
}

void BoardAdapter_Init(void)
{
    s_env_cache.temperature = 0.0f;
    s_env_cache.humidity = 0.0f;
    s_env_cache.led = 0U;
    s_env_cache.sensor_valid = false;
    s_has_valid_sample = false;
    s_next_sample_ms = 0U;

#if SENSOR_USE_DHT11
    DHT11_Init();
    printf("[SENSOR] DHT11 init done\r\n");
#else
    printf("[SENSOR] DHT11 disabled by config\r\n");
#endif

    BoardAdapter_SetLed(0U);
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
    return s_env_cache.sensor_valid;
}

bool BoardAdapter_ForceSample(void)
{
    bool ok;

    ok = board_sample_sensor();
    s_next_sample_ms = SysTick_GetMs() + SENSOR_SAMPLE_PERIOD_MS;
    return ok;
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
