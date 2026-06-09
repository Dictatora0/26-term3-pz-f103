#ifndef _adc_temp_H
#define _adc_temp_H

#include "system.h"
#include <limits.h>

#define ADC_SENSOR_TIMEOUT_VALUE   ((u16)0xFFFFU)
#define ADC_TEMP_INVALID_CENTI     INT_MIN

void ADC_Temp_Init(void);
u16 Get_ADC_Temp_Value(u8 ch, u8 times);
int Get_Temperture(void);
void ADC_Light_Init(void);
u16 Get_ADC_Light_Value(u8 ch, u8 times);
u16 Get_Light_Raw(void);
u8 Get_Light_Percent(void);

#endif
