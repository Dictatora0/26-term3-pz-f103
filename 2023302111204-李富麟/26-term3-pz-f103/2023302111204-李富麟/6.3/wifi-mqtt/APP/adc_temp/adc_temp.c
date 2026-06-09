#include "adc_temp.h"
#include "SysTick.h"

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
    ADC_InitStructure.ADC_NbrOfChannel = 1;
    ADC_Init(ADC1, &ADC_InitStructure);

    ADC_Cmd(ADC1, ENABLE);

    ADC_ResetCalibration(ADC1);
    while (ADC_GetResetCalibrationStatus(ADC1));

    ADC_StartCalibration(ADC1);
    while (ADC_GetCalibrationStatus(ADC1));

    ADC_SoftwareStartConvCmd(ADC1, ENABLE);
}

u16 Get_ADC_Temp_Value(u8 ch, u8 times)
{
    u32 temp_val = 0;
    u8 t;

    ADC_RegularChannelConfig(ADC1, ch, 1, ADC_SampleTime_239Cycles5);
    for (t = 0; t < times; t++)
    {
        ADC_SoftwareStartConvCmd(ADC1, ENABLE);
        while (!ADC_GetFlagStatus(ADC1, ADC_FLAG_EOC));
        temp_val += ADC_GetConversionValue(ADC1);
        delay_ms(5);
    }

    return (u16)(temp_val / times);
}

int Get_Temperture(void)
{
    u32 adc_value;
    int temp;
    double temperture;

    adc_value = Get_ADC_Temp_Value(ADC_Channel_16, 10);
    temperture = (float)adc_value * (3.3 / 4096);
    temperture = (1.43 - temperture) / 0.0043 + 25;
    temp = (int)(temperture * 100);
    return temp;
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
    ADC_InitStructure.ADC_NbrOfChannel = 1;
    ADC_Init(ADC3, &ADC_InitStructure);

    ADC_Cmd(ADC3, ENABLE);

    ADC_ResetCalibration(ADC3);
    while (ADC_GetResetCalibrationStatus(ADC3));

    ADC_StartCalibration(ADC3);
    while (ADC_GetCalibrationStatus(ADC3));

    ADC_SoftwareStartConvCmd(ADC3, ENABLE);
}

u16 Get_ADC_Light_Value(u8 ch, u8 times)
{
    u32 temp_val = 0;
    u8 t;

    ADC_RegularChannelConfig(ADC3, ch, 1, ADC_SampleTime_239Cycles5);
    for (t = 0; t < times; t++)
    {
        ADC_SoftwareStartConvCmd(ADC3, ENABLE);
        while (!ADC_GetFlagStatus(ADC3, ADC_FLAG_EOC));
        temp_val += ADC_GetConversionValue(ADC3);
        delay_ms(5);
    }

    return (u16)(temp_val / times);
}

u8 Get_Light_Percent(void)
{
    u32 adc_avg;

    adc_avg = Get_ADC_Light_Value(ADC_Channel_6, 10);
    if (adc_avg > 4000U)
    {
        adc_avg = 4000U;
    }

    return (u8)(100U - (adc_avg / 40U));
}
