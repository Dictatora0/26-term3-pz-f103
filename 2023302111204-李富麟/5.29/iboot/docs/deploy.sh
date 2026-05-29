#!/bin/bash

JAR_NAME="bootstrap.jar"
LOG_FILE="bootstrap.log"
PID_FILE="bootstrap.pid"

start() {
    if [ -f "$PID_FILE" ] && kill -0 "$(cat "$PID_FILE")" >/dev/null 2>&1; then
        echo "$JAR_NAME is already running."
        return 0
    fi
    nohup java -Xmx1g -jar  "$JAR_NAME" --spring.profiles.active=prod > "$LOG_FILE" 2>&1 &
    echo $! > "$PID_FILE"
    echo "$JAR_NAME started."
}

stop() {
    if [ ! -f "$PID_FILE" ] || ! kill -0 "$(cat "$PID_FILE")" >/dev/null 2>&1; then
        echo "$JAR_NAME is not running."
        return 0
    fi
    kill "$(cat "$PID_FILE")"
    rm "$PID_FILE"
    echo "$JAR_NAME stopped."
}

case "$1" in
    start)
        start
        ;;
    stop)
        stop
        ;;
    restart)
        stop
        start
        ;;
    *)
        echo "Usage: $0 {start|stop|restart}"
        exit 1
esac

exit 0

