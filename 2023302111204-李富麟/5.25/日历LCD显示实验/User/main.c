#include "system.h"
#include "SysTick.h"
#include "usart.h"
#include "led.h"
#include "tftlcd.h"

#define CALENDAR_YEAR_MIN 1970U
#define CALENDAR_YEAR_MAX 2099U

typedef struct
{
	u16 year;
	u8 month;
	u8 day;
} calendar_date_t;

static u8 IsLeapYear(u16 year);
static u8 GetMonthDays(u16 year, u8 month);
static u8 GetWeekday(u16 year, u8 month, u8 day);
static u8 GetFirstWeekdayOfMonth(u16 year, u8 month);
static void DrawTextCenter(u16 x, u16 y, u16 width, u8 size, u8 *text);
static void DrawCalendarPage(calendar_date_t *date);
static void DrawCalendarHeader(calendar_date_t *date);
static void DrawCalendarWeekTitle(u16 x, u16 y, u16 cell_w, u16 cell_h);
static void DrawCalendarGrid(calendar_date_t *date, u16 x, u16 y, u16 width, u16 height);
static void DrawDateBadge(u16 x, u16 y, u16 width, u16 height, calendar_date_t *date);
static void ParseBuildDate(calendar_date_t *date);
static u8 MatchMonthToken(const char *token, const char *month);

static const char *const g_month_name[12] =
{
	"January", "February", "March", "April", "May", "June",
	"July", "August", "September", "October", "November", "December"
};

static const char *const g_week_name[7] =
{
	"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"
};

static u8 IsLeapYear(u16 year)
{
	if((year % 400U) == 0U)
	{
		return 1U;
	}
	if((year % 100U) == 0U)
	{
		return 0U;
	}
	if((year % 4U) == 0U)
	{
		return 1U;
	}
	return 0U;
}

static u8 GetMonthDays(u16 year, u8 month)
{
	static const u8 month_days[12] = {31,28,31,30,31,30,31,31,30,31,30,31};

	if((month < 1U) || (month > 12U))
	{
		return 30U;
	}
	if((month == 2U) && IsLeapYear(year))
	{
		return 29U;
	}
	return month_days[month - 1U];
}

static u8 GetWeekday(u16 year, u8 month, u8 day)
{
	u16 calc_year = year;
	u8 calc_month = month;
	u16 century;
	u16 year_in_century;
	u16 result;

	if(calc_month < 3U)
	{
		calc_month += 12U;
		calc_year--;
	}

	century = calc_year / 100U;
	year_in_century = calc_year % 100U;

	result = (u16)(day + (13U * (calc_month + 1U)) / 5U + year_in_century
		+ year_in_century / 4U + century / 4U + 5U * century);
	result = (u16)(result % 7U);

	return (u8)((result + 6U) % 7U);
}

static u8 GetFirstWeekdayOfMonth(u16 year, u8 month)
{
	return GetWeekday(year, month, 1U);
}

static void DrawTextCenter(u16 x, u16 y, u16 width, u8 size, u8 *text)
{
	u16 text_len = 0U;
	u16 text_width = 0U;
	u16 start_x = x;
	u8 *p = text;

	while(*p != '\0')
	{
		text_len++;
		p++;
	}

	text_width = (u16)(text_len * (size / 2U));
	if((text_width < width) && (width > 0U))
	{
		start_x = (u16)(x + (width - text_width) / 2U);
	}

	LCD_ShowString(start_x, y, width, size + 4U, size, text);
}

