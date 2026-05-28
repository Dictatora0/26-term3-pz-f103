#ifndef __DHT11_H
#define __DHT11_H

#include "system.h"
#include <stdbool.h>

typedef enum
{
    DHT11_ERR_NONE = 0,
    DHT11_ERR_PARAM = 1,
    DHT11_ERR_RESP_LOW = 2,
    DHT11_ERR_RESP_HIGH = 3,
    DHT11_ERR_RESP_DATA = 4,
    DHT11_ERR_BIT_TIMEOUT = 5,
    DHT11_ERR_CHECKSUM = 6
} dht11_error_t;

typedef struct
{
    u32 read_ok_count;
    u32 read_fail_count;
    u32 consecutive_fail_count;
    u32 auto_reset_count;
    u8 last_error;
    u8 last_raw[5];
} dht11_diag_t;

void DHT11_Init(void);
bool DHT11_Read(float *temperature_c, float *humidity_rh);
void DHT11_ResetPin(void);
void DHT11_GetDiag(dht11_diag_t *diag);
void DHT11_ClearDiag(void);

#endif
