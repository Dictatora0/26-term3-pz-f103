# iBOOT / JetLinks / MQTT / 普中 F103 验收说明

## 1. 文档范围

本说明只保留这次课程验收真正需要讲清楚的内容：

- iBOOT、JetLinks、MySQL、Redis、Elasticsearch、Nginx、Mosquitto 如何启动
- 如何从停机状态快速恢复到可验收状态
- MQTT Broker 当前如何运行
- iBOOT 中如何查看 MQTT 子设备
- 普中 F103 + ESP8266 开发板如何接入 iBOOT，并上报温湿度、接收 LED 控制
- 本轮已完成的 HTTPS、OAuth2、JWT、RBAC 安全验证如何复现

当前工作目录：

```powershell
D:\Proj\5.29
```

本次实际交付涉及目录：

- iBOOT 后端：`D:\Proj\5.29\iboot-jetlinks`
- iBOOT 前端：`D:\Proj\5.29\iboot-v3`
- JetLinks 后端：`D:\Proj\5.29\jetlinks-community-master\jetlinks-community-master`
- 部署配置：`D:\Proj\5.29\deploy`
- 开发板工程：`D:\Proj\6.3\wifi-mqtt`

## 2. 当前机器实际运行结构

### 2.1 实际入口

- iBOOT 前端：`http://127.0.0.1/`
- iBOOT 后端 HTTPS API：`https://127.0.0.1:8443/api`
- JetLinks 管理页：`http://127.0.0.1:9000/admin/index.html`
- JetLinks 后端：`http://127.0.0.1:8848`
- MySQL：`127.0.0.1:3306`
- Redis：`127.0.0.1:6379`
- Elasticsearch：`127.0.0.1:9200`
- MQTT Broker：`127.0.0.1:1883`

### 2.2 当前关键服务名

当前机器上实际使用的 Windows 服务：

- `MySQL`
- `redis-service`
- `elasticsearch-service-x64`
- `jetlinks-standalone-service`
- `iboot-jetlinks-service`
- `nginx-service`
- `mosquitto`

### 2.3 当前 iBOOT 关键配置

配置文件：

```powershell
D:\Proj\5.29\deploy\iboot\config\application-prod.yml
```

当前关键点：

- HTTPS 端口：`8443`
- `context-path`：`/api`
- SSL 证书：`D:/Proj/5.29/deploy/iboot/certs/iboot-local.p12`
- MySQL：`jdbc:mysql://127.0.0.1:3306/iboot`
- Redis：`127.0.0.1:6379`
- 已启用 JWT
- 当前补齐了：
  - `framework.security.oauth2-issuer`
  - `framework.security.jwt-secret`

### 2.4 当前 JetLinks 关键配置

配置文件：

```powershell
D:\Proj\5.29\deploy\jetlinks\config\application-prod.yml
```

当前关键点：

- 端口：`8848`
- MySQL 数据库：`jetlinks`
- Redis：`127.0.0.1:6379`
- Elasticsearch：`http://127.0.0.1:9200`

### 2.5 当前 MQTT Broker 关键配置

当前使用的是 Windows 本机 Mosquitto 服务，不使用 WSL，不依赖 Docker。

- 服务名：`mosquitto`
- 可执行文件：`C:\Program Files\Mosquitto\mosquitto.exe`
- 已验证端口：`1883`

## 3. 一键启停脚本

### 3.1 一键启动

脚本路径：

```powershell
D:\Proj\5.29\iboot-jetlinks\scripts\security\start-acceptance.ps1
```

运行命令：

```powershell
powershell -ExecutionPolicy Bypass -File D:\Proj\5.29\iboot-jetlinks\scripts\security\start-acceptance.ps1
```

脚本作用：

1. 启动 `MySQL`
2. 启动 `Redis`
3. 启动 `Elasticsearch`
4. 等待 `3306`、`6379`、`9200` 就绪
5. 启动 `JetLinks`
6. 启动 `iBOOT`
7. 启动 `Nginx`
8. 启动 `Mosquitto`
9. 等待 `8848`、`8443`、`9000`、`1883` 就绪
10. 校验 `C:\tools\nginx\conf.d\server.iboot.conf` 是否已把 `/api/` 代理到 `https://127.0.0.1:8443`
11. 校验 OAuth2 客户端 `iboot-local-web` 是否已允许 `http://127.0.0.1/oauth/callback.html`
12. 输出服务状态、端口监听和页面探测结果

