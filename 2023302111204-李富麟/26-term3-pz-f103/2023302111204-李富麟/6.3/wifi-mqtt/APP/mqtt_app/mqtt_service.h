#ifndef __MQTT_SERVICE_H
#define __MQTT_SERVICE_H

#include "system.h"
#include <stdbool.h>

void MQTT_Service_Init(void);
void MQTT_Service_Task(void);
bool mqtt_connect(void);
bool mqtt_subscribe_control_topic(void);
bool mqtt_publish_sensor_data(float temp, float humi, u8 led);
bool mqtt_process_downlink(const char *payload);

#endif
