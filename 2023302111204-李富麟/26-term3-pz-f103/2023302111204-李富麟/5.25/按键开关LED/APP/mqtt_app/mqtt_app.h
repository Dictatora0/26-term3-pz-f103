#ifndef __MQTT_APP_H
#define __MQTT_APP_H

#include "system.h"
#include <stdbool.h>

void MQTT_App_Init(void);
void MQTT_App_Loop(void);
bool MQTT_App_PublishAck(const char *payload);

#endif