### 3.2 一键停机

脚本路径：

```powershell
D:\Proj\5.29\iboot-jetlinks\scripts\security\stop-acceptance.ps1
```

运行命令：

```powershell
powershell -ExecutionPolicy Bypass -File D:\Proj\5.29\iboot-jetlinks\scripts\security\stop-acceptance.ps1
```

脚本作用：

1. 停止 `iboot-jetlinks-service`
2. 停止 `jetlinks-standalone-service`
3. 停止 `nginx-service`
4. 停止 `mosquitto`
5. 停止 `elasticsearch-service-x64`
6. 停止 `redis-service`
7. 停止 `MySQL`

## 4. 不用脚本时的最短启动命令

### 4.1 启动全部服务

```powershell
Start-Service MySQL
Start-Service redis-service
Start-Service elasticsearch-service-x64
Start-Service jetlinks-standalone-service
Start-Service iboot-jetlinks-service
Start-Service nginx-service
Start-Service mosquitto
```

### 4.2 查看全部服务状态

```powershell
Get-Service MySQL,redis-service,elasticsearch-service-x64,nginx-service,jetlinks-standalone-service,iboot-jetlinks-service,mosquitto | Select-Object Status,Name,StartType
```

当前机器本轮实测结果是全部 `Running`。

### 4.3 核对关键端口

```powershell
Test-NetConnection 127.0.0.1 -Port 3306
Test-NetConnection 127.0.0.1 -Port 6379
Test-NetConnection 127.0.0.1 -Port 9200
Test-NetConnection 127.0.0.1 -Port 8848
Test-NetConnection 127.0.0.1 -Port 8443
Test-NetConnection 127.0.0.1 -Port 9000
Test-NetConnection 127.0.0.1 -Port 1883
```

或直接看监听：

```powershell
Get-NetTCPConnection -State Listen | Where-Object { $_.LocalPort -in 3306,6379,9200,8848,8443,9000,1883 } | Select-Object LocalAddress,LocalPort,State
```

### 4.4 核对页面和接口

```powershell
Invoke-WebRequest -UseBasicParsing http://127.0.0.1/ | Select-Object StatusCode
Invoke-WebRequest -UseBasicParsing http://127.0.0.1:9000/admin/index.html | Select-Object StatusCode
curl.exe -k -I https://127.0.0.1:8443/api/oauth2/config
curl.exe -I http://127.0.0.1:8848
```

说明：

- `http://127.0.0.1/` 返回 `200` 表示 iBOOT 前端正常
- `http://127.0.0.1:9000/admin/index.html` 返回 `200` 表示 JetLinks 管理页正常
- `https://127.0.0.1:8443/api/oauth2/config` 返回 `200` 表示 iBOOT HTTPS 安全入口正常
- `http://127.0.0.1:8848` 返回 `404` 是正常现象，表示 JetLinks 后端存活但根路径无页面

## 5. MQTT Broker 启动与验证

### 5.1 启动和状态命令

```powershell
sc.exe query mosquitto
Start-Service mosquitto
Restart-Service mosquitto
```

### 5.2 端口验证

```powershell
netstat -ano | findstr :1883
```

当前本机已验证：

- `1883` 正在监听
- `mosquitto` 处于运行中

### 5.3 本机调试连接参数

- Broker Host：`127.0.0.1`
- Broker Port：`1883`
- Username：留空
- Password：留空

### 5.4 局域网开发板连接参数

当前本机实际联网网卡是 `WLAN`，已通过 `ipconfig` 确认为：

- Windows 局域网 IPv4：`10.128.129.180`
- MQTT Broker Host：`10.128.129.180`
- Broker Port：`1883`
- Username：留空
- Password：留空

开发板、手机或另一台电脑连接 MQTT 时，不要填写 `127.0.0.1`。`127.0.0.1` 只代表客户端自己，开发板填这个地址时会连到开发板自身，不会连到 Windows 电脑上的 Mosquitto。

当前验收建议优先使用同一局域网方式：

