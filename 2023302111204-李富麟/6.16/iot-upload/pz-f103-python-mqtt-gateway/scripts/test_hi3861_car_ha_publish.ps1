mosquitto_pub -h 127.0.0.1 -p 1883 -t iot/hi3861_car_01/status -m "RUNNING"
mosquitto_pub -h 127.0.0.1 -p 1883 -t iot/hi3861_car_01/direction -m "FORWARD"
mosquitto_pub -h 127.0.0.1 -p 1883 -t iot/hi3861_car_01/speed -m "60"
mosquitto_pub -h 127.0.0.1 -p 1883 -t iot/hi3861_car_01/distance_cm -m "35.2"
