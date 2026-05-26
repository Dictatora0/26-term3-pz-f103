#include "system.h"
#include "SysTick.h"
#include "led.h"
#include "beep.h"
#include "usart.h"
#include "adc_temp.h"
#include "key.h"
#include "tftlcd.h"
#include "stm32_flash.h"

#define TEMP_SAMPLE_PERIOD_TICK      50
#define LCD_REFRESH_PERIOD_TICK      20
#define SERIAL_PRINT_PERIOD_TICK     100
#define FILTER_DEPTH                 8
#define TEMP_STEP_CENTI              50
#define TEMP_MIN_GAP_CENTI           100
#define TEMP_HYST_CENTI              100

#define DEFAULT_LOW_TEMP_CENTI       3600
#define DEFAULT_WARN_TEMP_CENTI      4200
#define DEFAULT_ALARM_TEMP_CENTI     4800

#define LIMIT_LOW_TEMP_CENTI         0
#define LIMIT_WARN_TEMP_CENTI        9000
#define LIMIT_ALARM_TEMP_CENTI       12000

#define FLASH_PARAM_ADDR             0x0807F800
#define FLASH_MAGIC0                 0xA55A
#define FLASH_MAGIC1                 0x5AA5
#define FLASH_DATA_LEN               6

typedef enum
{
    STATE_LOW = 0,
    STATE_NORMAL,
    STATE_WARN,
    STATE_ALARM
} alarm_state_t;

typedef struct
{
    u16 low;
    u16 warn;
    u16 alarm;
} threshold_t;

static threshold_t g_thresholds;
static alarm_state_t g_state = STATE_NORMAL;
static int g_raw_temp = 0;
static int g_filtered_temp = 0;
static int g_temp_hist[FILTER_DEPTH];
static u8 g_setting_item = 0;
static u8 g_flash_loaded = 0;
static u16 g_notice_ticks = 0;
static u8 g_notice_type = 0;

static void Format_Temp(int temp, char *buf)
{
    int abs_temp = temp;

    if(abs_temp < 0)
    {
        abs_temp = -abs_temp;
        sprintf(buf, "-%d.%02dC", abs_temp / 100, abs_temp % 100);
    }
    else
    {
        sprintf(buf, "%d.%02dC", abs_temp / 100, abs_temp % 100);
    }
}

static char *Get_State_Name(alarm_state_t state)
{
    switch(state)
    {
        case STATE_LOW:    return "LOW TEMP";
        case STATE_NORMAL: return "NORMAL";
        case STATE_WARN:   return "HIGH WARN";
        default:           return "HIGH ALARM";
    }
}

static char *Get_Setting_Name(u8 item)
{
    switch(item)
    {
        case 1: return "LOW";
        case 2: return "WARN";
        case 3: return "ALARM";
        default:return "RUN";
    }
}

static u16 Get_State_Color(alarm_state_t state)
{
    switch(state)
    {
        case STATE_LOW:    return GREEN;
        case STATE_NORMAL: return BLUE;
        case STATE_WARN:   return BROWN;
        default:           return RED;
    }
}

static void Load_Default_Thresholds(void)
{
    g_thresholds.low = DEFAULT_LOW_TEMP_CENTI;
    g_thresholds.warn = DEFAULT_WARN_TEMP_CENTI;
    g_thresholds.alarm = DEFAULT_ALARM_TEMP_CENTI;
}

static void Clamp_Thresholds(void)
{
    if(g_thresholds.low < LIMIT_LOW_TEMP_CENTI)
        g_thresholds.low = LIMIT_LOW_TEMP_CENTI;

    if(g_thresholds.warn > LIMIT_WARN_TEMP_CENTI)
        g_thresholds.warn = LIMIT_WARN_TEMP_CENTI;

    if(g_thresholds.alarm > LIMIT_ALARM_TEMP_CENTI)
        g_thresholds.alarm = LIMIT_ALARM_TEMP_CENTI;

    if(g_thresholds.warn < g_thresholds.low + TEMP_MIN_GAP_CENTI)
        g_thresholds.warn = g_thresholds.low + TEMP_MIN_GAP_CENTI;

    if(g_thresholds.alarm < g_thresholds.warn + TEMP_MIN_GAP_CENTI)
        g_thresholds.alarm = g_thresholds.warn + TEMP_MIN_GAP_CENTI;

    if(g_thresholds.warn > LIMIT_WARN_TEMP_CENTI)
    {
        g_thresholds.warn = LIMIT_WARN_TEMP_CENTI;
        if(g_thresholds.low > g_thresholds.warn - TEMP_MIN_GAP_CENTI)
            g_thresholds.low = g_thresholds.warn - TEMP_MIN_GAP_CENTI;
    }

    if(g_thresholds.alarm > LIMIT_ALARM_TEMP_CENTI)
    {
        g_thresholds.alarm = LIMIT_ALARM_TEMP_CENTI;
        if(g_thresholds.warn > g_thresholds.alarm - TEMP_MIN_GAP_CENTI)
            g_thresholds.warn = g_thresholds.alarm - TEMP_MIN_GAP_CENTI;
        if(g_thresholds.low > g_thresholds.warn - TEMP_MIN_GAP_CENTI)
            g_thresholds.low = g_thresholds.warn - TEMP_MIN_GAP_CENTI;
    }
}

