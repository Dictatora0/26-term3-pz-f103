#include "car_telemetry_report.h"

#include <math.h>
#include <stdint.h>
#include <stdio.h>

#include "cmsis_os2.h"
#include "hi_i2c.h"
#include "hi_io.h"
#include "hi_time.h"
#include "iot_errno.h"
#include "iot_gpio.h"
#include "iot_gpio_ex.h"
#include "iot_i2c.h"

#define CAR_TELEMETRY_TASK_STACK_SIZE (4 * 1024)
#define CAR_TELEMETRY_SENSOR_INTERVAL_MS 100
#define CAR_TELEMETRY_REPORT_INTERVAL_MS 1000
#define CAR_DEVICE_ID "hi3861_car_01"
#define CAR_DEVICE_TYPE "car"
#define CAR_DISTANCE_TRIG_GPIO IOT_IO_NAME_GPIO_7
#define CAR_DISTANCE_ECHO_GPIO IOT_IO_NAME_GPIO_8
#define CAR_DISTANCE_WAIT_TIMEOUT_US 30000
#define CAR_DISTANCE_PULSE_TIMEOUT_US 30000
#define CAR_IMU_I2C_IDX 0
#define CAR_IMU_I2C_BAUDRATE 400000
#define CAR_IMU_SDA_GPIO IOT_IO_NAME_GPIO_13
#define CAR_IMU_SCL_GPIO IOT_IO_NAME_GPIO_14
#define CAR_IMU_WHO_AM_I_REG 0x0F
#define CAR_IMU_STATUS_REG 0x1E
#define CAR_IMU_OUTX_L_G 0x22
#define CAR_IMU_CTRL1_XL 0x10
#define CAR_IMU_CTRL2_G 0x11
#define CAR_IMU_CTRL3_C 0x12
#define CAR_IMU_CTRL8_XL 0x17
#define CAR_IMU_CTRL10_C 0x19
#define CAR_IMU_TAP_CFG 0x58
#define CAR_IMU_TAP_THS_6D 0x59
#define CAR_IMU_WAKE_UP_THS 0x5B
#define CAR_IMU_WAKE_UP_DUR 0x5C
#define CAR_IMU_WRITE_ADDR 0xD4
#define CAR_IMU_READ_ADDR 0xD5
#define CAR_IMU_GYRO_READY_MASK 0x02
#define CAR_IMU_GYRO_DEADBAND_DPS 2.5f
#define CAR_IMU_MAX_SAMPLE_INTERVAL_S 0.20f

static volatile CarReportStatus g_carStatus = CAR_REPORT_STATUS_IDLE;
static volatile CarReportDirection g_carDirection = CAR_REPORT_DIRECTION_STOP;
static volatile CarReportMotionState g_carMotionState = CAR_REPORT_MOTION_IDLE;
static volatile int g_carSpeed = 0;
static volatile int g_distanceValid = 0;
static volatile float g_distanceCm = 0.0f;
static volatile int g_imuValid = 0;
static volatile float g_yawDeg = 0.0f;
static volatile float g_gyroZDps = 0.0f;
static unsigned long g_lastImuSampleUs = 0;

static const char *Car_GetStatus(void)
{
    switch (g_carStatus) {
        case CAR_REPORT_STATUS_RUNNING:
            return "RUNNING";
        case CAR_REPORT_STATUS_STOPPED:
            return "STOPPED";
        case CAR_REPORT_STATUS_ERROR:
            return "ERROR";
        case CAR_REPORT_STATUS_IDLE:
        default:
            return "IDLE";
    }
}

