# ESP8266 AT Bring-Up

## 1. Current Diagnosis

If the board log looks like this:

```text
[ESP_CMD 1] TX: AT
[ESP_CMD 1] TIMEOUT no response
...
[ESP] no response at tried bauds: 9600, 115200, 74880, 57600.
```

then the failure is not on the phone side and not on the WSL Mosquitto side.

The failure is earlier:

STM32 USART3  
-> ESP8266 AT link  
-> Wi-Fi join  
-> MQTT connect  
-> phone receive

You must restore the STM32 <-> ESP8266 AT path first.

## 2. Board-Side Checks

Check these in order:

1. ESP8266 power must be stable 3.3 V. Do not power the module from a weak 3.3 V pin that sags during Wi-Fi startup.
2. STM32 and ESP8266 must share common GND.
3. USART wiring must be crossed:
   - `PB10` STM32 TX -> ESP8266 RX
   - `PB11` STM32 RX <- ESP8266 TX
4. `EN/CH_PD` must stay high.
5. `RST` must stay high except during reset pulse.
6. ESP8266 must run ESP-AT firmware. If it runs a transparent serial bridge firmware or user firmware, `AT+MQTT...` commands will never work.

## 3. Direct USB-UART Test

Before testing with STM32, test the ESP8266 alone from a USB-UART adapter.

Recommended serial parameters to try:

- `115200 8N1`
- `9600 8N1`
- `74880 8N1`
- `57600 8N1`

Send:

```text
AT
```

Expected:

```text
OK
```

Then test:

```text
AT+GMR
AT+CWMODE?
AT+UART_CUR?
```

If `AT` does not return `OK` on any baud:

- power is wrong,
- TX/RX is wrong,
- GND is missing,
- module is damaged,
- or the firmware is not ESP-AT.

If direct USB-UART works but STM32 still gets no reply:

- the STM32 USART3 path or pin mapping is wrong,
- the board-level EN/RST control is wrong,
- or the STM32-side baud does not match the module.

## 4. If Direct AT Works But Baud Is Wrong

If the module answers at a baud different from the firmware default, set it to `115200` for this project:

```text
AT+UART_CUR=115200,8,1,0,0
```

Then reboot the module and test:

```text
AT
```

again.

## 5. What Firmware Now Does

This repo now improves failure handling:

- probes `9600`, `115200`, `74880`, `57600`
- prints a hardware checklist when AT fails
- stops blind telemetry spam when MQTT is not ready
- retries MQTT bring-up every 5 seconds

So if you reflash the updated firmware and still see repeated `AT` timeout logs, treat that as a hardware or ESP firmware problem, not a phone topic problem.

## 6. After AT Is Restored

Once you can get `AT -> OK`, the expected sequence is:

1. `AT` responds
2. `AT+CWMODE=1`
3. `AT+CWJAP="DESKTOP-6NM70T","LFL-lab-204"`
4. `AT+MQTTUSERCFG...`
5. `AT+MQTTCONNCFG...`
6. `AT+MQTTCONN=0,"192.168.137.1",1883,0`
7. `AT+MQTTSUB=0,"pz103/control",0`
8. `AT+MQTTPUBRAW...`

Only after that can the phone receive `pz103/telemetry` and `pz103/status`.
