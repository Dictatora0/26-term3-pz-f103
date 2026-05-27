#include "dht11.h"
#include "SysTick.h"
#include "esp8266_mqtt_config.h"
#include <string.h>

/* DHT11 single wire pin config (default PA8). */
#ifndef DHT11_GPIO_PORT
#define DHT11_GPIO_PORT GPIOA
#endif

#ifndef DHT11_GPIO_PIN
#define DHT11_GPIO_PIN GPIO_Pin_8
#endif

#ifndef DHT11_GPIO_RCC
#define DHT11_GPIO_RCC RCC_APB2Periph_GPIOA
#endif

#ifndef DHT11_AUTO_RESET_FAIL_THRESHOLD
#define DHT11_AUTO_RESET_FAIL_THRESHOLD 5U
#endif

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
    s_dht11_diag.consecutive_fail_count = 0;
    s_dht11_diag.last_error = DHT11_ERR_NONE;
}

static void dht11_set_output(void)
{
    GPIO_InitTypeDef gpio;
    gpio.GPIO_Pin = DHT11_GPIO_PIN;
    gpio.GPIO_Speed = GPIO_Speed_50MHz;
    gpio.GPIO_Mode = GPIO_Mode_Out_OD;
    GPIO_Init(DHT11_GPIO_PORT, &gpio);
}

static void dht11_set_input(void)
{
    GPIO_InitTypeDef gpio;
    gpio.GPIO_Pin = DHT11_GPIO_PIN;
    gpio.GPIO_Speed = GPIO_Speed_50MHz;
    gpio.GPIO_Mode = GPIO_Mode_IN_FLOATING;
    GPIO_Init(DHT11_GPIO_PORT, &gpio);
}

static u8 dht11_read_pin(void)
{
    return (GPIO_ReadInputDataBit(DHT11_GPIO_PORT, DHT11_GPIO_PIN) != Bit_RESET) ? 1U : 0U;
}

static bool dht11_wait_level(u8 level, u16 timeout_us)
{
    while (timeout_us--) {
        if (dht11_read_pin() == level) {
            return true;
        }
        delay_us(1);
    }
    return false;
}

static bool dht11_read_byte(u8 *out)
{
    u8 i;
    u8 data = 0;
    u16 high_time;

    for (i = 0; i < 8; i++) {
        /* Each bit starts with ~50us low. */
        if (!dht11_wait_level(0, 80)) {
            return false;
        }
        if (!dht11_wait_level(1, 80)) {
            return false;
        }

        high_time = 0;
        while (dht11_read_pin()) {
            if (++high_time > 120) {
                dht11_set_error(DHT11_ERR_BIT_TIMEOUT);
                return false;
            }
            delay_us(1);
        }

        data <<= 1;
        if (high_time > 40) {
            data |= 0x01; /* ~70us high => bit 1 */
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
    delay_ms(2);
    dht11_set_input();
    s_dht11_diag.auto_reset_count++;
}

static void dht11_auto_recover_if_needed(void)
{
    if (s_dht11_diag.consecutive_fail_count >= DHT11_AUTO_RESET_FAIL_THRESHOLD) {
        DHT11_ResetPin();
        s_dht11_diag.consecutive_fail_count = 0;
    }
}

bool DHT11_Read(float *temperature_c, float *humidity_rh)
{
    u8 hum_i, hum_d, temp_i, temp_d, sum;
    u8 calc_sum;

    if ((temperature_c == 0) || (humidity_rh == 0)) {
        dht11_set_error(DHT11_ERR_PARAM);
        dht11_record_fail();
        return false;
    }

    /* Start signal: host pulls low >=18ms, then releases. */
    dht11_set_output();
    GPIO_ResetBits(DHT11_GPIO_PORT, DHT11_GPIO_PIN);
    delay_ms(20);
    GPIO_SetBits(DHT11_GPIO_PORT, DHT11_GPIO_PIN);
    delay_us(30);
    dht11_set_input();

    /* Sensor response: 80us low + 80us high. */
    if (!dht11_wait_level(0, 120)) {
        dht11_set_error(DHT11_ERR_RESP_LOW);
        dht11_record_fail();
        return false;
    }
    if (!dht11_wait_level(1, 120)) {
        dht11_set_error(DHT11_ERR_RESP_HIGH);
        dht11_record_fail();
        return false;
    }
    if (!dht11_wait_level(0, 120)) {
        dht11_set_error(DHT11_ERR_RESP_DATA);
        dht11_record_fail();
        return false;
    }

    if (!dht11_read_byte(&hum_i)) {
        if (s_dht11_diag.last_error == DHT11_ERR_NONE) {
            dht11_set_error(DHT11_ERR_BIT_TIMEOUT);
        }
        dht11_record_fail();
        return false;
    }
    if (!dht11_read_byte(&hum_d)) {
        if (s_dht11_diag.last_error == DHT11_ERR_NONE) {
            dht11_set_error(DHT11_ERR_BIT_TIMEOUT);
        }
        dht11_record_fail();
        return false;
    }
    if (!dht11_read_byte(&temp_i)) {
        if (s_dht11_diag.last_error == DHT11_ERR_NONE) {
            dht11_set_error(DHT11_ERR_BIT_TIMEOUT);
        }
        dht11_record_fail();
        return false;
    }
    if (!dht11_read_byte(&temp_d)) {
        if (s_dht11_diag.last_error == DHT11_ERR_NONE) {
            dht11_set_error(DHT11_ERR_BIT_TIMEOUT);
        }
        dht11_record_fail();
        return false;
    }
    if (!dht11_read_byte(&sum)) {
        if (s_dht11_diag.last_error == DHT11_ERR_NONE) {
            dht11_set_error(DHT11_ERR_BIT_TIMEOUT);
        }
        dht11_record_fail();
        return false;
    }

    s_dht11_diag.last_raw[0] = hum_i;
    s_dht11_diag.last_raw[1] = hum_d;
    s_dht11_diag.last_raw[2] = temp_i;
    s_dht11_diag.last_raw[3] = temp_d;
    s_dht11_diag.last_raw[4] = sum;

    calc_sum = (u8)(hum_i + hum_d + temp_i + temp_d);
    if (calc_sum != sum) {
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
    if (diag == 0) {
        return;
    }
    *diag = s_dht11_diag;
}

void DHT11_ClearDiag(void)
{
    memset(&s_dht11_diag, 0, sizeof(s_dht11_diag));
}
