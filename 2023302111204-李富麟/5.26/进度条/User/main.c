#include "system.h"
#include "SysTick.h"
#include "led.h"
#include "usart.h"
#include "key.h"
#include "tftlcd.h"

#define BAR_MIN_VALUE            0
#define BAR_MAX_VALUE            100
#define BAR_AUTO_STEP            1
#define BAR_MANUAL_STEP          5
#define UI_REFRESH_DELAY_MS      40
#define UART_REPORT_DIV          12

#define UI_HEADER_COLOR          DARKBLUE
#define UI_PANEL_COLOR           LGRAY
#define UI_PANEL_EDGE_COLOR      GRAYBLUE
#define UI_TEXT_COLOR            BLACK
#define UI_VALUE_COLOR           RED
#define UI_BAR_FRAME_COLOR       GRAYBLUE
#define UI_BAR_BACK_COLOR        WHITE
#define UI_BAR_FILL_A            0x55CC
#define UI_BAR_FILL_B            0x7F10
#define UI_BAR_HEAD_COLOR        0xB7F7
#define UI_VBAR_FILL_A           0x04BF
#define UI_VBAR_FILL_B           0x1D7F
#define UI_VBAR_HEAD_COLOR       0x7EFF

typedef struct
{
    u16 x1;
    u16 y1;
    u16 x2;
    u16 y2;
} ui_rect_t;

static ui_rect_t g_panel_rect;
static ui_rect_t g_hbar_rect;
static ui_rect_t g_vbar_rect;
static ui_rect_t g_value_rect;
static ui_rect_t g_info1_rect;
static ui_rect_t g_info2_rect;
static ui_rect_t g_info3_rect;
static ui_rect_t g_hint1_rect;
static ui_rect_t g_hint2_rect;

static u8 g_progress = 0;
static u8 g_auto_mode = 1;
static s8 g_direction = 1;
static u8 g_anim_phase = 0;

static u16 Text_Pixel_Width(const char *text, u8 size)
{
    u16 len = 0;

    while(text[len] != 0)
    {
        len++;
    }

    return (u16)(len * (size / 2));
}

static void Show_Text_Left(const ui_rect_t *rect, u8 size, u16 color, u16 bg, const char *text)
{
    FRONT_COLOR = color;
    BACK_COLOR = bg;
    LCD_Fill(rect->x1, rect->y1, rect->x2, rect->y2, bg);
    LCD_ShowString(rect->x1, rect->y1, rect->x2 - rect->x1 + 1, rect->y2 - rect->y1 + 1, size, (u8 *)text);
}

static void Show_Text_Center(const ui_rect_t *rect, u8 size, u16 color, u16 bg, const char *text)
{
    u16 area_width;
    u16 area_height;
    u16 text_width;
    u16 start_x;

    area_width = rect->x2 - rect->x1 + 1;
    area_height = rect->y2 - rect->y1 + 1;
    text_width = Text_Pixel_Width(text, size);
    start_x = rect->x1;

    LCD_Fill(rect->x1, rect->y1, rect->x2, rect->y2, bg);

    if(text_width < area_width)
    {
        start_x = rect->x1 + (area_width - text_width) / 2;
    }

    FRONT_COLOR = color;
    BACK_COLOR = bg;
    LCD_ShowString(start_x, rect->y1, rect->x2 - start_x + 1, area_height, size, (u8 *)text);
}

static void Set_Progress(int value)
{
    if(value < BAR_MIN_VALUE)
    {
        g_progress = BAR_MIN_VALUE;
    }
    else if(value > BAR_MAX_VALUE)
    {
        g_progress = BAR_MAX_VALUE;
    }
    else
    {
        g_progress = (u8)value;
    }
}

static void Update_LED_State(void)
{
    LED1 = g_auto_mode ? 0 : 1;

    if(g_auto_mode)
    {
        LED2 = (g_direction > 0) ? 0 : 1;
    }
    else
    {
        LED2 = (g_progress >= 50) ? 0 : 1;
    }
}

static void Print_Status(void)
{
    printf("progress=%u%% mode=%s dir=%s\r\n",
           g_progress,
           g_auto_mode ? "auto" : "manual",
           (g_direction > 0) ? "up" : "down");
}