static const char *Car_GetMotionState(void)
{
    switch (g_carMotionState) {
        case CAR_REPORT_MOTION_STARTUP:
            return "STARTUP";
        case CAR_REPORT_MOTION_CRUISE:
            return "CRUISE";
        case CAR_REPORT_MOTION_CAUTIOUS:
            return "CAUTIOUS";
        case CAR_REPORT_MOTION_OBSTACLE_DETECTED:
            return "OBSTACLE_DETECTED";
        case CAR_REPORT_MOTION_REVERSING:
            return "REVERSING";
        case CAR_REPORT_MOTION_TURNING_LEFT:
            return "TURNING_LEFT";
        case CAR_REPORT_MOTION_TURNING_RIGHT:
            return "TURNING_RIGHT";
        case CAR_REPORT_MOTION_RECOVERING:
            return "RECOVERING";
        case CAR_REPORT_MOTION_STOPPED:
            return "STOPPED";
        case CAR_REPORT_MOTION_ERROR:
            return "ERROR";
        case CAR_REPORT_MOTION_IDLE:
        default:
            return "IDLE";
    }
}

static const char *Car_GetDirection(void)
{
    switch (g_carDirection) {
        case CAR_REPORT_DIRECTION_FORWARD:
            return "FORWARD";
        case CAR_REPORT_DIRECTION_BACKWARD:
            return "BACKWARD";
        case CAR_REPORT_DIRECTION_LEFT:
            return "LEFT";
        case CAR_REPORT_DIRECTION_RIGHT:
            return "RIGHT";
        case CAR_REPORT_DIRECTION_STOP:
        default:
            return "STOP";
    }
}

static int Car_GetSpeed(void)
{
    int speed = g_carSpeed;
    if (speed < 0) {
        speed = 0;
    }
    if (speed > 100) {
        speed = 100;
    }
    return speed;
}

static float Car_NormalizeYawDegrees(float yaw_deg)
{
    float yaw = yaw_deg;

    while (yaw > 180.0f) {
        yaw -= 360.0f;
    }
    while (yaw < -180.0f) {
        yaw += 360.0f;
    }
    return yaw;
}

static void CarDistanceSensorInit(void)
{
    IoTGpioInit(CAR_DISTANCE_TRIG_GPIO);
    IoTGpioInit(CAR_DISTANCE_ECHO_GPIO);

    IoSetFunc(CAR_DISTANCE_ECHO_GPIO, IOT_IO_FUNC_GPIO_8_GPIO);
    IoTGpioSetDir(CAR_DISTANCE_ECHO_GPIO, IOT_GPIO_DIR_IN);

    IoSetFunc(CAR_DISTANCE_TRIG_GPIO, IOT_IO_FUNC_GPIO_7_GPIO);
    IoTGpioSetDir(CAR_DISTANCE_TRIG_GPIO, IOT_GPIO_DIR_OUT);
    IoTGpioSetOutputVal(CAR_DISTANCE_TRIG_GPIO, IOT_GPIO_VALUE0);
}

static int CarImuWriteReg(uint8_t reg_addr, uint8_t reg_value)
{
    uint8_t buffer[2] = {reg_addr, reg_value};
    unsigned int retval = IoTI2cWrite(CAR_IMU_I2C_IDX, CAR_IMU_WRITE_ADDR, buffer, sizeof(buffer));

    if (retval != IOT_SUCCESS) {
        printf("[car_telemetry] imu write failed reg=0x%02X err=0x%X\r\n", reg_addr, retval);
        return 0;
    }
    return 1;
}

static int CarImuReadCont(uint8_t reg_addr, uint8_t *buffer, uint16_t read_len)
{
    hi_i2c_data i2c_attr = {0};

    if (buffer == NULL || read_len == 0) {
        return 0;
    }

    i2c_attr.send_buf = &reg_addr;
    i2c_attr.send_len = 1;
    i2c_attr.receive_buf = buffer;
    i2c_attr.receive_len = read_len;

    return hi_i2c_writeread(CAR_IMU_I2C_IDX, CAR_IMU_READ_ADDR, &i2c_attr) == IOT_SUCCESS;
}

static int CarImuReadByte(uint8_t reg_addr, uint8_t *value)
{
    return CarImuReadCont(reg_addr, value, 1);
}