static u16 Get_Flash_Checksum(const threshold_t *cfg)
{
    return (u16)(cfg->low ^ cfg->warn ^ cfg->alarm ^ 0x3C5A);
}

static u8 Load_Thresholds_From_Flash(void)
{
    u16 data[FLASH_DATA_LEN];

    STM32_FLASH_Read(FLASH_PARAM_ADDR, data, FLASH_DATA_LEN);
    if(data[0] != FLASH_MAGIC0 || data[1] != FLASH_MAGIC1)
        return 0;

    g_thresholds.low = data[2];
    g_thresholds.warn = data[3];
    g_thresholds.alarm = data[4];

    if(data[5] != Get_Flash_Checksum(&g_thresholds))
        return 0;

    if(g_thresholds.low >= g_thresholds.warn)
        return 0;
    if(g_thresholds.warn >= g_thresholds.alarm)
        return 0;
    if(g_thresholds.alarm > LIMIT_ALARM_TEMP_CENTI)
        return 0;

    Clamp_Thresholds();
    return 1;
}

static void Save_Thresholds_To_Flash(void)
{
    u16 data[FLASH_DATA_LEN];

    data[0] = FLASH_MAGIC0;
    data[1] = FLASH_MAGIC1;
    data[2] = g_thresholds.low;
    data[3] = g_thresholds.warn;
    data[4] = g_thresholds.alarm;
    data[5] = Get_Flash_Checksum(&g_thresholds);
    STM32_FLASH_Write(FLASH_PARAM_ADDR, data, FLASH_DATA_LEN);

    g_notice_type = 1;
    g_notice_ticks = 300;
}

static void Filter_Init(int first_temp)
{
    u8 i;
    for(i = 0; i < FILTER_DEPTH; i++)
        g_temp_hist[i] = first_temp;
    g_raw_temp = first_temp;
    g_filtered_temp = first_temp;
}

static int Filter_Push(int new_temp)
{
    u8 i;
    long sum = 0;
    int max_temp;
    int min_temp;

    for(i = FILTER_DEPTH - 1; i > 0; i--)
        g_temp_hist[i] = g_temp_hist[i - 1];
    g_temp_hist[0] = new_temp;

    max_temp = g_temp_hist[0];
    min_temp = g_temp_hist[0];
    for(i = 0; i < FILTER_DEPTH; i++)
    {
        sum += g_temp_hist[i];
        if(g_temp_hist[i] > max_temp)
            max_temp = g_temp_hist[i];
        if(g_temp_hist[i] < min_temp)
            min_temp = g_temp_hist[i];
    }

    sum -= max_temp;
    sum -= min_temp;
    return (int)(sum / (FILTER_DEPTH - 2));
}

static alarm_state_t Decide_State_Immediate(int temp)
{
    if(temp <= g_thresholds.low)
        return STATE_LOW;
    if(temp >= g_thresholds.alarm)
        return STATE_ALARM;
    if(temp >= g_thresholds.warn)
        return STATE_WARN;
    return STATE_NORMAL;
}

