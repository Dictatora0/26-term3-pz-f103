#include "adc_temp.h"
#include "SysTick.h"

#define ADC_WAIT_EOC_TIMEOUT  500000UL

static int adc_round_to_int(float value)
{
	if (value >= 0.0f)
	{
		return (int)(value + 0.5f);
	}

	return (int)(value - 0.5f);
}

static u8 ADC_WaitForEoc(ADC_TypeDef *ADCx)
{
	u32 timeout = ADC_WAIT_EOC_TIMEOUT;

	while (timeout != 0UL)
	{
		if (ADC_GetFlagStatus(ADCx, ADC_FLAG_EOC) == SET)
		{
			return 1U;
		}
		timeout--;
	}

	return 0U;
}

void ADC_Temp_Init(void)
{
	ADC_InitTypeDef ADC_InitStructure;

	RCC_APB2PeriphClockCmd(RCC_APB2Periph_ADC1, ENABLE);
	RCC_ADCCLKConfig(RCC_PCLK2_Div6);
	ADC_TempSensorVrefintCmd(ENABLE);

	ADC_InitStructure.ADC_Mode = ADC_Mode_Independent;
	ADC_InitStructure.ADC_ScanConvMode = DISABLE;
	ADC_InitStructure.ADC_ContinuousConvMode = DISABLE;
	ADC_InitStructure.ADC_ExternalTrigConv = ADC_ExternalTrigConv_None;
	ADC_InitStructure.ADC_DataAlign = ADC_DataAlign_Right;
	ADC_InitStructure.ADC_NbrOfChannel = 1U;
	ADC_Init(ADC1, &ADC_InitStructure);

	ADC_Cmd(ADC1, ENABLE);
	ADC_ResetCalibration(ADC1);
	while (ADC_GetResetCalibrationStatus(ADC1))
	{
	}

	ADC_StartCalibration(ADC1);
	while (ADC_GetCalibrationStatus(ADC1))
	{
	}
}

u16 Get_ADC_Temp_Value(u8 ch, u8 times)
{
	u32 temp_val = 0UL;
	u8 t;

	if (times == 0U)
	{
		return ADC_SENSOR_TIMEOUT_VALUE;
	}

	ADC_RegularChannelConfig(ADC1, ch, 1U, ADC_SampleTime_239Cycles5);
	for (t = 0U; t < times; t++)
	{
		ADC_SoftwareStartConvCmd(ADC1, ENABLE);
		if (ADC_WaitForEoc(ADC1) == 0U)
		{
			return ADC_SENSOR_TIMEOUT_VALUE;
		}
		temp_val += ADC_GetConversionValue(ADC1);
		delay_ms(5U);
	}

	return (u16)(temp_val / times);
}

int Get_Temperture(void)
{
	u32 adc_value;
	float voltage;
	float temperature_c;

	adc_value = Get_ADC_Temp_Value(ADC_Channel_16, 10U);
	if (adc_value == ADC_SENSOR_TIMEOUT_VALUE)
	{
		return ADC_TEMP_INVALID_CENTI;
	}

	voltage = ((float)adc_value) * (3.3f / 4095.0f);
	temperature_c = ((1.43f - voltage) / 0.0043f) + 25.0f;

	return adc_round_to_int(temperature_c * 100.0f);
}

void ADC_Light_Init(void)
{
	GPIO_InitTypeDef GPIO_InitStructure;
	ADC_InitTypeDef ADC_InitStructure;

	RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIOF | RCC_APB2Periph_ADC3, ENABLE);
	RCC_ADCCLKConfig(RCC_PCLK2_Div6);

	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_8;
	GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AIN;
	GPIO_Init(GPIOF, &GPIO_InitStructure);

	ADC_InitStructure.ADC_Mode = ADC_Mode_Independent;
	ADC_InitStructure.ADC_ScanConvMode = DISABLE;
	ADC_InitStructure.ADC_ContinuousConvMode = DISABLE;
	ADC_InitStructure.ADC_ExternalTrigConv = ADC_ExternalTrigConv_None;
	ADC_InitStructure.ADC_DataAlign = ADC_DataAlign_Right;
	ADC_InitStructure.ADC_NbrOfChannel = 1U;
	ADC_Init(ADC3, &ADC_InitStructure);

	ADC_Cmd(ADC3, ENABLE);
	ADC_ResetCalibration(ADC3);
	while (ADC_GetResetCalibrationStatus(ADC3))
	{
	}

	ADC_StartCalibration(ADC3);
	while (ADC_GetCalibrationStatus(ADC3))
	{
	}
}

u16 Get_ADC_Light_Value(u8 ch, u8 times)
{
	u32 temp_val = 0UL;
	u8 t;

	if (times == 0U)
	{
		return ADC_SENSOR_TIMEOUT_VALUE;
	}

	ADC_RegularChannelConfig(ADC3, ch, 1U, ADC_SampleTime_239Cycles5);
	for (t = 0U; t < times; t++)
	{
		ADC_SoftwareStartConvCmd(ADC3, ENABLE);
		if (ADC_WaitForEoc(ADC3) == 0U)
		{
			return ADC_SENSOR_TIMEOUT_VALUE;
		}
		temp_val += ADC_GetConversionValue(ADC3);
		delay_ms(5U);
	}

	return (u16)(temp_val / times);
}

u16 Get_Light_Raw(void)
{
	return Get_ADC_Light_Value(ADC_Channel_6, 10U);
}

u8 Get_Light_Percent(void)
{
	u32 adc_avg;

	adc_avg = Get_Light_Raw();
	if (adc_avg == ADC_SENSOR_TIMEOUT_VALUE)
	{
		return 0U;
	}

	if (adc_avg > 4000UL)
	{
		adc_avg = 4000UL;
	}

	return (u8)(100UL - (adc_avg / 40UL));
}