```text
Windows 电脑连接校园网或实验室 Wi-Fi
开发板 ESP8266 连接同一个 Wi-Fi
MQTT Broker Host = 10.128.129.180
MQTT Broker Port = 1883
```

如果现场改用 Windows 本机热点，再重新执行下面命令确认热点网卡 IP：

```powershell
ipconfig
```

查找名称类似 `本地连接*`、`Local Area Connection*`、`移动热点` 或 `Mobile Hotspot` 的无线虚拟网卡。如果该网卡显示：

```text
IPv4 Address . . . . . . . . . . . : 192.168.137.1
```

则开发板程序中的 MQTT Broker Host 才改为：

```text
192.168.137.1
```

本机当前端口监听状态可用下面命令复核：

```powershell
netstat -ano | findstr :1883
```

当前已验证结果：

- `0.0.0.0:1883` 正在监听，说明局域网设备可以访问 MQTT TCP 端口
- `127.0.0.1:1883` 也可用于本机 MQTTX 或本机脚本测试
- 当前实际验收连接参数以 `10.128.129.180:1883` 为准

## 6. iBOOT 本轮实验页面与接口

本轮实验前端页面：

```powershell
D:\Proj\5.29\iboot-v3\src\views\iot\experiment\env-led\index.vue
```

本轮实验主要后端接口：

- 设备列表：`GET /api/iot/panels/devices`
- MQTT 设备列表：`GET /api/iot/panels/mqtt/devices`
- LED 控制：`POST /api/iot/panels/switchCtrlStatus`

实际权限边界：

- 查询设备：需要 `device.read` Scope，并具备 `iot:device:view`
- 控制 LED：需要 `device.control` Scope，并具备 `iot:device:ctrl`

真实链路：

```text
前端按钮
-> /api/iot/panels/switchCtrlStatus
-> 后端权限检查
-> ProtocolInvokeUtil
-> MQTT_DEFAULT_IMPL
-> Mosquitto
-> 开发板
-> 开发板状态回传
```

## 7. 如何在 iBOOT 中接入 MQTT 子设备

### 7.1 当前实际设备模型

本轮页面最终按这三个字段展示：

- `temperature`
- `humidity`
- `led`

建议设备属性也按这三个名字建模，这样页面无需额外字段映射。

### 7.2 当前开发板实际 Topic

当前开发板工程中写死并已跑通的 Topic 是：

- 上报 Topic：`env_led_node/board001/up`
- 下发 Topic：`env_led_node/board001/down`

因此 iBOOT 里产品、设备、上下行模型必须和这组实际 Topic 对齐。

### 7.3 建议的 iBOOT 配置步骤

#### 第 1 步：创建 MQTT 网关或使用已有 MQTT 默认网关

- 协议：`MQTT_DEFAULT_IMPL`
- Broker：`127.0.0.1:1883`

#### 第 2 步：创建产品

建议当前产品编码与实际开发板保持一致：

- 产品编码：`env_led_node`
- 产品名称：`ENV_LED_BOARD`
- 协议：`MQTT_DEFAULT_IMPL`

#### 第 3 步：定义属性

至少包括：

- `temperature`
- `humidity`
- `led`

其中：

- `temperature` 为数值型
- `humidity` 为数值型
- `led` 为枚举或字符串型，建议字典：
  - `0 -> off`
  - `1 -> on`

#### 第 4 步：配置上报模型

- 上报 Topic：`env_led_node/board001/up`
- 上报 Payload 示例：

```json
{"temperature":26.5,"humidity":61.0,"led":1}
```

#### 第 5 步：配置下发模型

- 下发 Topic：`env_led_node/board001/down`
- 下发 Payload 示例：

```json
{"led":1}
```

或：

```json
{"led":0}
```

#### 第 6 步：创建设备

当前实际设备编号：

- 设备号：`board001`

平台上看到的网关与子设备实际是：

- 网关：`mqtt_gateway_env_led`
- 子设备：`board001`

## 8. 普中 F103 + ESP8266 开发板工程说明

### 8.1 工程位置

开发板工程路径：

```powershell
D:\Proj\6.3\wifi-mqtt
```

Keil 工程：

```powershell
D:\Proj\6.3\wifi-mqtt\Template.uvprojx
```

已存在的构建产物：