static void Setup_Layout(void)
{
    u16 margin;
    u16 gap;
    u16 vbar_width;

    margin = (tftlcd_data.width >= 300) ? 18 : 12;
    gap = (tftlcd_data.width >= 300) ? 18 : 12;
    vbar_width = (tftlcd_data.width >= 300) ? 28 : 22;

    g_panel_rect.x1 = 8;
    g_panel_rect.y1 = 56;
    g_panel_rect.x2 = tftlcd_data.width - 9;
    g_panel_rect.y2 = tftlcd_data.height - 52;

    g_hbar_rect.x1 = margin + 2;
    g_hbar_rect.y1 = g_panel_rect.y1 + 34;
    g_hbar_rect.x2 = tftlcd_data.width - margin - vbar_width - gap - 1;
    g_hbar_rect.y2 = g_hbar_rect.y1 + 24;

    g_value_rect.x1 = g_hbar_rect.x1;
    g_value_rect.y1 = g_hbar_rect.y2 + 14;
    g_value_rect.x2 = g_hbar_rect.x2;
    g_value_rect.y2 = g_value_rect.y1 + 30;

    g_info1_rect.x1 = g_hbar_rect.x1;
    g_info1_rect.y1 = g_value_rect.y2 + 10;
    g_info1_rect.x2 = g_hbar_rect.x2;
    g_info1_rect.y2 = g_info1_rect.y1 + 18;

    g_info2_rect.x1 = g_hbar_rect.x1;
    g_info2_rect.y1 = g_info1_rect.y2 + 6;
    g_info2_rect.x2 = g_hbar_rect.x2;
    g_info2_rect.y2 = g_info2_rect.y1 + 18;

    g_info3_rect.x1 = g_hbar_rect.x1;
    g_info3_rect.y1 = g_info2_rect.y2 + 6;
    g_info3_rect.x2 = g_hbar_rect.x2;
    g_info3_rect.y2 = g_info3_rect.y1 + 18;

    g_vbar_rect.x2 = tftlcd_data.width - margin - 1;
    g_vbar_rect.x1 = g_vbar_rect.x2 - vbar_width + 1;
    g_vbar_rect.y1 = g_hbar_rect.y2 + 30;
    g_vbar_rect.y2 = g_panel_rect.y2 - 40;

    if(g_vbar_rect.y2 < (u16)(g_vbar_rect.y1 + 100))
    {
        g_vbar_rect.y2 = g_vbar_rect.y1 + 100;
    }

    g_hint1_rect.x1 = margin;
    g_hint1_rect.y1 = tftlcd_data.height - 38;
    g_hint1_rect.x2 = tftlcd_data.width - margin;
    g_hint1_rect.y2 = g_hint1_rect.y1 + 12;

    g_hint2_rect.x1 = margin;
    g_hint2_rect.y1 = g_hint1_rect.y2 + 3;
    g_hint2_rect.x2 = tftlcd_data.width - margin;
    g_hint2_rect.y2 = g_hint2_rect.y1 + 12;
}

static void Draw_Bar_Frame(const ui_rect_t *rect)
{
    LCD_Fill(rect->x1, rect->y1, rect->x2, rect->y2, UI_BAR_BACK_COLOR);
    FRONT_COLOR = UI_BAR_FRAME_COLOR;
    LCD_DrawRectangle(rect->x1, rect->y1, rect->x2, rect->y2);
    LCD_DrawRectangle(rect->x1 + 1, rect->y1 + 1, rect->x2 - 1, rect->y2 - 1);
}

static void Draw_H_Scale(void)
{
    u8 i;
    u16 x;
    u16 width;

    width = g_hbar_rect.x2 - g_hbar_rect.x1;

    for(i = 0; i <= 10; i++)
    {
        x = g_hbar_rect.x1 + (u16)((width * i) / 10);
        LCD_DrawLine_Color(x, g_hbar_rect.y2 + 2, x, g_hbar_rect.y2 + 6, UI_PANEL_EDGE_COLOR);
    }
}

