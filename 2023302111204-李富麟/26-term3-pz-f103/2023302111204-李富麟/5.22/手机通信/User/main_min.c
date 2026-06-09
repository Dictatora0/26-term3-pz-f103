#include "system.h"
#include "SysTick.h"
#include "led.h"
#include "usart.h"

int main(void)
{
    SysTick_Init(72);
    NVIC_PriorityGroupConfig(NVIC_PriorityGroup_2);
    USART1_Init(115200);
    LED_Init();
    printf("F103_MIN_OPEN running\r\n");

    while (1) {
        LED1 = 0;
        delay_ms(300);
        LED1 = 1;
        delay_ms(300);
    }
}
