#ifndef __LCD_STATUS_H
#define __LCD_STATUS_H

#include "system.h"

void LCD_Status_Init(void);
void LCD_Status_Process(void);
void LCD_Status_RefreshNow(void);
void LCD_Status_SetLastCommand(const char *cmd, u8 accepted);

#endif