static alarm_state_t Update_State_With_Hysteresis(int temp)
{
    switch(g_state)
    {
        case STATE_LOW:
            if(temp >= g_thresholds.low + TEMP_HYST_CENTI)
            {
                if(temp >= g_thresholds.alarm)
                    return STATE_ALARM;
                if(temp >= g_thresholds.warn)
                    return STATE_WARN;
                return STATE_NORMAL;
            }
            return STATE_LOW;

        case STATE_NORMAL:
            if(temp <= g_thresholds.low)
                return STATE_LOW;
            if(temp >= g_thresholds.alarm)
                return STATE_ALARM;
            if(temp >= g_thresholds.warn)
                return STATE_WARN;
            return STATE_NORMAL;

        case STATE_WARN:
            if(temp >= g_thresholds.alarm)
                return STATE_ALARM;
            if(temp <= g_thresholds.warn - TEMP_HYST_CENTI)
            {
                if(temp <= g_thresholds.low)
                    return STATE_LOW;
                return STATE_NORMAL;
            }
            return STATE_WARN;

        default:
            if(temp <= g_thresholds.alarm - TEMP_HYST_CENTI)
            {
                if(temp >= g_thresholds.warn)
                    return STATE_WARN;
                if(temp <= g_thresholds.low)
                    return STATE_LOW;
                return STATE_NORMAL;
            }
            return STATE_ALARM;
    }
}

static void Apply_Output(u32 tick_10ms)
{
    switch(g_state)
    {
        case STATE_LOW:
            LED1 = 1;
            LED2 = 0;
            BEEP = 0;
            break;

        case STATE_NORMAL:
            LED1 = ((tick_10ms / 50) % 2 == 0) ? 0 : 1;
            LED2 = 1;
            BEEP = 0;
            break;

        case STATE_WARN:
            LED1 = ((tick_10ms / 20) % 2 == 0) ? 0 : 1;
            LED2 = 1;
            BEEP = ((tick_10ms % 100) < 20) ? 1 : 0;
            break;

        default:
            LED1 = ((tick_10ms / 10) % 2 == 0) ? 0 : 1;
            LED2 = 1;
            BEEP = 1;
            break;
    }
}

static void LCD_Show_Line(u16 y, char *text, u16 color)
{
    FRONT_COLOR = color;
    BACK_COLOR = WHITE;
    LCD_Fill(0, y, tftlcd_data.width - 1, y + 18, WHITE);
    LCD_ShowString(10, y, tftlcd_data.width - 20, 18, 16, (u8 *)text);
}

static void Refresh_LCD(void)
{
    char raw_buf[20];
    char filt_buf[20];
    char low_buf[20];
    char warn_buf[20];
    char alarm_buf[20];
    char line[80];

    Format_Temp(g_raw_temp, raw_buf);
    Format_Temp(g_filtered_temp, filt_buf);
    Format_Temp(g_thresholds.low, low_buf);
    Format_Temp(g_thresholds.warn, warn_buf);
    Format_Temp(g_thresholds.alarm, alarm_buf);

    LCD_Show_Line(10, "PZ103 TEMP CONTROL DEMO", BLACK);

    sprintf(line, "RAW : %s", raw_buf);
    LCD_Show_Line(36, line, BLACK);

    sprintf(line, "FILT: %s", filt_buf);
    LCD_Show_Line(62, line, BLACK);

    sprintf(line, "STAT: %s", Get_State_Name(g_state));
    LCD_Show_Line(88, line, Get_State_Color(g_state));

    sprintf(line, "LOW : %s", low_buf);
    LCD_Show_Line(114, line, (g_setting_item == 1) ? RED : BLACK);

    sprintf(line, "WARN: %s", warn_buf);
    LCD_Show_Line(140, line, (g_setting_item == 2) ? RED : BLACK);

    sprintf(line, "ALRM: %s", alarm_buf);
    LCD_Show_Line(166, line, (g_setting_item == 3) ? RED : BLACK);

    sprintf(line, "MODE: %s  CFG:%s", Get_Setting_Name(g_setting_item), g_flash_loaded ? "FLASH" : "DEFAULT");
    LCD_Show_Line(192, line, BLUE);

    if(g_notice_ticks > 0)
    {
        if(g_notice_type == 1)
            LCD_Show_Line(218, "FLASH SAVE OK", GREEN);
        else
            LCD_Show_Line(218, "KEY0:Set KEY1/2:+- KEYUP:Save", BLUE);
    }
    else
    {
        LCD_Show_Line(218, "KEY0:Set KEY1/2:+- KEYUP:Save", BLUE);
    }
}

static void Print_Status(void)
{
    char raw_buf[20];
    char filt_buf[20];
    char low_buf[20];
    char warn_buf[20];
    char alarm_buf[20];

    Format_Temp(g_raw_temp, raw_buf);
    Format_Temp(g_filtered_temp, filt_buf);
    Format_Temp(g_thresholds.low, low_buf);
    Format_Temp(g_thresholds.warn, warn_buf);
    Format_Temp(g_thresholds.alarm, alarm_buf);

    printf("raw=%s filt=%s state=%s low=%s warn=%s alarm=%s mode=%s\r\n",
           raw_buf,
           filt_buf,
           Get_State_Name(g_state),
           low_buf,
           warn_buf,
           alarm_buf,
           Get_Setting_Name(g_setting_item));
}

