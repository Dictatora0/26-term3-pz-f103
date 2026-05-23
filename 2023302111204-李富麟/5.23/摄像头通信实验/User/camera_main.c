#include "system.h"
#include "SysTick.h"
#include "led.h"
#include "usart.h"
#include "tftlcd.h"
#include <stdio.h>

#define UART_BAUDRATE       460800UL
#define FRAME_SYNC0         0xA5
#define FRAME_SYNC1         0x5A
#define FRAME_ACK           0x06
#define FRAME_NAK           0x15
#define FRAME_DBG           0x21
#define MAX_FRAME_WIDTH     80
#define MAX_FRAME_HEIGHT    60
#define MAX_FRAME_PIXELS    (MAX_FRAME_WIDTH * MAX_FRAME_HEIGHT)
#define LCD_LINEBUF_PIXELS  800
#define DEBUG_BAR_HEIGHT    40U
#define FRAME_AREA_TOP      (DEBUG_BAR_HEIGHT + 4U)
#define UART_RX_TIMEOUT_MS  300U

typedef struct
{
	u8 width;
	u8 height;
	u8 seq;
	u16 payload_len;
} frame_header_t;

typedef enum
{
	FRAME_STATUS_WAIT = 0,
	FRAME_STATUS_SYNC_TIMEOUT,
	FRAME_STATUS_HEADER_ERROR,
	FRAME_STATUS_HEADER_TIMEOUT,
	FRAME_STATUS_LENGTH_ERROR,
	FRAME_STATUS_PAYLOAD_TIMEOUT,
	FRAME_STATUS_CRC_TIMEOUT,
	FRAME_STATUS_CRC_ERROR,
	FRAME_STATUS_OK
} frame_status_t;

static u16 g_frame_buffer[MAX_FRAME_PIXELS];
static u16 g_line_buffer[LCD_LINEBUF_PIXELS];
static u8 g_last_render_width = 0;
static u8 g_last_render_height = 0;
static u32 g_frame_ok = 0;
static u32 g_frame_bad = 0;
static u8 g_last_seq = 0;
static u8 g_last_rx_width = 0;
static u8 g_last_rx_height = 0;
static u16 g_last_payload_len = 0;
static u16 g_last_crc_expected = 0;
static u16 g_last_crc_received = 0;
static frame_status_t g_last_status = FRAME_STATUS_WAIT;
static u16 g_last_detail = 0;

static const char *frame_status_text(frame_status_t status)
{
	switch (status)
	{
	case FRAME_STATUS_SYNC_TIMEOUT:
		return "SYNC";
	case FRAME_STATUS_HEADER_ERROR:
		return "HDR";
	case FRAME_STATUS_HEADER_TIMEOUT:
		return "HDTO";
	case FRAME_STATUS_LENGTH_ERROR:
		return "LEN";
	case FRAME_STATUS_PAYLOAD_TIMEOUT:
		return "PYTO";
	case FRAME_STATUS_CRC_TIMEOUT:
		return "CRTO";
	case FRAME_STATUS_CRC_ERROR:
		return "CRC";
	case FRAME_STATUS_OK:
		return "OK";
	case FRAME_STATUS_WAIT:
	default:
		return "WAIT";
	}
}

static void lcd_show_status(void)
{
	char line[48];

	FRONT_COLOR = WHITE;
	BACK_COLOR = DARKBLUE;
	LCD_Fill(0, 0, tftlcd_data.width - 1, DEBUG_BAR_HEIGHT - 1, DARKBLUE);

	sprintf(line, "SEQ:%03u OK:%lu BAD:%lu",
		(unsigned int)g_last_seq,
		(unsigned long)g_frame_ok,
		(unsigned long)g_frame_bad);
	LCD_ShowString(4, 2, tftlcd_data.width, 16, 16, (u8 *)line);

	sprintf(line, "ST:%s %ux%u C:%04X/%04X",
		frame_status_text(g_last_status),
		(unsigned int)g_last_rx_width,
		(unsigned int)g_last_rx_height,
		(unsigned int)g_last_crc_received,
		(unsigned int)g_last_crc_expected);
	LCD_ShowString(4, 20, tftlcd_data.width, 16, 16, (u8 *)line);

	sprintf(line, "LEN:%u DT:%04X",
		(unsigned int)g_last_payload_len,
		(unsigned int)g_last_detail);
	LCD_ShowString(180, 2, tftlcd_data.width, 16, 16, (u8 *)line);
}

static u16 crc16_ccitt_update(u16 crc, u8 data)
{
	u8 bit_index;

	crc ^= (u16)data << 8;
	for (bit_index = 0; bit_index < 8; bit_index++)
	{
		if ((crc & 0x8000U) != 0U)
		{
			crc = (u16)((crc << 1) ^ 0x1021U);
		}
		else
		{
			crc <<= 1;
		}
	}
	return crc;
}

static void send_control_byte(u8 value)
{
	USART1_SendByte(value);
}