static void CarImuInit(void)
{
    uint8_t who_am_i = 0;

    IoTI2cInit(CAR_IMU_I2C_IDX, CAR_IMU_I2C_BAUDRATE);
    IoTI2cSetBaudrate(CAR_IMU_I2C_IDX, CAR_IMU_I2C_BAUDRATE);
    IoSetFunc(CAR_IMU_SDA_GPIO, IOT_IO_FUNC_GPIO_13_I2C0_SDA);
    IoSetFunc(CAR_IMU_SCL_GPIO, IOT_IO_FUNC_GPIO_14_I2C0_SCL);

    if (CarImuReadByte(CAR_IMU_WHO_AM_I_REG, &who_am_i)) {
        printf("[car_telemetry] imu who_am_i=0x%02X\r\n", who_am_i);
    } else {
        printf("[car_telemetry] imu who_am_i read failed\r\n");
    }

    CarImuWriteReg(CAR_IMU_CTRL3_C, 0x34);
    CarImuWriteReg(CAR_IMU_CTRL2_G, 0x4C);
    CarImuWriteReg(CAR_IMU_CTRL10_C, 0x38);
    CarImuWriteReg(CAR_IMU_CTRL1_XL, 0x4F);
    CarImuWriteReg(CAR_IMU_TAP_CFG, 0x10);
    CarImuWriteReg(CAR_IMU_WAKE_UP_DUR, 0x00);
    CarImuWriteReg(CAR_IMU_WAKE_UP_THS, 0x02);
    CarImuWriteReg(CAR_IMU_TAP_THS_6D, 0x40);
    CarImuWriteReg(CAR_IMU_CTRL8_XL, 0x01);
}

/*
 * This is derived from the reference HC-SR04 implementation under
 * histreaming_demo/hcsr04.c, but narrowed to a direct distance read with
 * explicit timeouts so the telemetry task does not block forever.
 */
static int Car_TryGetDistanceCm(float *distance_cm)
{
    unsigned long wait_start = 0;
    unsigned long pulse_start = 0;
    unsigned long pulse_width_us = 0;
    IotGpioValue echo_value = IOT_GPIO_VALUE0;

    if (distance_cm == NULL) {
        return 0;
    }

    IoTGpioSetOutputVal(CAR_DISTANCE_TRIG_GPIO, IOT_GPIO_VALUE0);
    hi_udelay(2);
    IoTGpioSetOutputVal(CAR_DISTANCE_TRIG_GPIO, IOT_GPIO_VALUE1);
    hi_udelay(20);
    IoTGpioSetOutputVal(CAR_DISTANCE_TRIG_GPIO, IOT_GPIO_VALUE0);

    wait_start = hi_get_us();
    while ((hi_get_us() - wait_start) < CAR_DISTANCE_WAIT_TIMEOUT_US) {
        IoTGpioGetInputVal(CAR_DISTANCE_ECHO_GPIO, &echo_value);
        if (echo_value == IOT_GPIO_VALUE1) {
            pulse_start = hi_get_us();
            break;
        }
    }
    if (pulse_start == 0) {
        return 0;
    }

    while ((hi_get_us() - pulse_start) < CAR_DISTANCE_PULSE_TIMEOUT_US) {
        IoTGpioGetInputVal(CAR_DISTANCE_ECHO_GPIO, &echo_value);
        if (echo_value == IOT_GPIO_VALUE0) {
            pulse_width_us = hi_get_us() - pulse_start;
            break;
        }
    }

    if (pulse_width_us == 0) {
        return 0;
    }

    *distance_cm = ((float)pulse_width_us) * 0.034f / 2.0f;
    return 1;
}

