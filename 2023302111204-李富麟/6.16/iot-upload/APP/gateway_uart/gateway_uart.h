#ifndef __GATEWAY_UART_H
#define __GATEWAY_UART_H

#include "system.h"

#define GATEWAY_UART_REC_LEN       64U

void GatewayUart_Init(u32 bound);
void GatewayUart_PollReceive(void);
u8 GatewayUart_GetCommand(char *out, u16 out_size);
u32 GatewayUart_GetRxDebugCount(void);
u8 GatewayUart_GetRxDebugLastByte(void);

#endif
