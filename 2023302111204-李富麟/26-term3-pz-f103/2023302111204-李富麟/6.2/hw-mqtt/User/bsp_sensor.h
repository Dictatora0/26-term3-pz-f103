#ifndef __BSP_SENSOR_H
#define __BSP_SENSOR_H

#include "system.h"
#include <stdbool.h>
#include <stdint.h>

typedef struct
{
	float temperature;
	uint32_t light;
	uint32_t uptime;

	bool temperature_valid;
	bool light_valid;
} SensorData;

bool Sensor_Init(void);
bool Sensor_Read(SensorData *data);
void Sensor_Process(void);

#endif
