#include "adc_temp.h"
#include "SysTick.h"

void ADC_Temp_Init(void)
{
    ADC_InitTypeDef ADC_InitStructure;

    RCC_APB2PeriphClockCmd(RCC_APB2Periph_ADC1, ENABLE);
    RCC_ADCCLKConfig(RCC_PCLK2_Div6); // 72M/6 = 12MHz

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
    return temp_val / times;
}

int Get_Temperture(void)
{
    u32 adc_value;
    int temp;
    double temperture;

    adc_value = Get_ADC_Temp_Value(ADC_Channel_16, 10);
    temperture = (float)adc_value * (3.3 / 4096);
    temperture = (1.43 - temperture) / 0.0043 + 25;
    temp = temperture * 100;
    return temp;
}

void ADC_Light_Init(void)
{
    GPIO_InitTypeDef GPIO_InitStructure;
    ADC_InitTypeDef ADC_InitStructure;

    RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIOF | RCC_APB2Periph_ADC3, ENABLE);
    RCC_ADCCLKConfig(RCC_PCLK2_Div6); // 72M/6 = 12MHz

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
    return temp_val / times;
}

u8 Get_Light_Percent(void)
{
    u32 adc_avg;

    adc_avg = Get_ADC_Light_Value(ADC_Channel_6, 10); // PF8 -> ADC3_CH6
    if (adc_avg > 4000)
    {
        adc_avg = 4000;
    }

    return (u8)(100 - (adc_avg / 40));
}

/* -------------------- DHT11 humidity (board schematic: PG11) -------------------- */
#define HUM_PORT GPIOG
#define HUM_PIN GPIO_Pin_11
#define HUM_RCC RCC_APB2Periph_GPIOG

static void Hum_Pin_Output(void)
{
    GPIO_InitTypeDef GPIO_InitStructure;
    GPIO_InitStructure.GPIO_Pin = HUM_PIN;
    GPIO_InitStructure.GPIO_Mode = GPIO_Mode_Out_PP;
    GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
    GPIO_Init(HUM_PORT, &GPIO_InitStructure);
}

static void Hum_Pin_Input(void)
{
    GPIO_InitTypeDef GPIO_InitStructure;
    GPIO_InitStructure.GPIO_Pin = HUM_PIN;
    GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IN_FLOATING;
    GPIO_Init(HUM_PORT, &GPIO_InitStructure);
}

static void Hum_Pin_High(void)
{
    GPIO_SetBits(HUM_PORT, HUM_PIN);
}

static void Hum_Pin_Low(void)
{
    GPIO_ResetBits(HUM_PORT, HUM_PIN);
}

static u8 Hum_Pin_Read(void)
{
    return GPIO_ReadInputDataBit(HUM_PORT, HUM_PIN) ? 1 : 0;
}

static u8 Hum_Wait_Level(u8 level, u16 timeout_us)
{
    while (timeout_us--)
    {
        if (Hum_Pin_Read() == level)
        {
            return 1;
        }
        delay_us(1);
    }
    return 0;
}

static u8 Hum_Start_Signal_And_Check(void)
{
    Hum_Pin_Output();
    Hum_Pin_Low();
    delay_ms(20);
    Hum_Pin_High();
    delay_us(30);
    Hum_Pin_Input();

    if (!Hum_Wait_Level(0, 100))
    {
        return 2;
    }
    if (!Hum_Wait_Level(1, 100))
    {
        return 3;
    }
    if (!Hum_Wait_Level(0, 100))
    {
        return 4;
    }

    return 0;
}

static u8 Hum_Read_Bit(u8 *bit_value)
{
    if (bit_value == 0)
    {
        return 10;
    }

    if (!Hum_Wait_Level(1, 80))
    {
        return 11;
    }

    delay_us(35);
    *bit_value = Hum_Pin_Read();

    if (!Hum_Wait_Level(0, 120))
    {
        return 12;
    }

    return 0;
}

static u8 Hum_Read_Frame(u8 *buf5)
{
    u8 i;
    u8 j;
    u8 bit_value;
    u8 ret;

    if (buf5 == 0)
    {
        return 20;
    }

    ret = Hum_Start_Signal_And_Check();
    if (ret != 0)
    {
        return ret;
    }

    for (i = 0; i < 5; i++)
    {
        buf5[i] = 0;
        for (j = 0; j < 8; j++)
        {
            ret = Hum_Read_Bit(&bit_value);
            if (ret != 0)
            {
                return ret;
            }
            buf5[i] <<= 1;
            buf5[i] |= bit_value;
        }
    }

    return 0;
}

u8 Humidity_Init(void)
{
    RCC_APB2PeriphClockCmd(HUM_RCC, ENABLE);
    Hum_Pin_Output();
    Hum_Pin_High();
    delay_ms(2);
    return 0;
}

u8 Humidity_Read(u8 *humidity)
{
    u8 buf[5];
    u8 ret;
    u8 retry;

    if (humidity == 0)
    {
        return 1;
    }

    for (retry = 0; retry < 2; retry++)
    {
        ret = Hum_Read_Frame(buf);
        if (ret == 0)
        {
            if (((u8)(buf[0] + buf[1] + buf[2] + buf[3])) != buf[4])
            {
                continue;
            }

            *humidity = buf[0];

            Hum_Pin_Output();
            Hum_Pin_High();
            return 0;
        }
    }

    Hum_Pin_Output();
    Hum_Pin_High();
    return 5;
}