```powershell
D:\Proj\6.3\wifi-mqtt\Obj\Template.hex
D:\Proj\6.3\wifi-mqtt\Obj\Template.axf
```

### 8.2 当前板端程序实际实现了什么

当前这份程序已经实现：

- STM32F103 采样温度
- 程序生成模拟湿度
- 通过 ESP8266 连接 Wi-Fi
- 通过 MQTT 上报 `temperature`、`humidity`、`led`
- 接收平台下发的 `led` 控制
- 控制板上 `LED1`

需要如实说明的一点：

- 当前湿度不是 DHT11 实测值
- 当前湿度是程序模拟值

因此现场可准确表述为：

```text
当前实验已经实现温度、湿度、LED 状态通过 MQTT 上报到 iBOOT，并实现平台下发 LED 控制。
其中温度来自板上采样，湿度当前为程序模拟值，后续可以继续替换为 DHT11 或 DHT22 实测。
```

### 8.3 关键源码位置

- 主程序入口：`D:\Proj\6.3\wifi-mqtt\User\main.c`
- 配置文件：`D:\Proj\6.3\wifi-mqtt\User\iot_config.h`

### 8.4 当前真实联网配置

当前开发板工程中已配置：

- Wi-Fi SSID：`DESKTOP-6NM70T`
- MQTT Host：`192.168.137.1`
- MQTT Port：`1883`
- MQTT Client ID：`env_led_node_board001`
- 产品编码：`env_led_node`
- 设备编号：`board001`
- 上报 Topic：`env_led_node/board001/up`
- 下发 Topic：`env_led_node/board001/down`

说明：

- 热点密码和其他敏感值以 `iot_config.h` 当前本机实际配置为准
- 验收讲解时不要把密码写进截图或公开文档

### 8.5 当前串口与硬件连接

根据工程当前配置：

- ESP8266 串口：`USART3`
- `PB10 -> ESP8266 RX`
- `PB11 -> ESP8266 TX`
- `PA4 -> EN/CH_PD`
- `PA15 -> RST`
- 调试串口：`USART1`
- 调试波特率：`115200`

### 8.6 当前数据来源

当前页面字段与板端来源对应关系：

| 页面字段 | 板端来源 |
| --- | --- |
| `temperature` | 板上温度采样 |
| `humidity` | 程序模拟湿度 |
| `led` | LED1 当前状态 |

### 8.7 编译与烧录

方法 1：在 Keil 中打开工程重新编译和下载

```powershell
D:\Proj\6.3\wifi-mqtt\Template.uvprojx
```

方法 2：直接使用现成产物烧录

```powershell
D:\Proj\6.3\wifi-mqtt\Obj\Template.hex
```

## 9. 当前已验证的运行结果

### 9.1 服务状态

本轮已实际验证以下服务均为 `Running`：

- `MySQL`
- `redis-service`
- `elasticsearch-service-x64`
- `jetlinks-standalone-service`
- `iboot-jetlinks-service`
- `nginx-service`
- `mosquitto`

### 9.2 iBOOT 启动已恢复

本轮已修复 iBOOT 启动问题，当前日志已出现：

```text
Started IBootApplication
```

### 9.3 MQTT 与开发板在线

本轮已在 iBOOT 日志中持续看到：

```text
接收到设备上报数据 设备编号: mqtt_gateway_env_led->board001
```

说明：

- `board001` 已在线
- 开发板正在持续上报数据
- Windows 本机 MQTT Broker、iBOOT、开发板链路已打通

### 9.4 设备状态已实测变化

本轮 `operator` 控灯后，设备状态已查询到：

- `ctrlValue: "1"`
- `led.value: "1"`
- `led.label: "on"`

说明平台下发、开发板执行、状态回传都已打通。

## 10. 安全模块运行态验收结果

### 10.1 真实安全入口

本轮必须走 iBOOT 实际生产路径：

```text
https://127.0.0.1:8443/api/...
```

不是：

```text
/oauth2/...
```

而是：

```text
/api/oauth2/...
```

另外当前页面实际从：

```text
http://127.0.0.1/
```

发起 OAuth2 登录，所以浏览器回调地址必须被登记为：

```text
http://127.0.0.1/oauth/callback.html
```

### 10.2 已验证通过的内容

本机已实际验证：