static void send_debug_packet(frame_status_t status)
{
	u8 packet[10];

	packet[0] = FRAME_DBG;
	packet[1] = (u8)status;
	packet[2] = g_last_seq;
	packet[3] = g_last_rx_width;
	packet[4] = g_last_rx_height;
	packet[5] = (u8)(g_last_payload_len & 0xFFU);
	packet[6] = (u8)(g_last_payload_len >> 8);
	packet[7] = (u8)(g_last_detail & 0xFFU);
	packet[8] = (u8)(g_last_detail >> 8);
	packet[9] = '\n';
	USART1_SendBuffer(packet, sizeof(packet));
}

static void report_failure(frame_status_t status, u16 detail)
{
	g_last_status = status;
	g_last_detail = detail;
	g_frame_bad++;
	lcd_show_status();
	send_control_byte(FRAME_NAK);
	send_debug_packet(status);
}

static u8 uart_read_or_timeout(u8 *data)
{
	return USART1_ReadByteTimeout(data, UART_RX_TIMEOUT_MS);
}

static u8 wait_for_sync(void)
{
	u8 byte0 = 0;
	u8 byte1 = 0;
	u16 skipped = 0;

	for (;;)
	{
		if (uart_read_or_timeout(&byte0) == 0U)
		{
			return 0U;
		}
		if (byte0 != FRAME_SYNC0)
		{
			skipped++;
			continue;
		}

		if (uart_read_or_timeout(&byte1) == 0U)
		{
			g_last_detail = 0xA500U;
			return 0U;
		}
		if (byte1 == FRAME_SYNC1)
		{
			g_last_detail = skipped;
			return 1U;
		}
		skipped++;
	}
}

static u8 receive_frame(frame_header_t *header)
{
	u16 pixel_count;
	u16 expected_crc = 0xFFFFU;
	u16 received_crc;
	u16 index;
	u8 low_byte;
	u8 high_byte;

	if (wait_for_sync() == 0U)
	{
		report_failure(FRAME_STATUS_SYNC_TIMEOUT, g_last_detail);
		return 0U;
	}

	if (uart_read_or_timeout(&header->width) == 0U)
	{
		report_failure(FRAME_STATUS_HEADER_TIMEOUT, 1U);
		return 0U;
	}
	if (uart_read_or_timeout(&header->height) == 0U)
	{
		report_failure(FRAME_STATUS_HEADER_TIMEOUT, 2U);
		return 0U;
	}
	if (uart_read_or_timeout(&header->seq) == 0U)
	{
		report_failure(FRAME_STATUS_HEADER_TIMEOUT, 3U);
		return 0U;
	}
	if (uart_read_or_timeout(&low_byte) == 0U)
	{
		report_failure(FRAME_STATUS_HEADER_TIMEOUT, 4U);
		return 0U;
	}
	if (uart_read_or_timeout(&high_byte) == 0U)
	{
		report_failure(FRAME_STATUS_HEADER_TIMEOUT, 5U);
		return 0U;
	}
	header->payload_len = (u16)low_byte | ((u16)high_byte << 8);
	g_last_seq = header->seq;
	g_last_rx_width = header->width;
	g_last_rx_height = header->height;
	g_last_payload_len = header->payload_len;
	g_last_crc_expected = 0U;
	g_last_crc_received = 0U;
	g_last_detail = 0U;

	if ((header->width == 0U) ||
		(header->height == 0U) ||
		(header->width > MAX_FRAME_WIDTH) ||
		(header->height > MAX_FRAME_HEIGHT))
	{
		report_failure(FRAME_STATUS_HEADER_ERROR, ((u16)header->width << 8) | header->height);
		return 0;
	}

	pixel_count = (u16)header->width * (u16)header->height;
	if (header->payload_len != (u16)(pixel_count * 2U))
	{
		report_failure(FRAME_STATUS_LENGTH_ERROR, (u16)(pixel_count * 2U));
		return 0;
	}

	for (index = 0; index < pixel_count; index++)
	{
		if (uart_read_or_timeout(&low_byte) == 0U)
		{
			report_failure(FRAME_STATUS_PAYLOAD_TIMEOUT, (u16)(index * 2U));
			return 0U;
		}
		if (uart_read_or_timeout(&high_byte) == 0U)
		{
			report_failure(FRAME_STATUS_PAYLOAD_TIMEOUT, (u16)(index * 2U + 1U));
			return 0U;
		}
		expected_crc = crc16_ccitt_update(expected_crc, low_byte);
		expected_crc = crc16_ccitt_update(expected_crc, high_byte);
		g_frame_buffer[index] = (u16)low_byte | ((u16)high_byte << 8);
	}

	if (uart_read_or_timeout(&low_byte) == 0U)
	{
		report_failure(FRAME_STATUS_CRC_TIMEOUT, 0U);
		return 0U;
	}
	if (uart_read_or_timeout(&high_byte) == 0U)
	{
		report_failure(FRAME_STATUS_CRC_TIMEOUT, 1U);
		return 0U;
	}
	received_crc = (u16)low_byte | ((u16)high_byte << 8);
	g_last_crc_expected = expected_crc;
	g_last_crc_received = received_crc;

	if (received_crc != expected_crc)
	{
		report_failure(FRAME_STATUS_CRC_ERROR, expected_crc ^ received_crc);
		return 0;
	}

	g_last_status = FRAME_STATUS_OK;
	g_last_detail = 0U;
	g_frame_ok++;
	return 1;
}