static void Draw_V_Scale(void)
{
    u8 i;
    u16 y;
    u16 height;

    height = g_vbar_rect.y2 - g_vbar_rect.y1;

    for(i = 0; i <= 10; i++)
    {
        y = g_vbar_rect.y2 - (u16)((height * i) / 10);
        LCD_DrawLine_Color(g_vbar_rect.x1 - 6, y, g_vbar_rect.x1 - 2, y, UI_PANEL_EDGE_COLOR);
    }
}

static void Draw_Static_UI(void)
{
    ui_rect_t header_rect;
    ui_rect_t title_rect;
    ui_rect_t label_rect;

    LCD_Clear(WHITE);

    LCD_Fill(0, 0, tftlcd_data.width - 1, 44, UI_HEADER_COLOR);

    header_rect.x1 = 0;
    header_rect.y1 = 8;
    header_rect.x2 = tftlcd_data.width - 1;
    header_rect.y2 = 24;
    Show_Text_Center(&header_rect, 16, WHITE, UI_HEADER_COLOR, "LCD PROGRESS BAR DEMO");

    header_rect.y1 = 26;
    header_rect.y2 = 38;
    Show_Text_Center(&header_rect, 12, WHITE, UI_HEADER_COLOR, "STM32F103 + TFTLCD");

    LCD_Fill(g_panel_rect.x1, g_panel_rect.y1, g_panel_rect.x2, g_panel_rect.y2, UI_PANEL_COLOR);
    FRONT_COLOR = UI_PANEL_EDGE_COLOR;
    LCD_DrawRectangle(g_panel_rect.x1, g_panel_rect.y1, g_panel_rect.x2, g_panel_rect.y2);
    LCD_DrawRectangle(g_panel_rect.x1 + 1, g_panel_rect.y1 + 1, g_panel_rect.x2 - 1, g_panel_rect.y2 - 1);

    title_rect.x1 = g_panel_rect.x1 + 10;
    title_rect.y1 = g_panel_rect.y1 + 8;
    title_rect.x2 = g_panel_rect.x2 - 10;
    title_rect.y2 = title_rect.y1 + 16;
    Show_Text_Left(&title_rect, 16, UI_TEXT_COLOR, UI_PANEL_COLOR, "Reference style: LVGL bar demo");

    label_rect.x1 = g_hbar_rect.x1;
    label_rect.y1 = g_hbar_rect.y1 - 22;
    label_rect.x2 = g_hbar_rect.x1 + 80;
    label_rect.y2 = label_rect.y1 + 14;
    Show_Text_Left(&label_rect, 12, UI_TEXT_COLOR, UI_PANEL_COLOR, "H-BAR");

    label_rect.x1 = g_vbar_rect.x1 - 6;
    label_rect.y1 = g_vbar_rect.y1 - 22;
    label_rect.x2 = g_vbar_rect.x2 + 6;
    label_rect.y2 = label_rect.y1 + 14;
    Show_Text_Center(&label_rect, 12, UI_TEXT_COLOR, UI_PANEL_COLOR, "V-BAR");

    Draw_Bar_Frame(&g_hbar_rect);
    Draw_Bar_Frame(&g_vbar_rect);
    Draw_H_Scale();
    Draw_V_Scale();

    label_rect.x1 = g_hbar_rect.x1;
    label_rect.y1 = g_hbar_rect.y2 + 8;
    label_rect.x2 = g_hbar_rect.x1 + 24;
    label_rect.y2 = label_rect.y1 + 12;
    Show_Text_Left(&label_rect, 12, UI_TEXT_COLOR, UI_PANEL_COLOR, "0%");

    label_rect.x1 = g_hbar_rect.x2 - 22;
    label_rect.x2 = g_hbar_rect.x2 + 18;
    Show_Text_Left(&label_rect, 12, UI_TEXT_COLOR, UI_PANEL_COLOR, "100%");

    label_rect.x1 = g_vbar_rect.x1 - 12;
    label_rect.y1 = g_vbar_rect.y1 - 14;
    label_rect.x2 = g_vbar_rect.x2 + 8;
    label_rect.y2 = label_rect.y1 + 12;
    Show_Text_Center(&label_rect, 12, UI_TEXT_COLOR, UI_PANEL_COLOR, "100%");

    label_rect.y1 = g_vbar_rect.y2 + 6;
    label_rect.y2 = label_rect.y1 + 12;
    Show_Text_Center(&label_rect, 12, UI_TEXT_COLOR, UI_PANEL_COLOR, "0%");

    Show_Text_Center(&g_hint1_rect, 12, UI_TEXT_COLOR, WHITE, "KEY0:AUTO/MANUAL   KEY1:UP/+");
    Show_Text_Center(&g_hint2_rect, 12, UI_TEXT_COLOR, WHITE, "KEY2:DOWN/-        WK_UP:RESET");
}

