#include "dht11.h"
#include "SysTick.h"
#include "iot_config.h"
#include <string.h>

static dht11_diag_t s_dht11_diag;

static void dht11_auto_recover_if_needed(void);

static void dht11_set_error(u8 err)
{
    s_dht11_diag.last_error = err;
}

static void dht11_record_fail(void)
{
    s_dht11_diag.read_fail_count++;
    s_dht11_diag.consecutive_fail_count++;
    dht11_auto_recover_if_needed();
}

static void dht11_record_ok(void)
{
    s_dht11_diag.read_ok_count++;
    s_dht11_diag.consecutive_fail_count = 0U;
    s_dht11_diag.last_error = DHT11_ERR_NONE;
}

static void dht11_set_output(void)
{
    GPIO_InitTypeDef gpio;

    gpio.GPIO_Pin = DHT11_GPIO_PIN;
    gpio.GPIO_Speed = GPIO_Speed_50MHz;
    gpio.GPIO_Mode = GPIO_Mode_Out_PP;
    GPIO_Init(DHT11_GPIO_PORT, &gpio);
}

static void dht11_set_input(void)
{
    GPIO_InitTypeDef gpio;

    gpio.GPIO_Pin = DHT11_GPIO_PIN;
    gpio.GPIO_Speed = GPIO_Speed_50MHz;
    gpio.GPIO_Mode = GPIO_Mode_IPU;
    GPIO_Init(DHT11_GPIO_PORT, &gpio);
}

static u8 dht11_read_pin(void)
{
    return (GPIO_ReadInputDataBit(DHT11_GPIO_PORT, DHT11_GPIO_PIN) != Bit_RESET) ? 1U : 0U;
}

static bool dht11_wait_level(u8 level, u16 timeout_us)
{
    while (timeout_us-- != 0U)
    {
        if (dht11_read_pin() == level)
        {
            return true;
        }
        delay_us(1U);
    }
    return false;
}

static bool dht11_read_byte(u8 *out)
{
    u8 i;
    u8 data = 0U;
    u16 high_time;

    for (i = 0U; i < 8U; i++)
    {
        if (!dht11_wait_level(0U, 80U))
        {
            return false;
        }
        if (!dht11_wait_level(1U, 80U))
        {
            return false;
        }

        high_time = 0U;
        while (dht11_read_pin() != 0U)
        {
            if (++high_time > 120U)
            {
                dht11_set_error(DHT11_ERR_BIT_TIMEOUT);
                return false;
            }
            delay_us(1U);
        }

        data <<= 1;
        if (high_time > 40U)
        {
            data |= 0x01U;
        }
    }

    *out = data;
    return true;
}

void DHT11_Init(void)
{
    RCC_APB2PeriphClockCmd(DHT11_GPIO_RCC, ENABLE);
    dht11_set_output();
    GPIO_SetBits(DHT11_GPIO_PORT, DHT11_GPIO_PIN);
    DHT11_ClearDiag();
}

void DHT11_ResetPin(void)
{
    dht11_set_output();
    GPIO_SetBits(DHT11_GPIO_PORT, DHT11_GPIO_PIN);
    delay_ms(2U);
    dht11_set_input();
    s_dht11_diag.auto_reset_count++;
}

static void dht11_auto_recover_if_needed(void)
{
    if (s_dht11_diag.consecutive_fail_count >= DHT11_AUTO_RESET_FAIL_THRESHOLD)
    {
        DHT11_ResetPin();
        s_dht11_diag.consecutive_fail_count = 0U;
    }
}

bool DHT11_Read(float *temperature_c, float *humidity_rh)
{
    u8 hum_i;
    u8 hum_d;
    u8 temp_i;
    u8 temp_d;
    u8 sum;
    u8 calc_sum;

    if ((temperature_c == 0) || (humidity_rh == 0))
    {
        dht11_set_error(DHT11_ERR_PARAM);
        dht11_record_fail();
        return false;
    }

    dht11_set_input();
    delay_us(5U);
    if (!dht11_wait_level(1U, 200U))
    {
        dht11_set_error(DHT11_ERR_RESP_HIGH);
        dht11_record_fail();
        return false;
    }

    dht11_set_output();
    GPIO_ResetBits(DHT11_GPIO_PORT, DHT11_GPIO_PIN);
    delay_ms(20U);
    GPIO_SetBits(DHT11_GPIO_PORT, DHT11_GPIO_PIN);
    delay_us(40U);
    dht11_set_input();
    delay_us(10U);

    if (!dht11_wait_level(0U, 120U))
    {
        dht11_set_error(DHT11_ERR_RESP_LOW);
        dht11_record_fail();
        return false;
    }
    if (!dht11_wait_level(1U, 120U))
    {
        dht11_set_error(DHT11_ERR_RESP_HIGH);
        dht11_record_fail();
        return false;
    }
    if (!dht11_wait_level(0U, 120U))
    {
        dht11_set_error(DHT11_ERR_RESP_DATA);
        dht11_record_fail();
        return false;
    }

    if (!dht11_read_byte(&hum_i))
    {
        dht11_record_fail();
        return false;
    }
    if (!dht11_read_byte(&hum_d))
    {
        dht11_record_fail();
        return false;
    }
    if (!dht11_read_byte(&temp_i))
    {
        dht11_record_fail();
        return false;
    }
    if (!dht11_read_byte(&temp_d))
    {
        dht11_record_fail();
        return false;
    }
    if (!dht11_read_byte(&sum))
    {
        dht11_record_fail();
        return false;
    }

    s_dht11_diag.last_raw[0] = hum_i;
    s_dht11_diag.last_raw[1] = hum_d;
    s_dht11_diag.last_raw[2] = temp_i;
    s_dht11_diag.last_raw[3] = temp_d;
    s_dht11_diag.last_raw[4] = sum;

    calc_sum = (u8)(hum_i + hum_d + temp_i + temp_d);
    if (calc_sum != sum)
    {
        dht11_set_error(DHT11_ERR_CHECKSUM);
        dht11_record_fail();
        return false;
    }

    *humidity_rh = (float)hum_i + ((float)hum_d * 0.1f);
    *temperature_c = (float)temp_i + ((float)temp_d * 0.1f);
    dht11_record_ok();
    return true;
}

void DHT11_GetDiag(dht11_diag_t *diag)
{
    if (diag == 0)
    {
        return;
    }
    *diag = s_dht11_diag;
}

void DHT11_ClearDiag(void)
{
    memset(&s_dht11_diag, 0, sizeof(s_dht11_diag));
}