static void build_scaled_row(u8 src_width, const u16 *src_row, u16 scale_x, u16 draw_width)
{
	u16 src_x;
	u16 dst_index = 0;
	u16 repeat_x;

	for (src_x = 0; src_x < src_width; src_x++)
	{
		for (repeat_x = 0; repeat_x < scale_x; repeat_x++)
		{
			if (dst_index < draw_width)
			{
				g_line_buffer[dst_index++] = src_row[src_x];
			}
		}
	}
}

static void render_frame(u8 src_width, u8 src_height)
{
	u16 scale_x;
	u16 scale_y;
	u16 draw_width;
	u16 draw_height;
	u16 available_height;
	u16 offset_x;
	u16 offset_y;
	u16 src_y;
	u16 repeat_y;
	u16 line_y;
	const u16 *src_row;
	u16 x;

	if ((src_width == 0U) || (src_height == 0U))
	{
		return;
	}

	scale_x = tftlcd_data.width / src_width;
	available_height = tftlcd_data.height - FRAME_AREA_TOP;
	scale_y = available_height / src_height;

	if (scale_x == 0U)
	{
		scale_x = 1U;
	}
	if (scale_y == 0U)
	{
		scale_y = 1U;
	}

	draw_width = (u16)src_width * scale_x;
	draw_height = (u16)src_height * scale_y;
	offset_x = (tftlcd_data.width - draw_width) / 2U;
	offset_y = FRAME_AREA_TOP + ((available_height - draw_height) / 2U);

	if ((src_width != g_last_render_width) || (src_height != g_last_render_height))
	{
		LCD_Fill(0, FRAME_AREA_TOP, tftlcd_data.width - 1, tftlcd_data.height - 1, BLACK);
		g_last_render_width = src_width;
		g_last_render_height = src_height;
	}

	for (src_y = 0; src_y < src_height; src_y++)
	{
		src_row = &g_frame_buffer[src_y * src_width];
		build_scaled_row(src_width, src_row, scale_x, draw_width);

		for (repeat_y = 0; repeat_y < scale_y; repeat_y++)
		{
			line_y = offset_y + (u16)(src_y * scale_y) + repeat_y;
			LCD_Set_Window(offset_x, line_y, (u16)(offset_x + draw_width - 1U), line_y);
			for (x = 0; x < draw_width; x++)
			{
				LCD_WriteData_Color(g_line_buffer[x]);
			}
		}
	}
}

static void lcd_show_boot_text(void)
{
	FRONT_COLOR = WHITE;
	BACK_COLOR = BLACK;
	LCD_Clear(BLACK);
	LCD_ShowString(12, 12, tftlcd_data.width, 24, 16, (u8 *)"PZ103 Camera LCD");
	LCD_ShowString(12, 40, tftlcd_data.width, 24, 16, (u8 *)"UART frame receiver");
	LCD_ShowString(12, 68, tftlcd_data.width, 24, 16, (u8 *)"Waiting for PC camera...");
}

static void lcd_show_self_test(void)
{
	u16 block_w;

	block_w = tftlcd_data.width / 4U;
	LCD_Fill(0, 100, block_w - 1U, 179, RED);
	LCD_Fill(block_w, 100, block_w * 2U - 1U, 179, GREEN);
	LCD_Fill(block_w * 2U, 100, block_w * 3U - 1U, 179, BLUE);
	LCD_Fill(block_w * 3U, 100, tftlcd_data.width - 1U, 179, WHITE);
	LCD_ShowString(12, 190, tftlcd_data.width, 24, 16, (u8 *)"Self-test: RGBW bars");
}

int main(void)
{
	frame_header_t header;

	SysTick_Init(72);
	NVIC_PriorityGroupConfig(NVIC_PriorityGroup_2);
	USART1_Init(UART_BAUDRATE);
	LED_Init();
	TFTLCD_Init();
	LCD_Display_Dir(1);
	LCD_LED = 1;

	LED1 = 1;
	LED2 = 1;
	lcd_show_boot_text();
	lcd_show_self_test();
	lcd_show_status();

	printf("CAMLCD READY\r\n");
	printf("baud=%lu\r\n", (unsigned long)UART_BAUDRATE);
	printf("lcd=%ux%u id=0x%04X\r\n", tftlcd_data.width, tftlcd_data.height, tftlcd_data.id);
	printf("frame_max=%ux%u rgb565\r\n", MAX_FRAME_WIDTH, MAX_FRAME_HEIGHT);

	for (;;)
	{
		if (receive_frame(&header) == 0U)
		{
			LED2 = !LED2;
			continue;
		}

		render_frame(header.width, header.height);
		lcd_show_status();
		LED1 = !LED1;
		send_control_byte(FRAME_ACK);
	}
}
