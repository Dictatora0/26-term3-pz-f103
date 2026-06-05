#ifndef _ADC_TEMP_H
#define _ADC_TEMP_H

#include "system.h"

void ADC_Temp_Init(void);
u16 Get_ADC_Temp_Value(u8 ch, u8 times);
int Get_Temperture(void);

void ADC_Light_Init(void);
u16 Get_ADC_Light_Value(u8 ch, u8 times);
u8 Get_Light_Percent(void);

#endif