- `GET /api/oauth2/config` 可访问
- `POST /api/oauth2/doLogin` 成功
- `GET /api/oauth2/authorize` 可获取授权码
- `POST /api/oauth2/token` 可获取 `access_token` 和 `refresh_token`
- `grant_type=refresh_token` 可刷新令牌
- `POST /api/oauth2/revoke` 可撤销令牌
- 无 Token 访问设备接口返回 `401`
- 非法 Token 访问设备接口返回 `401`
- `viewer` 可以查询设备
- `viewer` 不允许控制 LED
- `operator` 可以控制 LED

### 10.3 重要说明：VIEWER 的拒绝结果

本项目当前返回风格不是 HTTP 403，而是：

- HTTP 状态码：`200`
- 业务响应体：`code = 403`

本轮实际响应体已验证为：

```json
{"data":null,"code":403,"message":"没有权限访问[iot:device:ctrl]"}
```

同时 iBOOT 日志中也已出现：

```text
SecurityException: 没有权限访问[iot:device:ctrl]
```

因此结论是：

- 权限拦截已生效
- 只是当前项目返回风格为“HTTP 200 + 业务码 403”

### 10.4 OPERATOR 控灯实测结果

`operator` 实测控制 LED 成功，接口响应为：

```json
{"data":null,"code":200,"message":"操作成功"}
```

随后再次查询设备，`board001` 的 `led` 状态已变为 `on`。

### 10.5 安全测试脚本

脚本路径：

```powershell
D:\Proj\5.29\iboot-jetlinks\scripts\security\test-security-flow.sh
```

运行命令：

```powershell
bash D:\Proj\5.29\iboot-jetlinks\scripts\security\test-security-flow.sh
```

如需补管理员运行态验证：

```powershell
$env:ADMIN_PASSWORD="你的管理员密码"
bash D:\Proj\5.29\iboot-jetlinks\scripts\security\test-security-flow.sh
```

脚本覆盖的核心验证项：

- HTTPS 可访问
- TLS 握手成功
- Authorization Code 成功
- Access Token 成功
- Refresh Token 成功
- Refresh Token 刷新成功
- Refresh Token 撤销成功
- 无 Token 返回 `401`
- 非法 Token 返回 `401`
- `viewer` 控灯被拒绝
- `operator` 控灯成功

### 10.6 PPT 截图用权限验证脚本

为了现场验收和 PPT 截图，新增 Windows PowerShell 脚本：

```powershell
D:\Proj\5.29\iboot-jetlinks\scripts\security\permission-demo-ppt.ps1
```

该脚本默认使用浏览器实际访问入口：

```text
http://127.0.0.1/api
```

原因是当前 Windows PowerShell 5 和 Windows 自带 `curl.exe` 直连 `https://127.0.0.1:8443` 时，可能会因为本地自签名证书和 Windows TLS 栈兼容问题报错；而浏览器页面实际也是通过 Nginx 的 `http://127.0.0.1/api` 反代访问后端。因此 PPT 验收脚本默认走 Nginx 入口，更接近页面真实链路。

推荐截图命令：

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File D:\Proj\5.29\iboot-jetlinks\scripts\security\permission-demo-ppt.ps1 -MaskToken
```

如果老师要求看到完整 Token，可运行：

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File D:\Proj\5.29\iboot-jetlinks\scripts\security\permission-demo-ppt.ps1
```

如果需要每一段停下来单独截图，可运行：

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File D:\Proj\5.29\iboot-jetlinks\scripts\security\permission-demo-ppt.ps1 -MaskToken -Interactive
```

脚本截图重点：

- `viewer` 获取 Access Token、Refresh Token 和 JWT Payload
- `viewer` 的 JWT 中 `roles = VIEWER`、`scope = device.read`
- `operator` 获取 Access Token、Refresh Token 和 JWT Payload
- `operator` 的 JWT 中 `roles = OPERATOR`、`scope = device.read device.control`
- 无 Token 访问设备接口返回 `401`
- 非法 Token 访问设备接口返回 `401`
- `viewer` 可以查询设备，但访问管理接口和控制 LED 都返回业务码 `403`
- `operator` 不能访问管理接口，但可以进入 LED 控制业务链路
- Refresh Token 可以换新 Access Token
- Refresh Token 被 Revoke 后再次刷新会失败

本机已实际运行通过：

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File D:\Proj\5.29\iboot-jetlinks\scripts\security\permission-demo-ppt.ps1 -MaskToken
```

