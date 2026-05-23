#include "system.h"
#include "SysTick.h"
#include "led.h"
#include "usart.h"
#include "adc_temp.h"

int main()
{
    u16 tick_10ms = 0;
    u32 timestamp_ms = 0;
    int temp = 0;
    u8 light = 0;
    u8 hum = 0;
    u8 hum_ok = 0;

    SysTick_Init(72);
    NVIC_PriorityGroupConfig(NVIC_PriorityGroup_2);
    LED_Init();
    USART1_Init(115200);
    ADC_Temp_Init();
    ADC_Light_Init();
    Humidity_Init();

    while (1)
    {
        tick_10ms++;
        timestamp_ms += 10;

        if (tick_10ms % 50 == 0)
        {
            LED1 = !LED1;
        }

        if (tick_10ms % 100 == 0)
        {
            temp = Get_Temperture();
            light = Get_Light_Percent();
            hum_ok = (Humidity_Read(&hum) == 0);
            if (hum_ok)
            {
                printf("{\"temp_centi\":%d,\"light\":%u,\"humidity\":%u,\"timestamp_ms\":%lu}\r\n", temp, light, hum, timestamp_ms);
            }
            else
            {
                printf("{\"temp_centi\":%d,\"light\":%u,\"humidity\":null,\"timestamp_ms\":%lu}\r\n", temp, light, timestamp_ms);
            }
        }

        delay_ms(10);
    }
}
