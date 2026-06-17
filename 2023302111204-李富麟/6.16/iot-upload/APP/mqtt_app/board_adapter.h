#ifndef __BOARD_ADAPTER_H
#define __BOARD_ADAPTER_H

#include "system.h"
#include <stdbool.h>

typedef struct
{
    float temperature;
    float humidity;
    float light;
    u8 led;
    u8 buzzer;
    bool sensor_valid;
} board_env_data_t;

void BoardAdapter_Init(void);
void BoardAdapter_Process(void);
bool BoardAdapter_GetData(board_env_data_t *out);
bool BoardAdapter_ForceSample(void);
void BoardAdapter_PrintStatusLine(void);
void BoardAdapter_SetLed(u8 on);
u8 BoardAdapter_GetLed(void);
void BoardAdapter_SetBuzzer(u8 on);
u8 BoardAdapter_GetBuzzer(void);

#endif