static void DrawDateBadge(u16 x, u16 y, u16 width, u16 height, calendar_date_t *date)
{
	char line[32];

	LCD_Fill(x, y, x + width - 1U, y + height - 1U, WHITE);
	FRONT_COLOR = BLACK;
	LCD_DrawRectangle(x, y, x + width - 1U, y + height - 1U);

	FRONT_COLOR = DARKBLUE;
	sprintf(line, "%04u-%02u-%02u",
		(unsigned int)date->year,
		(unsigned int)date->month,
		(unsigned int)date->day);
	LCD_ShowString(x + 10U, y + 10U, width - 20U, 24U, 16U, (u8 *)line);

	FRONT_COLOR = GRAYBLUE;
	sprintf(line, "%s", g_week_name[GetWeekday(date->year, date->month, date->day)]);
	LCD_ShowString(x + 160U, y + 10U, width - 170U, 20U, 16U, (u8 *)line);

	FRONT_COLOR = BLACK;
	LCD_ShowString(x + 10U, y + 30U, width - 20U, 16U, 12U, (u8 *)"LCD Calendar Demo");
}

static void DrawCalendarHeader(calendar_date_t *date)
{
	char title[40];
	u16 screen_w = tftlcd_data.width;

	LCD_Fill(0U, 0U, screen_w - 1U, 52U, DARKBLUE);
	FRONT_COLOR = WHITE;
	sprintf(title, "%s %u", g_month_name[date->month - 1U], (unsigned int)date->year);
	DrawTextCenter(0U, 10U, screen_w, 24U, (u8 *)title);

	FRONT_COLOR = LIGHTGRAY;
	DrawTextCenter(0U, 34U, screen_w, 12U, (u8 *)"Month View");
}

static void DrawCalendarWeekTitle(u16 x, u16 y, u16 cell_w, u16 cell_h)
{
	u8 index;

	for(index = 0U; index < 7U; index++)
	{
		u16 left = (u16)(x + index * cell_w);
		u16 right = (u16)(left + cell_w - 1U);

		if(index == 0U)
		{
			LCD_Fill(left, y, right, y + cell_h - 1U, BRRED);
		}
		else if(index == 6U)
		{
			LCD_Fill(left, y, right, y + cell_h - 1U, LIGHTBLUE);
		}
		else
		{
			LCD_Fill(left, y, right, y + cell_h - 1U, LGRAYBLUE);
		}

		FRONT_COLOR = WHITE;
		DrawTextCenter(left, y + 8U, cell_w, 16U, (u8 *)g_week_name[index]);
	}
}

static void DrawCalendarGrid(calendar_date_t *date, u16 x, u16 y, u16 width, u16 height)
{
	u8 first_weekday;
	u8 month_days;
	u8 prev_month_days;
	u8 row;
	u8 col;
	u16 cell_w;
	u16 cell_h;
	s16 day_cursor;

	cell_w = (u16)(width / 7U);
	cell_h = (u16)(height / 6U);

	first_weekday = GetFirstWeekdayOfMonth(date->year, date->month);
	month_days = GetMonthDays(date->year, date->month);
	if(date->month == 1U)
	{
		prev_month_days = GetMonthDays((u16)(date->year - 1U), 12U);
	}
	else
	{
		prev_month_days = GetMonthDays(date->year, (u8)(date->month - 1U));
	}

	day_cursor = (s16)(1 - first_weekday);

	for(row = 0U; row < 6U; row++)
	{
		for(col = 0U; col < 7U; col++)
		{
			u16 left = (u16)(x + col * cell_w);
			u16 top = (u16)(y + row * cell_h);
			u16 right = (u16)(left + cell_w - 1U);
			u16 bottom = (u16)(top + cell_h - 1U);
			u16 fill_color = WHITE;
			u16 text_color = BLACK;
			u8 display_day;
			char day_text[4];

			if(day_cursor < 1)
			{
				display_day = (u8)(prev_month_days + day_cursor);
				fill_color = LGRAY;
				text_color = GRAYBLUE;
			}
			else if(day_cursor > month_days)
			{
				display_day = (u8)(day_cursor - month_days);
				fill_color = LGRAY;
				text_color = GRAYBLUE;
			}
			else
			{
				display_day = (u8)day_cursor;
				if((date->day == display_day) && (date->month >= 1U) && (date->month <= 12U))
				{
					fill_color = YELLOW;
					text_color = RED;
				}
				else if((col == 0U) || (col == 6U))
				{
					fill_color = LIGHTGRAY;
					text_color = DARKBLUE;
				}
			}

			LCD_Fill(left, top, right, bottom, fill_color);
			FRONT_COLOR = GRAYBLUE;
			LCD_DrawRectangle(left, top, right, bottom);

			sprintf(day_text, "%2u", (unsigned int)display_day);
			FRONT_COLOR = text_color;
			DrawTextCenter(left, (u16)(top + 12U), cell_w, 16U, (u8 *)day_text);

			if((day_cursor >= 1) && (day_cursor <= month_days) && (display_day == date->day))
			{
				LCD_DrawLine_Color((u16)(left + 8U), (u16)(bottom - 8U), (u16)(right - 8U), (u16)(bottom - 8U), RED);
			}

			day_cursor++;
		}
	}
}

