#include <math.h>
#include <unistd.h>

#include "car_telemetry_report.h"
#include "cmsis_os2.h"
#include "motor_control.h"
#include "ohos_init.h"

#define AUTO_CAR_STARTUP_DELAY_MS 600
#define AUTO_CAR_CRUISE_STEP_MS 180
#define AUTO_CAR_CAUTIOUS_DRIVE_MS 100
#define AUTO_CAR_CAUTIOUS_BRAKE_MS 80
#define AUTO_CAR_STOP_SETTLE_MS 150
#define AUTO_CAR_REVERSE_MS 450
#define AUTO_CAR_TURN_TARGET_DEG 65.0f
#define AUTO_CAR_TURN_TIMEOUT_MS 1600
#define AUTO_CAR_TURN_POLL_MS 50
#define AUTO_CAR_OBSTACLE_DISTANCE_CM 18.0f
#define AUTO_CAR_CAUTIOUS_DISTANCE_CM 30.0f

static float AutoCarNormalizeYawDelta(float current_yaw_deg, float start_yaw_deg)
{
    float delta = current_yaw_deg - start_yaw_deg;

    while (delta > 180.0f) {
        delta -= 360.0f;
    }
    while (delta < -180.0f) {
        delta += 360.0f;
    }
    return delta;
}

static void AutoCarSetMotion(
    CarReportStatus status,
    CarReportDirection direction,
    int speed,
    CarReportMotionState motion_state
)
{
    CarTelemetryReportSetMotion(status, direction, speed, motion_state);
}

static void AutoCarStopAndSettle(void)
{
    car_stop();
    AutoCarSetMotion(CAR_REPORT_STATUS_STOPPED, CAR_REPORT_DIRECTION_STOP, 0, CAR_REPORT_MOTION_RECOVERING);
    usleep(AUTO_CAR_STOP_SETTLE_MS * 1000);
}

static void AutoCarTurnWithYaw(int turn_left)
{
    CarTelemetrySnapshot snapshot = {0};
    float start_yaw_deg = 0.0f;
    int has_yaw_reference = 0;
    int elapsed_ms = 0;

    CarTelemetryReportGetSnapshot(&snapshot);
    if (snapshot.imu_valid) {
        start_yaw_deg = snapshot.yaw_deg;
        has_yaw_reference = 1;
    }

    if (turn_left) {
        car_left();
        AutoCarSetMotion(CAR_REPORT_STATUS_RUNNING, CAR_REPORT_DIRECTION_LEFT, 45, CAR_REPORT_MOTION_TURNING_LEFT);
    } else {
        car_right();
        AutoCarSetMotion(CAR_REPORT_STATUS_RUNNING, CAR_REPORT_DIRECTION_RIGHT, 45, CAR_REPORT_MOTION_TURNING_RIGHT);
    }

    while (elapsed_ms < AUTO_CAR_TURN_TIMEOUT_MS) {
        usleep(AUTO_CAR_TURN_POLL_MS * 1000);
        elapsed_ms += AUTO_CAR_TURN_POLL_MS;
        if (!has_yaw_reference) {
            continue;
        }

        CarTelemetryReportGetSnapshot(&snapshot);
        if (!snapshot.imu_valid) {
            continue;
        }
        if (fabsf(AutoCarNormalizeYawDelta(snapshot.yaw_deg, start_yaw_deg)) >= AUTO_CAR_TURN_TARGET_DEG) {
            break;
        }
    }

    AutoCarStopAndSettle();
}

static void *AutoCarTask(const char *arg)
{
    (void)arg;
    int turn_left_next = 1;
    CarTelemetrySnapshot snapshot = {0};

    GA12N20Init();
    AutoCarSetMotion(CAR_REPORT_STATUS_IDLE, CAR_REPORT_DIRECTION_STOP, 0, CAR_REPORT_MOTION_STARTUP);
    car_stop();
    usleep(AUTO_CAR_STARTUP_DELAY_MS * 1000);
    AutoCarSetMotion(CAR_REPORT_STATUS_STOPPED, CAR_REPORT_DIRECTION_STOP, 0, CAR_REPORT_MOTION_STOPPED);
    usleep(300 * 1000);

    while (1) {
        CarTelemetryReportGetSnapshot(&snapshot);

        if (!snapshot.distance_valid) {
            car_forward();
            AutoCarSetMotion(CAR_REPORT_STATUS_RUNNING, CAR_REPORT_DIRECTION_FORWARD, 50, CAR_REPORT_MOTION_CRUISE);
            usleep(AUTO_CAR_CRUISE_STEP_MS * 1000);
            continue;
        }

        if (snapshot.distance_cm <= AUTO_CAR_OBSTACLE_DISTANCE_CM) {
            car_stop();
            AutoCarSetMotion(
                CAR_REPORT_STATUS_RUNNING,
                CAR_REPORT_DIRECTION_STOP,
                0,
                CAR_REPORT_MOTION_OBSTACLE_DETECTED
            );
            usleep(AUTO_CAR_STOP_SETTLE_MS * 1000);

            car_backward();
            AutoCarSetMotion(
                CAR_REPORT_STATUS_RUNNING,
                CAR_REPORT_DIRECTION_BACKWARD,
                35,
                CAR_REPORT_MOTION_REVERSING
            );
            usleep(AUTO_CAR_REVERSE_MS * 1000);

            AutoCarStopAndSettle();
            AutoCarTurnWithYaw(turn_left_next);
            turn_left_next = !turn_left_next;
            continue;
        }

        if (snapshot.distance_cm <= AUTO_CAR_CAUTIOUS_DISTANCE_CM) {
            car_forward();
            AutoCarSetMotion(
                CAR_REPORT_STATUS_RUNNING,
                CAR_REPORT_DIRECTION_FORWARD,
                25,
                CAR_REPORT_MOTION_CAUTIOUS
            );
            usleep(AUTO_CAR_CAUTIOUS_DRIVE_MS * 1000);

            car_stop();
            AutoCarSetMotion(CAR_REPORT_STATUS_STOPPED, CAR_REPORT_DIRECTION_STOP, 0, CAR_REPORT_MOTION_CAUTIOUS);
            usleep(AUTO_CAR_CAUTIOUS_BRAKE_MS * 1000);
            continue;
        }

        car_forward();
        AutoCarSetMotion(CAR_REPORT_STATUS_RUNNING, CAR_REPORT_DIRECTION_FORWARD, 60, CAR_REPORT_MOTION_CRUISE);
        usleep(AUTO_CAR_CRUISE_STEP_MS * 1000);
    }

    return NULL;
}

static void AutoCarEntry(void)
{
    osThreadAttr_t attr = {0};
    CarTelemetryReportInit();
    attr.name = "AutoCarTask";
    attr.stack_size = 4 * 1024;
    attr.priority = osPriorityNormal;

    if (osThreadNew((osThreadFunc_t)AutoCarTask, NULL, &attr) == NULL) {
        car_stop();
        AutoCarSetMotion(CAR_REPORT_STATUS_ERROR, CAR_REPORT_DIRECTION_STOP, 0, CAR_REPORT_MOTION_ERROR);
    }
}

APP_FEATURE_INIT(AutoCarEntry);