static void Draw_H_Progress(u8 value)
{
    u16 inner_x1;
    u16 inner_y1;
    u16 inner_x2;
    u16 inner_y2;
    u16 inner_width;
    u16 filled_width;
    u16 fill_end;
    u16 cursor;
    u16 seg_end;
    u16 color;
    u16 head_x1;

    inner_x1 = g_hbar_rect.x1 + 3;
    inner_y1 = g_hbar_rect.y1 + 3;
    inner_x2 = g_hbar_rect.x2 - 3;
    inner_y2 = g_hbar_rect.y2 - 3;
    inner_width = inner_x2 - inner_x1 + 1;

    LCD_Fill(inner_x1, inner_y1, inner_x2, inner_y2, UI_BAR_BACK_COLOR);

    filled_width = (u16)(((u32)inner_width * value) / BAR_MAX_VALUE);
    if(filled_width == 0)
    {
        return;
    }

    fill_end = inner_x1 + filled_width - 1;
    cursor = inner_x1;

    while(cursor <= fill_end)
    {
        seg_end = cursor + 9;
        if(seg_end > fill_end)
        {
            seg_end = fill_end;
        }

        color = ((((cursor - inner_x1) / 10) + g_anim_phase) & 0x01) ? UI_BAR_FILL_A : UI_BAR_FILL_B;
        LCD_Fill(cursor, inner_y1, seg_end, inner_y2, color);

        if(seg_end == fill_end)
        {
            break;
        }

        cursor = seg_end + 1;
    }

    head_x1 = inner_x1;
    if(fill_end > (u16)(inner_x1 + 14))
    {
        head_x1 = fill_end - 14;
    }
    LCD_Fill(head_x1, inner_y1, fill_end, inner_y2, UI_BAR_HEAD_COLOR);
}

static void Draw_V_Progress(u8 value)
{
    u16 inner_x1;
    u16 inner_y1;
    u16 inner_x2;
    u16 inner_y2;
    u16 inner_height;
    u16 filled_height;
    u16 fill_start;
    u16 head_y2;
    int band_end;
    int band_start;
    u16 color;

    inner_x1 = g_vbar_rect.x1 + 3;
    inner_y1 = g_vbar_rect.y1 + 3;
    inner_x2 = g_vbar_rect.x2 - 3;
    inner_y2 = g_vbar_rect.y2 - 3;
    inner_height = inner_y2 - inner_y1 + 1;

    LCD_Fill(inner_x1, inner_y1, inner_x2, inner_y2, UI_BAR_BACK_COLOR);

    filled_height = (u16)(((u32)inner_height * value) / BAR_MAX_VALUE);
    if(filled_height == 0)
    {
        return;
    }

    fill_start = inner_y2 - filled_height + 1;
    band_end = inner_y2;

    while(band_end >= fill_start)
    {
        band_start = band_end - 7;
        if(band_start < fill_start)
        {
            band_start = fill_start;
        }

        color = ((((inner_y2 - band_end) / 8) + g_anim_phase) & 0x01) ? UI_VBAR_FILL_A : UI_VBAR_FILL_B;
        LCD_Fill(inner_x1, (u16)band_start, inner_x2, (u16)band_end, color);

        if(band_start == fill_start)
        {
            break;
        }

        band_end = band_start - 1;
    }

    head_y2 = fill_start + 14;
    if(head_y2 > inner_y2)
    {
        head_y2 = inner_y2;
    }
    LCD_Fill(inner_x1, fill_start, inner_x2, head_y2, UI_VBAR_HEAD_COLOR);
}

