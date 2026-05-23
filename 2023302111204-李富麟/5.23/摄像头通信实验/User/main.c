#include "system.h"
#include "SysTick.h"
#include "led.h"
#include "usart.h"
#include "wifi_config.h"
#include "wifi_function.h"
#include <string.h>


void (*pNet_Test)(void);

int main()
{
	char cCh;	
	
	SysTick_Init(72);
	NVIC_PriorityGroupConfig(NVIC_PriorityGroup_2);  //中断优先级分组 分2组
	USART1_Init(115200);
	LED_Init();
	WiFi_Config();
	
	PC_Usart("\r\n普中PZ-ESP8266 WiFi模块测试例程\r\n"); //打印测试例程提示信息
	PC_Usart ( "\r\n请输入模块的测试模式的编号，编号如下：\
              \r\n1.STA模式： TCP Client\
              \r\n2.AP模式： AP Server\
              \r\n3.STA+AP模式： (STA)TCP Client + (AP)TCP Server\
              \r\n输入格式为：编号+空格，点击发送\r\n" );
	
	scanf("%c", &cCh );
	switch(cCh)
	{
		case '1':
			pNet_Test = ESP8266_STA_TCP_Client;
			PC_Usart("\r\n即将进行的测试是：1.STA模式： TCP Client\r\n");
		  break;
		
		case '2':
			pNet_Test = ESP8266_AP_TCP_Server;
			PC_Usart("\r\n即将进行的测试是：2.AP模式： TCP Server\r\n");
		  break;

		case '3':
			pNet_Test = ESP8266_StaTcpClient_ApTcpServer;
			PC_Usart("\r\n即将进行的测试是：3.STA+AP模式： (STA)TCP Client + (AP)TCP Server\r\n");
		  break;

		default:
			pNet_Test = NULL;
			PC_Usart("\r\n输入错误，不进行测试！\r\n");
		  break;		
			
	}
	if(pNet_Test)
		pNet_Test();
	
	while(1);
}
