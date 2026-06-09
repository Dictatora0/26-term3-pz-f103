# 摄像头通信实验

本项目已改成：

- `PC` 端使用电脑摄像头采集图像
- 通过 `USART1` 把低分辨率 `RGB565` 图像帧发送给普中 `STM32F103`
- 单片机通过 `FSMC TFTLCD` 在屏幕上放大显示

## 固件说明

- 当前主程序：`[camera_main.c](D:/Proj/5.23/摄像头通信实验/User/camera_main.c)`
- 串口驱动：`[usart_app.c](D:/Proj/5.23/摄像头通信实验/User/usart_app.c)`
- LCD 驱动：`[tftlcd.c](D:/Proj/5.23/摄像头通信实验/APP/tftlcd/tftlcd.c)`

当前参数：

- 串口：`USART1`
- 波特率：`460800`
- 图像格式：`RGB565`
- 默认图像尺寸：`80x60`
- LCD 显示：自动整数倍放大并居中

## 帧协议

单帧格式：

```text
A5 5A | width(1) | height(1) | seq(1) | payload_len(2, little-endian) | payload | crc16(2)
```

- `payload` 为 `width * height * 2` 字节
- 每个像素是 `RGB565 little-endian`
- CRC 为 `CRC16-CCITT(poly=0x1021, init=0xFFFF)`
- 单片机成功接收后返回 `0x06`
- 校验失败或帧非法返回 `0x15`

## PC 端发送脚本

脚本位置：`[camera_to_stm32.py](D:/Proj/5.23/摄像头通信实验/tools/camera_to_stm32.py)`

先安装依赖：

```powershell
pip install opencv-python pyserial numpy
```

运行示例：

```powershell
python .\tools\camera_to_stm32.py --port COM5 --baud 460800 --width 80 --height 60 --fps 3 --show
```

说明：

- `COM5` 改成你电脑识别出的普中开发板串口
- 若画面不稳，可降到 `--fps 2`
- 若串口质量较好，可尝试 `--fps 4`
- 若要看详细串口交互，追加 `--verbose`
- 若只想看单片机启动日志，不开摄像头，可用：

```powershell
python .\tools\camera_to_stm32.py --port COM5 --listen-only
```

## 验收建议

1. 用 Keil 编译并烧录当前工程。
2. 上电后 TFT 屏会先显示 `Waiting for PC camera...`
3. 电脑运行发送脚本。
4. 屏幕上应显示摄像头画面放大结果。

## 注意

- 当前 `TFTLCD` 驱动采用仓库内已有普中例程配置，默认启用的是 `HX8357DN` 宏。
- 如果你的屏幕驱动型号不同，需要改 `[tftlcd.h](D:/Proj/5.23/摄像头通信实验/APP/tftlcd/tftlcd.h)` 里的驱动宏。
- 若屏幕方向不对，可改 `TFTLCD_DIR` 或 `camera_main.c` 中的 `LCD_Display_Dir(1)`。
