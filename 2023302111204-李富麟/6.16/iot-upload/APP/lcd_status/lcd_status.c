#include "lcd_status.h"
#include "board_adapter.h"
#include "tftlcd.h"
#include "SysTick.h"
#include <stdio.h>
#include <string.h>

#define LCD_STATUS_REFRESH_MS       500U
#define LCD_STATUS_TEXT_SIZE        16U
#define LCD_STATUS_LINE_HEIGHT      24U
#define LCD_STATUS_LABEL_X          8U
#define LCD_STATUS_VALUE_X          88U
#define LCD_STATUS_VALUE_WIDTH      220U

static u8 s_lcd_ready = 0U;
static u32 s_next_refresh_ms = 0U;
static char s_last_command[24] = "NONE";
static char s_command_state[8] = "WAIT";

static void lcd_status_clear_value_line(u16 y)
{
    u16 y_end;

    if ((tftlcd_data.width > LCD_STATUS_VALUE_X) && (tftlcd_data.height > y))
    {
        y_end = (u16)(y + LCD_STATUS_LINE_HEIGHT - 1U);
        if (y_end >= tftlcd_data.height)
        {
            y_end = (u16)(tftlcd_data.height - 1U);
        }

        LCD_Fill(LCD_STATUS_VALUE_X,
                 y,
                 tftlcd_data.width - 1U,
                 y_end,
                 BLACK);
    }
}

static void lcd_status_show_text(u16 x, u16 y, const char *text, u16 color)
{
    u16 width;

    if ((tftlcd_data.width <= x) || (tftlcd_data.height <= y))
    {
        return;
    }

    width = (u16)(tftlcd_data.width - x);
    if (width > LCD_STATUS_VALUE_WIDTH)
    {
        width = LCD_STATUS_VALUE_WIDTH;
    }

    FRONT_COLOR = color;
    BACK_COLOR = BLACK;
    LCD_ShowString(x,
                   y,
                   width,
                   LCD_STATUS_LINE_HEIGHT,
                   LCD_STATUS_TEXT_SIZE,
                   (u8 *)text);
}

static void lcd_status_show_value(u16 y, const char *text, u16 color)
{
    lcd_status_clear_value_line(y);
    lcd_status_show_text(LCD_STATUS_VALUE_X, y, text, color);
}

static void lcd_status_draw_static(void)
{
    LCD_Clear(BLACK);
    lcd_status_show_text(8U, 8U, "PZ F103 UART GW", YELLOW);
    LCD_DrawLine_Color(0U, 32U, (u16)(tftlcd_data.width - 1U), 32U, GRAY);

    lcd_status_show_text(LCD_STATUS_LABEL_X, 48U, "TEMP", CYAN);
    lcd_status_show_text(LCD_STATUS_LABEL_X, 72U, "LIGHT", CYAN);
    lcd_status_show_text(LCD_STATUS_LABEL_X, 96U, "LED", CYAN);
    lcd_status_show_text(LCD_STATUS_LABEL_X, 120U, "BUZZER", CYAN);
    lcd_status_show_text(LCD_STATUS_LABEL_X, 144U, "UART", CYAN);
    lcd_status_show_text(LCD_STATUS_LABEL_X, 168U, "CMD", CYAN);

    lcd_status_show_value(48U, "--.- C", WHITE);
    lcd_status_show_value(72U, "--- %", WHITE);
    lcd_status_show_value(96U, "OFF", WHITE);
    lcd_status_show_value(120U, "OFF", WHITE);
    lcd_status_show_value(144U, "WAIT", WHITE);
    lcd_status_show_value(168U, "NONE", WHITE);
}

static void lcd_status_format_temperature(char *text, float temperature)
{
    int temp_x10;
    int abs_x10;

    if (temperature >= 0.0f)
    {
        temp_x10 = (int)((temperature * 10.0f) + 0.5f);
    }
    else
    {
        temp_x10 = (int)((temperature * 10.0f) - 0.5f);
    }

    if (temp_x10 < 0)
    {
        abs_x10 = -temp_x10;
        sprintf(text, "-%d.%d C", abs_x10 / 10, abs_x10 % 10);
    }
    else
    {
        sprintf(text, "%d.%d C", temp_x10 / 10, temp_x10 % 10);
    }
}

static void lcd_status_format_light(char *text, float light)
{
    int light_percent;

    if (light >= 0.0f)
    {
        light_percent = (int)(light + 0.5f);
    }
    else
    {
        light_percent = 0;
    }

    if (light_percent > 100)
    {
        light_percent = 100;
    }

    sprintf(text, "%d %%", light_percent);
}

void LCD_Status_Init(void)
{
    TFTLCD_Init();
    s_lcd_ready = 1U;
    s_next_refresh_ms = 0U;
    strcpy(s_last_command, "NONE");
    strcpy(s_command_state, "WAIT");
    lcd_status_draw_static();
    LCD_Status_RefreshNow();
}

void LCD_Status_SetLastCommand(const char *cmd, u8 accepted)
{
    if (cmd == 0)
    {
        return;
    }

    strncpy(s_last_command, cmd, sizeof(s_last_command) - 1U);
    s_last_command[sizeof(s_last_command) - 1U] = '\0';

    if (accepted != 0U)
    {
        strcpy(s_command_state, "OK");
    }
    else
    {
        strcpy(s_command_state, "ERR");
    }

    LCD_Status_RefreshNow();
}

void LCD_Status_RefreshNow(void)
{
    board_env_data_t data;
    char text[32];

    if (s_lcd_ready == 0U)
    {
        return;
    }

    if (BoardAdapter_GetData(&data))
    {
        lcd_status_format_temperature(text, data.temperature);
        lcd_status_show_value(48U, text, GREEN);

        lcd_status_format_light(text, data.light);
        lcd_status_show_value(72U, text, GREEN);

        lcd_status_show_value(96U, (data.led != 0U) ? "ON" : "OFF",
                              (data.led != 0U) ? YELLOW : WHITE);
        lcd_status_show_value(120U, (data.buzzer != 0U) ? "ON" : "OFF",
                              (data.buzzer != 0U) ? RED : WHITE);
        lcd_status_show_value(144U, "OK", GREEN);
    }
    else
    {
        lcd_status_show_value(144U, "WAIT", YELLOW);
    }

    sprintf(text, "%s %s", s_command_state, s_last_command);
    lcd_status_show_value(168U, text, (strcmp(s_command_state, "ERR") == 0) ? RED : WHITE);
}

void LCD_Status_Process(void)
{
    u32 now;

    if (s_lcd_ready == 0U)
    {
        return;
    }

    now = SysTick_GetMs();
    if ((s_next_refresh_ms == 0U) || (now >= s_next_refresh_ms))
    {
        LCD_Status_RefreshNow();
        s_next_refresh_ms = SysTick_GetMs() + LCD_STATUS_REFRESH_MS;
    }
}
