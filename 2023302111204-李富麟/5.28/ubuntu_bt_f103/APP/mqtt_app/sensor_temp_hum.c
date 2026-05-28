#include "sensor_temp_hum.h"
#include "adc_temp.h"
#include "dht11.h"
#include "esp8266_mqtt_config.h"

static bool s_dht11_ready = false;

void Sensor_TempHum_Init(void)
{
    ADC_Temp_Init();
#if SENSOR_ENABLE_DHT11
    DHT11_Init();
    s_dht11_ready = true;
#else
    s_dht11_ready = false;
#endif
}

bool Sensor_TempHum_Read(sensor_temp_hum_data_t *out)
{
    int t100;
    float dht_t = 0.0f;
    float dht_h = 0.0f;
    bool dht_ok = false;

    if (out == 0) {
        return false;
    }

    t100 = Get_Temperture(); /* unit: 0.01 C */
    out->temperature_valid = true;
    out->temperature_c = ((float)t100) / 100.0f;

    if (s_dht11_ready) {
        dht_ok = DHT11_Read(&dht_t, &dht_h);
    }

    if (dht_ok) {
        out->humidity_valid = true;
        out->humidity_rh = dht_h;
        /*
         * Prefer DHT11 ambient temperature for consistency with humidity.
         * Keep internal ADC temperature as fallback.
         */
        out->temperature_c = dht_t;
    } else {
        out->humidity_valid = false;
        out->humidity_rh = 0.0f;
    }

    return true;
}
