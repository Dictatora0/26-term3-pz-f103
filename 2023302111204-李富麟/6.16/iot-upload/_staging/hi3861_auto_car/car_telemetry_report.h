#ifndef CAR_TELEMETRY_REPORT_H
#define CAR_TELEMETRY_REPORT_H

typedef enum {
    CAR_REPORT_STATUS_IDLE = 0,
    CAR_REPORT_STATUS_RUNNING,
    CAR_REPORT_STATUS_STOPPED,
    CAR_REPORT_STATUS_ERROR,
} CarReportStatus;

typedef enum {
    CAR_REPORT_DIRECTION_FORWARD = 0,
    CAR_REPORT_DIRECTION_BACKWARD,
    CAR_REPORT_DIRECTION_LEFT,
    CAR_REPORT_DIRECTION_RIGHT,
    CAR_REPORT_DIRECTION_STOP,
} CarReportDirection;

typedef enum {
    CAR_REPORT_MOTION_IDLE = 0,
    CAR_REPORT_MOTION_STARTUP,
    CAR_REPORT_MOTION_CRUISE,
    CAR_REPORT_MOTION_CAUTIOUS,
    CAR_REPORT_MOTION_OBSTACLE_DETECTED,
    CAR_REPORT_MOTION_REVERSING,
    CAR_REPORT_MOTION_TURNING_LEFT,
    CAR_REPORT_MOTION_TURNING_RIGHT,
    CAR_REPORT_MOTION_RECOVERING,
    CAR_REPORT_MOTION_STOPPED,
    CAR_REPORT_MOTION_ERROR,
} CarReportMotionState;

typedef struct {
    CarReportStatus status;
    CarReportDirection direction;
    CarReportMotionState motion_state;
    int speed;
    int distance_valid;
    float distance_cm;
    int imu_valid;
    float yaw_deg;
    float gyro_z_dps;
} CarTelemetrySnapshot;

void CarTelemetryReportInit(void);
void CarTelemetryReportSetMotion(
    CarReportStatus status,
    CarReportDirection direction,
    int speed,
    CarReportMotionState motion_state
);
void CarTelemetryReportGetSnapshot(CarTelemetrySnapshot *snapshot);

#endif /* CAR_TELEMETRY_REPORT_H */