static void DrawCalendarPage(calendar_date_t *date)
{
	u16 screen_w = tftlcd_data.width;
	u16 screen_h = tftlcd_data.height;
	u16 grid_x = 8U;
	u16 grid_y = 150U;
	u16 grid_w = (u16)(screen_w - 16U);
	u16 grid_h = (u16)(screen_h - grid_y - 10U);
	u16 week_h = 28U;

	BACK_COLOR = WHITE;
	FRONT_COLOR = BLACK;
	LCD_Clear(WHITE);

	DrawCalendarHeader(date);
	DrawDateBadge(10U, 62U, (u16)(screen_w - 20U), 46U, date);
	DrawCalendarWeekTitle(grid_x, (u16)(grid_y - week_h - 6U), (u16)(grid_w / 7U), week_h);
	DrawCalendarGrid(date, grid_x, grid_y, grid_w, grid_h);
}

static u8 MatchMonthToken(const char *token, const char *month)
{
	u8 index;

	for(index = 0U; index < 3U; index++)
	{
		if(token[index] != month[index])
		{
			return 0U;
		}
	}
	return 1U;
}

static void ParseBuildDate(calendar_date_t *date)
{
	const char *build_date = __DATE__;
	u8 month_index;
	u16 year_value;
	u8 day_tens;
	u8 day_units;

	date->year = 2026U;
	date->month = 5U;
	date->day = 25U;

	for(month_index = 0U; month_index < 12U; month_index++)
	{
		if(MatchMonthToken(build_date, g_month_name[month_index]) != 0U)
		{
			date->month = (u8)(month_index + 1U);
			break;
		}
	}

	day_tens = (build_date[4] == ' ') ? 0U : (u8)(build_date[4] - '0');
	day_units = (u8)(build_date[5] - '0');
	date->day = (u8)(day_tens * 10U + day_units);

	year_value = (u16)((build_date[7] - '0') * 1000
		+ (build_date[8] - '0') * 100
		+ (build_date[9] - '0') * 10
		+ (build_date[10] - '0'));

	if((year_value >= CALENDAR_YEAR_MIN) && (year_value <= CALENDAR_YEAR_MAX))
	{
		date->year = year_value;
	}
}

/*******************************************************************************
* Function Name  : main
* Description    : Main program.
*******************************************************************************/
int main(void)
{
	u8 led_tick = 0U;
	calendar_date_t today;

	HAL_Init();
	SystemClock_Init(RCC_PLL_MUL9);
	SysTick_Init(72);
	USART1_Init(115200);
	LED_Init();
	TFTLCD_Init();

	ParseBuildDate(&today);
	DrawCalendarPage(&today);

	printf("Calendar LCD: %04u-%02u-%02u %s\r\n",
		(unsigned int)today.year,
		(unsigned int)today.month,
		(unsigned int)today.day,
		g_week_name[GetWeekday(today.year, today.month, today.day)]);

	while(1)
	{
		led_tick++;
		if(led_tick >= 20U)
		{
			led_tick = 0U;
			LED1 = !LED1;
		}

		delay_ms(10);
	}
}
