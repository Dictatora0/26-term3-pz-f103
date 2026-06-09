#ifndef __SENSOR_TEMP_HUM_H
#define __SENSOR_TEMP_HUM_H

#include "system.h"
#include <stdbool.h>

typedef struct
{
    bool temperature_valid;
    bool humidity_valid;
    float temperature_c;
    float humidity_rh;
} sensor_temp_hum_data_t;

void Sensor_TempHum_Init(void);
bool Sensor_TempHum_Read(sensor_temp_hum_data_t *out);

#endif