static void Run_Self_Test(void)
{
    LCD_Clear(WHITE);
    LCD_Show_Line(10, "PZ103 TEMP CONTROL DEMO", BLACK);
    LCD_Show_Line(40, "Power-on self test...", BLUE);
    LCD_Show_Line(70, g_flash_loaded ? "Param source: FLASH" : "Param source: DEFAULT", BLACK);

    LED1 = 1;
    LED2 = 1;
    BEEP = 0;
    delay_ms(150);

    LED1 = 0;
    LED2 = 0;
    BEEP = 1;
    LCD_Show_Line(100, "LED/BEEP test OK", GREEN);
    delay_ms(180);

    LED1 = 1;
    LED2 = 1;
    BEEP = 0;
    LCD_Show_Line(130, "System init success", GREEN);
    delay_ms(400);
}

static void Adjust_Selected_Threshold(s8 dir)
{
    if(g_setting_item == 1)
    {
        if(dir > 0)
            g_thresholds.low += TEMP_STEP_CENTI;
        else if(g_thresholds.low >= TEMP_STEP_CENTI)
            g_thresholds.low -= TEMP_STEP_CENTI;
    }
    else if(g_setting_item == 2)
    {
        if(dir > 0)
            g_thresholds.warn += TEMP_STEP_CENTI;
        else if(g_thresholds.warn >= TEMP_STEP_CENTI)
            g_thresholds.warn -= TEMP_STEP_CENTI;
    }
    else if(g_setting_item == 3)
    {
        if(dir > 0)
            g_thresholds.alarm += TEMP_STEP_CENTI;
        else if(g_thresholds.alarm >= TEMP_STEP_CENTI)
            g_thresholds.alarm -= TEMP_STEP_CENTI;
    }

    Clamp_Thresholds();
    g_state = Decide_State_Immediate(g_filtered_temp);
    g_notice_type = 2;
    g_notice_ticks = 200;
}

static void Process_Key(u8 key)
{
    switch(key)
    {
        case KEY0_PRESS:
            g_setting_item++;
            if(g_setting_item > 3)
                g_setting_item = 0;
            g_notice_type = 2;
            g_notice_ticks = 200;
            break;

        case KEY1_PRESS:
            if(g_setting_item != 0)
                Adjust_Selected_Threshold(1);
            break;

        case KEY2_PRESS:
            if(g_setting_item != 0)
                Adjust_Selected_Threshold(-1);
            break;

        case KEY_UP_PRESS:
            Save_Thresholds_To_Flash();
            g_flash_loaded = 1;
            g_setting_item = 0;
            break;

        default:
            break;
    }
}

int main(void)
{
    u32 tick_10ms = 0;
    u8 key_value;

    SysTick_Init(72);
    NVIC_PriorityGroupConfig(NVIC_PriorityGroup_2);
    LED_Init();
    BEEP_Init();
    USART1_Init(115200);
    ADC_Temp_Init();
    KEY_Init();
    TFTLCD_Init();

    Load_Default_Thresholds();
    g_flash_loaded = Load_Thresholds_From_Flash();
    Run_Self_Test();

    g_raw_temp = Get_Temperture();
    Filter_Init(g_raw_temp);
    g_state = Decide_State_Immediate(g_filtered_temp);
    Refresh_LCD();
    Print_Status();

    while(1)
    {
        tick_10ms++;

        key_value = KEY_Scan(0);
        if(key_value != 0)
            Process_Key(key_value);

        if((tick_10ms % TEMP_SAMPLE_PERIOD_TICK) == 0)
        {
            g_raw_temp = Get_Temperture();
            g_filtered_temp = Filter_Push(g_raw_temp);
            g_state = Update_State_With_Hysteresis(g_filtered_temp);
        }

        Apply_Output(tick_10ms);

        if((tick_10ms % LCD_REFRESH_PERIOD_TICK) == 0)
            Refresh_LCD();

        if((tick_10ms % SERIAL_PRINT_PERIOD_TICK) == 0)
            Print_Status();

        if(g_notice_ticks > 0)
            g_notice_ticks--;

        delay_ms(10);
    }
}