本次实测输出记录保存到：

```text
D:\Proj\5.29\iboot-jetlinks\scripts\security\output\permission-demo-ppt-20260610-144917.txt
```

## 11. 从停机到完整验收的最短命令

### 11.1 一键启动

```powershell
powershell -ExecutionPolicy Bypass -File D:\Proj\5.29\iboot-jetlinks\scripts\security\start-acceptance.ps1
```

### 11.2 安全验收

适合 PPT 截图的 Windows PowerShell 验证脚本：

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File D:\Proj\5.29\iboot-jetlinks\scripts\security\permission-demo-ppt.ps1 -MaskToken
```

完整自动化安全验证脚本：

```powershell
bash D:\Proj\5.29\iboot-jetlinks\scripts\security\test-security-flow.sh
```

### 11.3 页面验收

打开：

- `http://127.0.0.1/`
- `http://127.0.0.1:9000/admin/index.html`

在 iBOOT 页面重点看：

1. 是否看到 `board001`
2. 是否看到 `temperature / humidity / led`
3. `viewer` 是否只能看不能控
4. `operator` 是否可以控灯
5. 开发板 LED1 是否真实变化

### 11.4 一键停机

```powershell
powershell -ExecutionPolicy Bypass -File D:\Proj\5.29\iboot-jetlinks\scripts\security\stop-acceptance.ps1
```

## 12. 老师现场问答模板

### 12.1 1 到 2 分钟讲解词

```text
本次实验我主要完成了三部分。
第一部分是平台运行环境，我把 MySQL、Redis、Elasticsearch、JetLinks、iBOOT、Nginx 和 Mosquitto 都整理成了 Windows 本机服务，并补了一键启动和一键停机脚本，验收时可以直接拉起整套环境。

第二部分是设备接入，我把普中 F103 加 ESP8266 的开发板接入到了本机 MQTT Broker，开发板通过 MQTT 向 iBOOT 上报 temperature、humidity 和 led 三个字段，并订阅下行 Topic 接收 LED 控制。当前 board001 已经在线，平台上可以看到实时数据，也可以控制板上的 LED1。

第三部分是安全改造，我在 iBOOT 上补齐了 HTTPS、OAuth2、JWT、Refresh Token、Token 撤销、Scope 和 RBAC。现在设备查询和 LED 控制都不是匿名可调，viewer 只能查看，operator 才能控制 LED。我已经在本机跑通了授权码换 token、refresh、revoke、401 和越权拦截验证，权限通过后才会真正触发 MQTT 下发。
```

### 12.2 如果老师问“你到底改了什么”

可以直接回答：

```text
我主要改了四块：一是 iBOOT 前后端实验页面和接口接入；二是 MQTT 子设备和普中 F103 开发板联调；三是 OAuth2、JWT、RBAC 安全链路；四是 Windows 本机的一键启停和验收脚本。
```

### 12.3 如果老师问“为什么页面能直接显示数据”

可以回答：

```text
因为平台、接口和开发板程序统一使用了 temperature、humidity、led 三个字段名，开发板 MQTT 上报后，iBOOT 页面可以直接识别，不需要额外做字段转换。
```

### 12.4 如果老师问“点一下 LED 后面发生了什么”

可以回答：

```text
前端先调用 iBOOT 的 LED 控制接口，后端先做 OAuth2 Token、Scope 和角色权限校验，通过后再调用 MQTT 协议驱动向开发板下发控制消息，开发板执行 LED 开关后再把最新状态通过 MQTT 回传到平台。
```

### 12.5 如果老师问“现在还有什么边界”

可以回答：

```text
当前温度已经是实测链路，湿度目前还是程序模拟值，后续可以继续替换为 DHT11 或 DHT22 的实测读取。
另外 viewer 被拒绝时，当前项目是 HTTP 200 但业务码 403，这说明权限已经生效，只是返回风格还不是标准 HTTP 403。
```

## 13. 本轮关键修复记录

### 13.1 iBOOT 修复

本轮已修复并确认：

- `iboot-jetlinks-service` 可正常启动
- `8443` 正常监听
- `https://127.0.0.1:8443/api/oauth2/config` 可访问

