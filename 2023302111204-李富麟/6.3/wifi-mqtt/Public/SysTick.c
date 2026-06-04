#include "SysTick.h"

static u8 fac_us = 0U;
static u16 fac_ms = 0U;
static volatile u32 g_systick_uptime_ms = 0U;

void SysTick_Init(u8 SYSCLK)
{
    SysTick_CLKSourceConfig(SysTick_CLKSource_HCLK_Div8);
    fac_us = SYSCLK / 8U;
    fac_ms = (u16)fac_us * 1000U;
    g_systick_uptime_ms = 0U;
}

void delay_us(u32 nus)
{
    u32 temp;

    SysTick->LOAD = nus * fac_us;
    SysTick->VAL = 0x00U;
    SysTick->CTRL |= SysTick_CTRL_ENABLE_Msk;
    do
    {
        temp = SysTick->CTRL;
    } while ((temp & 0x01U) && !(temp & (1U << 16)));
    SysTick->CTRL &= ~SysTick_CTRL_ENABLE_Msk;
    SysTick->VAL = 0x00U;
}

static void delay_nms(u16 nms)
{
    u32 temp;

    SysTick->LOAD = (u32)nms * fac_ms;
    SysTick->VAL = 0x00U;
    SysTick->CTRL |= SysTick_CTRL_ENABLE_Msk;
    do
    {
        temp = SysTick->CTRL;
    } while ((temp & 0x01U) && !(temp & (1U << 16)));
    SysTick->CTRL &= ~SysTick_CTRL_ENABLE_Msk;
    SysTick->VAL = 0x00U;
    g_systick_uptime_ms += nms;
}

void delay_ms(u16 nms)
{
    u8 repeat;
    u16 remain;

    repeat = (u8)(nms / 540U);
    remain = nms % 540U;
    while (repeat != 0U)
    {
        delay_nms(540U);
        repeat--;
    }
    if (remain != 0U)
    {
        delay_nms(remain);
    }
}

u32 SysTick_GetMs(void)
{
    return g_systick_uptime_ms;
}

u32 SysTick_GetSeconds(void)
{
    return g_systick_uptime_ms / 1000U;
}

void SysTick_ResetMs(void)
{
    g_systick_uptime_ms = 0U;
}