static void Refresh_Dynamic_UI(void)
{
    char line[32];

    Draw_H_Progress(g_progress);
    Draw_V_Progress(g_progress);

    sprintf(line, "%03u%%", g_progress);
    Show_Text_Center(&g_value_rect, 24, UI_VALUE_COLOR, UI_PANEL_COLOR, line);

    sprintf(line, "MODE : %s", g_auto_mode ? "AUTO" : "MANUAL");
    Show_Text_Left(&g_info1_rect, 16, g_auto_mode ? BLUE : UI_TEXT_COLOR, UI_PANEL_COLOR, line);

    if(g_auto_mode)
    {
        sprintf(line, "DIR  : %s", (g_direction > 0) ? "UP" : "DOWN");
    }
    else
    {
        sprintf(line, "STEP : %u%% / KEY", BAR_MANUAL_STEP);
    }
    Show_Text_Left(&g_info2_rect, 16, UI_TEXT_COLOR, UI_PANEL_COLOR, line);

    if(g_progress == BAR_MIN_VALUE)
    {
        sprintf(line, "STATE: EMPTY");
    }
    else if(g_progress == BAR_MAX_VALUE)
    {
        sprintf(line, "STATE: FULL");
    }
    else
    {
        sprintf(line, "STATE: RUNNING");
    }
    Show_Text_Left(&g_info3_rect, 16, UI_TEXT_COLOR, UI_PANEL_COLOR, line);
}

static void Run_Auto_Step(void)
{
    if(g_auto_mode != 0)
    {
        if(g_direction > 0)
        {
            if(g_progress >= (BAR_MAX_VALUE - BAR_AUTO_STEP))
            {
                g_progress = BAR_MAX_VALUE;
                g_direction = -1;
            }
            else
            {
                g_progress += BAR_AUTO_STEP;
            }
        }
        else
        {
            if(g_progress <= (BAR_MIN_VALUE + BAR_AUTO_STEP))
            {
                g_progress = BAR_MIN_VALUE;
                g_direction = 1;
            }
            else
            {
                g_progress -= BAR_AUTO_STEP;
            }
        }
    }

    g_anim_phase++;
}

static void Process_Key(u8 key_value)
{
    switch(key_value)
    {
        case KEY0_PRESS:
            g_auto_mode = !g_auto_mode;
            break;

        case KEY1_PRESS:
            if(g_auto_mode)
            {
                g_direction = 1;
            }
            else
            {
                Set_Progress((int)g_progress + BAR_MANUAL_STEP);
            }
            break;

        case KEY2_PRESS:
            if(g_auto_mode)
            {
                g_direction = -1;
            }
            else
            {
                Set_Progress((int)g_progress - BAR_MANUAL_STEP);
            }
            break;

        case KEY_UP_PRESS:
            g_progress = BAR_MIN_VALUE;
            g_direction = 1;
            break;

        default:
            break;
    }

    Update_LED_State();
    Print_Status();
}

int main(void)
{
    u8 key_value;
    u16 report_div;

    SysTick_Init(72);
    NVIC_PriorityGroupConfig(NVIC_PriorityGroup_2);
    LED_Init();
    USART1_Init(115200);
    KEY_Init();
    TFTLCD_Init();

    FRONT_COLOR = UI_TEXT_COLOR;
    BACK_COLOR = WHITE;

    Setup_Layout();
    Draw_Static_UI();
    Update_LED_State();
    Refresh_Dynamic_UI();
    Print_Status();

    report_div = 0;

    while(1)
    {
        key_value = KEY_Scan(0);
        if(key_value != 0)
        {
            Process_Key(key_value);
        }

        Run_Auto_Step();
        Update_LED_State();
        Refresh_Dynamic_UI();

        report_div++;
        if(report_div >= UART_REPORT_DIV)
        {
            report_div = 0;
            Print_Status();
        }

        delay_ms(UI_REFRESH_DELAY_MS);
    }
}