static int Car_TryUpdateImu(void)
{
    uint8_t status_reg = 0;
    uint8_t buffer[12] = {0};
    int16_t ang_rate_z = 0;
    float gyro_z_dps = 0.0f;
    float delta_s = 0.0f;
    unsigned long now_us = 0;

    if (!CarImuReadByte(CAR_IMU_STATUS_REG, &status_reg)) {
        return 0;
    }
    if ((status_reg & CAR_IMU_GYRO_READY_MASK) == 0) {
        return 0;
    }
    if (!CarImuReadCont(CAR_IMU_OUTX_L_G, buffer, sizeof(buffer))) {
        return 0;
    }

    ang_rate_z = (int16_t)((buffer[5] << 8) | buffer[4]);
    gyro_z_dps = ((float)ang_rate_z) / 14.29f;
    if (fabsf(gyro_z_dps) < CAR_IMU_GYRO_DEADBAND_DPS) {
        gyro_z_dps = 0.0f;
    }

    now_us = hi_get_us();
    if (g_lastImuSampleUs != 0 && now_us > g_lastImuSampleUs) {
        delta_s = ((float)(now_us - g_lastImuSampleUs)) / 1000000.0f;
        if (delta_s > CAR_IMU_MAX_SAMPLE_INTERVAL_S) {
            delta_s = CAR_IMU_MAX_SAMPLE_INTERVAL_S;
        }
        g_yawDeg = Car_NormalizeYawDegrees(g_yawDeg + gyro_z_dps * delta_s);
    }
    g_lastImuSampleUs = now_us;
    g_gyroZDps = gyro_z_dps;
    g_imuValid = 1;
    return 1;
}

void CarTelemetryReportSetMotion(
    CarReportStatus status,
    CarReportDirection direction,
    int speed,
    CarReportMotionState motion_state
)
{
    g_carStatus = status;
    g_carDirection = direction;
    g_carSpeed = speed;
    g_carMotionState = motion_state;
}

void CarTelemetryReportGetSnapshot(CarTelemetrySnapshot *snapshot)
{
    if (snapshot == NULL) {
        return;
    }

    snapshot->status = g_carStatus;
    snapshot->direction = g_carDirection;
    snapshot->motion_state = g_carMotionState;
    snapshot->speed = Car_GetSpeed();
    snapshot->distance_valid = g_distanceValid;
    snapshot->distance_cm = g_distanceCm;
    snapshot->imu_valid = g_imuValid;
    snapshot->yaw_deg = g_yawDeg;
    snapshot->gyro_z_dps = g_gyroZDps;
}

static void CarTelemetryReportTask(void *arg)
{
    (void)arg;
    unsigned int elapsed_ms = 0;

    while (1) {
        float distance_cm = 0.0f;

        if (Car_TryGetDistanceCm(&distance_cm)) {
            g_distanceValid = 1;
            g_distanceCm = distance_cm;
        } else {
            g_distanceValid = 0;
            g_distanceCm = 0.0f;
        }

        Car_TryUpdateImu();
        elapsed_ms += CAR_TELEMETRY_SENSOR_INTERVAL_MS;

        if (elapsed_ms >= CAR_TELEMETRY_REPORT_INTERVAL_MS) {
            elapsed_ms = 0;

            printf(
                "{\"device_id\":\"%s\",\"type\":\"%s\",\"status\":\"%s\","
                "\"direction\":\"%s\",\"motion_state\":\"%s\",\"speed\":%d",
                CAR_DEVICE_ID,
                CAR_DEVICE_TYPE,
                Car_GetStatus(),
                Car_GetDirection(),
                Car_GetMotionState(),
                Car_GetSpeed()
            );
            if (g_distanceValid) {
                printf(",\"distance_cm\":%.1f", g_distanceCm);
            }
            if (g_imuValid) {
                printf(",\"yaw_deg\":%.1f,\"gyro_z_dps\":%.2f", g_yawDeg, g_gyroZDps);
            }
            printf("}\r\n");
        }

        osDelay(CAR_TELEMETRY_SENSOR_INTERVAL_MS);
    }
}

void CarTelemetryReportInit(void)
{
    osThreadAttr_t attr = {0};
    attr.name = "CarTelemetry";
    attr.stack_size = CAR_TELEMETRY_TASK_STACK_SIZE;
    attr.priority = osPriorityBelowNormal;

    CarDistanceSensorInit();
    CarImuInit();

    if (osThreadNew((osThreadFunc_t)CarTelemetryReportTask, NULL, &attr) == NULL) {
        printf("[car_telemetry] Failed to create telemetry task\r\n");
        g_carStatus = CAR_REPORT_STATUS_ERROR;
        g_carMotionState = CAR_REPORT_MOTION_ERROR;
    }
}