实际补齐的配置重点包括：

- `framework.security.oauth2-issuer`
- `framework.security.jwt-secret`

### 13.1.1 Nginx 502 的真实修复

本轮曾出现前端页面可打开，但接口请求返回：

```text
502 Bad Gateway
```

实际根因不是 iBOOT 宕机，而是：

- Nginx 还在把 `/api/` 和 `/img/` 转发到旧地址 `http://127.0.0.1:8085`
- 但当前 iBOOT 实际运行在 `https://127.0.0.1:8443/api`

本轮已实际修复：

- `D:\Proj\5.29\deploy\nginx\server.iboot.conf`
- `C:\tools\nginx\conf.d\server.iboot.conf`

修复后的关键配置是：

```nginx
location /api/ {
    proxy_pass         https://127.0.0.1:8443;
    proxy_ssl_verify   off;
}
```

以及：

```nginx
location /img/ {
    proxy_pass         https://127.0.0.1:8443;
    proxy_ssl_verify   off;
}
```

修复后已实际验证：

- `http://127.0.0.1/api/oauth2/config` 返回 `200`
- `http://127.0.0.1/` 返回 `200`

### 13.1.2 OAuth2 非法 redirect_url 的真实修复

本轮还实际遇到过：

```json
{"code":500,"msg":"非法redirect_url：http://127.0.0.1/oauth/callback.html","data":null}
```

实际根因是：

- OAuth2 客户端 `iboot-local-web` 只登记了 `5173` 开发端口的回调地址
- 但当前验收实际访问地址是 `http://127.0.0.1/`
- 所以 OAuth2 授权时回调地址变成了 `http://127.0.0.1/oauth/callback.html`

本轮已实际把数据库中的 `oauth2_app.allow_url` 更新为包含：

- `http://localhost:5173/oauth/callback.html`
- `http://127.0.0.1:5173/oauth/callback.html`
- `http://127.0.0.1/oauth/callback.html`
- `http://localhost/oauth/callback.html`

因此当前课程验收应优先使用：

```text
http://127.0.0.1/
```

并允许它走：

```text
http://127.0.0.1/oauth/callback.html
```

### 13.2 一键脚本修复

本轮已修复：

- `start-acceptance.ps1`
- `stop-acceptance.ps1`

修复点包括：

- PowerShell 语法错误
- 乱码导致的字符串问题
- 与 PowerShell 内置变量 `$Host` 的命名冲突
- 全部改为适合 Windows PowerShell 5.1 的写法
- 增加 Nginx 代理目标自检
- 增加 OAuth2 回调白名单自检

### 13.3 MQTT 与开发板链路修复

本轮确认：

- `board001` 已上线
- iBOOT 日志持续接收 `board001` 上报
- `operator` 控灯后板端状态已同步回平台

## 14. 当前最终结论

截至 2026-06-09，本机当前可用于课程验收的结论如下：

- Windows 本机服务方式可稳定启动 iBOOT、JetLinks、MySQL、Redis、Elasticsearch、Nginx 和 Mosquitto
- iBOOT 前端、JetLinks 管理页、iBOOT HTTPS 安全入口均已可访问
- MQTT Broker 运行在 Windows 本机 `1883`
- 普中 F103 + ESP8266 已接入本机 MQTT Broker
- `board001` 已在线并持续上报数据
- iBOOT 页面可展示 `temperature`、`humidity`、`led`
- `operator` 已实测可控制开发板 LED
- `viewer` 已实测被权限拦截
- OAuth2 授权码、JWT、Refresh Token、Revoke、401、越权拦截均已完成本机运行态验证

唯一需要现场说明的偏差：

- `viewer` 控灯被拒绝时，当前项目返回的是“HTTP 200 + 业务码 403”
- 如果老师严格要求 HTTP 层必须直接返回 `403 Forbidden`，则这一点属于返回风格偏差
- 但从权限效果和日志上看，后端权限控制已经真实生效

当前还需要注意两条现场规则：

- 必须从 `http://127.0.0.1/` 进入页面，不要再混用旧的开发端口地址
- 如果页面已更新但行为异常，先执行一次 `Ctrl + F5` 强制刷新，避免浏览器继续使用旧的前端缓存
