# STM32 + PC Dashboard (温度/光照/湿度)

本项目基于 STM32F103ZET6（StdPeriph）实验工程，已经集成：
- 内部温度采集
- 光敏采集（ADC3/PF8/通道6）
- DHT11 湿度采集（按玄武 F103 原理图默认 PG11）
- USART1 每 1 秒输出一条 JSON 数据
- PC 端 Flask 后端读取串口并提供 API
- 浏览器 Dashboard 实时显示卡片、历史表、趋势图

## 目录结构

- `APP/adc_temp/`：温度、光照、湿度采集逻辑
- `User/main.c`：主循环，1 秒串口输出
- `pc_dashboard/backend/`：Python 后端
- `pc_dashboard/frontend/`：前端页面

## STM32 串口数据格式（当前实际）

每秒一行 JSON：

```json
{"temp_centi":2836,"light":22,"humidity":61,"timestamp_ms":11000}
```

字段说明：
- `temp_centi`：温度 x100（`2836` 表示 `28.36°C`）
- `light`：光照百分比（0~100）
- `humidity`：湿度 `%RH`（读取失败时为 `null`）
- `timestamp_ms`：板端运行毫秒计数

## STM32 编译与下载

1. Keil 打开 `Template.uvprojx`
2. 确认工程配置：
   - `USE_STDPERIPH_DRIVER`
   - `STM32F10X_HD`
   - 启动文件 `startup_stm32f10x_hd.s`
3. 编译下载到开发板
4. 串口参数：`115200 8N1`

## PC 端启动

请在后端目录执行：

```powershell
cd D:\Proj\5.21\温度光照仪表盘\pc_dashboard\backend
python -m venv .venv
.\.venv\Scripts\activate
pip install -r requirements.txt
python app.py --port COM3 --baudrate 115200
```

浏览器访问：

- `http://127.0.0.1:5000`

## API

- `GET /api/latest`：最新数据
- `GET /api/history`：最近 100 条历史
- `GET /api/debug`：调试状态（`read_count`、`parse_ok`、`latest`、`recent_raw_lines`）

## 湿度接入说明

当前湿度走 DHT11，默认引脚：`PG11`。

- 读取成功：串口输出 `"humidity":数字`
- 读取失败：串口输出 `"humidity":null`

如果你的 DHT11 不在 `PG11`，修改文件：
- `APP/adc_temp/adc_temp.c`

修改宏：
- `HUM_PORT`
- `HUM_PIN`
- `HUM_RCC`

## 快速验证湿度是否成功

1. 看串口原始行：`humidity` 是否不是长期 `null`
2. 看 `http://127.0.0.1:5000/api/debug`：
   - `read_count` 持续增长
   - `parse_ok` 持续增长
   - `latest.humidity` 出现数值
3. 看 Dashboard：湿度卡片从“暂未接入”变成数值

## 常见问题

1. 串口被占用
- 关闭串口助手、Keil 串口监视、其他占用 COM 的程序

2. COM 口错误
- 设备管理器确认端口，启动时用 `--port COMx`

3. 波特率不一致
- STM32 和后端都要 `115200`

4. 下载成功但网页无数据
- 先查 `api/debug` 的 `read_count/parse_ok`
- 若 `read_count=0`，通常是串口没真正读到板子输出
- 若 `read_count>0 parse_ok=0`，检查串口输出格式

5. 页面显示“等待数据”
- 通常是后端已连上串口但还未收到第一条可解析 JSON

## 后续换其他湿度传感器（如 SHT3x）

需要改：

1. STM32：新增 I2C + SHT3x 驱动，替换 `Humidity_Read()` 数据来源
2. 后端：`serial_reader.py` 已支持 `humidity` 字段，通常不用改
3. 前端：`app.js` 已支持湿度展示，通常不用改
