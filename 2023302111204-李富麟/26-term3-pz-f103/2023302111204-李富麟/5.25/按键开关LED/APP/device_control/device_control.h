#ifndef __DEVICE_CONTROL_H
#define __DEVICE_CONTROL_H

#include "system.h"
#include "sensor_temp_hum.h"
#include <stdbool.h>

typedef struct
{
    bool led_on;
    bool mqtt_connected;
    bool sensor_valid;
    bool humidity_valid;
    float temperature_c;
    float humidity_rh;
    u32 local_event_count;
    u32 remote_event_count;
    char last_source[16];
    char last_action[24];
} device_snapshot_t;

void Device_Control_Init(void);
bool Device_Control_HandleLocalInput(void);
bool Device_Control_HandleCommand(const char *payload, char *ack, u16 ack_size);
void Device_Control_SetMqttConnected(bool connected);
void Device_Control_UpdateSensor(const sensor_temp_hum_data_t *data);
bool Device_Control_BuildStatePayload(char *payload, u16 payload_size);
void Device_Control_RefreshDisplay(void);
bool Device_Control_IsStateDirty(void);
void Device_Control_ClearStateDirty(void);
const device_snapshot_t *Device_Control_GetSnapshot(void);

#endif
