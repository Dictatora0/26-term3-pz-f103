# Windows 本地部署说明

## 1. 部署内容

本机已完成以下组件部署：

- MySQL
- Redis
- Elasticsearch
- Nginx
- iboot-jetlinks 后端
- JetLinks 后端
- iBoot 前端
- JetLinks 前端

项目根目录：

- `D:\Proj\5.29`

## 2. 服务情况

以下服务已注册为 Windows 开机自启服务：

- `MySQL`
- `redis-service`
- `elasticsearch-service-x64`
- `nginx-service`
- `iboot-jetlinks-service`
- `jetlinks-standalone-service`

## 3. 访问地址

- iBoot 首页：`http://127.0.0.1/`
- iBoot 后端接口根路径：`http://127.0.0.1:8085/api`
- iBoot WebSocket：`ws://127.0.0.1/ws/`
- JetLinks 管理页面：`http://127.0.0.1:9000/admin/index.html`
- JetLinks 后端端口：`http://127.0.0.1:8848`

说明：

- `http://127.0.0.1:8848/` 返回 `404` 属于正常现象。
- JetLinks 前端入口是 `/admin/index.html`。

## 4. 核心配置

### 4.1 MySQL

- 地址：`127.0.0.1`
- 端口：`3306`
- 用户名：`root`
- 密码：`123456`
- 数据库：
  - `iboot`
  - `jetlinks`

### 4.2 Redis

- 地址：`127.0.0.1`
- 端口：`6379`
- 密码：空

### 4.3 Elasticsearch

- 地址：`127.0.0.1`
- 端口：`9200`
- 访问 URI：`http://127.0.0.1:9200`

### 4.4 iBoot 外部配置

配置文件：

- `D:\Proj\5.29\deploy\iboot\config\application-prod.yml`

关键配置：

- 服务端口：`8085`
- 接口上下文路径：`/api`
- MySQL 数据库：`iboot`
- Redis 数据库：`0`
- 上传目录：`D:/Proj/5.29/deploy/runtime/iboot/upload`
- JetLinks 对接配置：
  - `jetlinks.host=127.0.0.1`
  - `jetlinks.port=8848`
  - `autoConnect=false`

### 4.5 JetLinks 外部配置

配置文件：

- `D:\Proj\5.29\deploy\jetlinks\config\application-prod.yml`

关键配置：

- 服务端口：`8848`
- MySQL 数据库：`jetlinks`
- R2DBC 地址：
  - `r2dbc:mysql://127.0.0.1:3306/jetlinks?sslMode=DISABLED&serverZoneId=Asia/Shanghai`
- Redis：
  - `127.0.0.1:6379`
- Elasticsearch URI：
  - `http://127.0.0.1:9200`
- EasyORM：
  - `dialect: mysql`
  - `default-schema: jetlinks`
- 上传目录：
  - `./static/upload`
- 上传文件访问地址：
  - `http://127.0.0.1:9000/upload`
- API 基础地址：
  - `http://127.0.0.1:9000`

### 4.6 Nginx 路由配置

Nginx 站点配置文件：

- `D:\Proj\5.29\deploy\nginx\server.iboot.conf`
- `D:\Proj\5.29\deploy\nginx\server.jetlinks.conf`

路由说明：

- 端口 `80`
  - 静态目录：`D:/Proj/5.29/iboot-v3/dist`
  - `/api/` 转发到 `127.0.0.1:8085`
  - `/img/` 转发到 `127.0.0.1:8085`
  - `/ws/` 转发到 `127.0.0.1:8170`
- 端口 `9000`
  - 整站反向代理到 `127.0.0.1:8848`

## 5. 运行目录

- iBoot 日志目录：
  - `D:\Proj\5.29\deploy\runtime\iboot\logs`
- iBoot 上传目录：
  - `D:\Proj\5.29\deploy\runtime\iboot\upload`
- JetLinks 日志目录：
  - `D:\Proj\5.29\deploy\runtime\jetlinks\logs`
- JetLinks 前端静态目录：
  - `D:\Proj\5.29\deploy\runtime\jetlinks\static\admin`
- JetLinks 上传目录：
  - `D:\Proj\5.29\deploy\runtime\jetlinks\static\upload`

## 6. 构建产物

- iBoot 后端 JAR：
  - `D:\Proj\5.29\iboot-jetlinks\bootstrap\target\bootstrap.jar`
- JetLinks 后端 JAR：
  - `D:\Proj\5.29\jetlinks-community-master\jetlinks-community-master\jetlinks-standalone\target\jetlinks-standalone.jar`
- iBoot 前端构建目录：
  - `D:\Proj\5.29\iboot-v3\dist`
- JetLinks 前端构建目录：
  - `D:\Proj\5.29\jetlinks-ui-vue\dist`

## 7. 常用运维命令

### 7.1 查看服务状态

```powershell
Get-Service MySQL,redis-service,elasticsearch-service-x64,nginx-service,iboot-jetlinks-service,jetlinks-standalone-service | Select-Object Status,Name,StartType
```

### 7.2 启动全部服务

```powershell
Start-Service MySQL,redis-service,elasticsearch-service-x64,nginx-service,jetlinks-standalone-service,iboot-jetlinks-service
```

### 7.3 重启服务

```powershell
Restart-Service MySQL
Restart-Service redis-service
Restart-Service elasticsearch-service-x64
Restart-Service nginx-service
Restart-Service jetlinks-standalone-service
Restart-Service iboot-jetlinks-service
```

### 7.4 停止服务

```powershell
Stop-Service iboot-jetlinks-service
Stop-Service jetlinks-standalone-service
Stop-Service nginx-service
Stop-Service elasticsearch-service-x64
Stop-Service redis-service
Stop-Service MySQL
```

建议启停顺序：

- 停止顺序：
  - `iboot-jetlinks-service`
  - `jetlinks-standalone-service`
  - `nginx-service`
  - `elasticsearch-service-x64`
  - `redis-service`
  - `MySQL`
- 启动顺序：
  - `MySQL`
  - `redis-service`
  - `elasticsearch-service-x64`
  - `jetlinks-standalone-service`
  - `iboot-jetlinks-service`
  - `nginx-service`

### 7.5 查看监听端口

```powershell
Get-NetTCPConnection -State Listen | Where-Object { $_.LocalPort -in 80,9000,8085,8170,8848,3306,6379,9200 } | Select-Object LocalAddress,LocalPort,State
```

### 7.6 端口连通性检查

```powershell
Test-NetConnection 127.0.0.1 -Port 3306
Test-NetConnection 127.0.0.1 -Port 6379
Test-NetConnection 127.0.0.1 -Port 9200
Test-NetConnection 127.0.0.1 -Port 8085
Test-NetConnection 127.0.0.1 -Port 8170
Test-NetConnection 127.0.0.1 -Port 8848
Test-NetConnection 127.0.0.1 -Port 80
Test-NetConnection 127.0.0.1 -Port 9000
```

### 7.7 页面状态检查

```powershell
Invoke-WebRequest -UseBasicParsing http://127.0.0.1/ | Select-Object StatusCode
Invoke-WebRequest -UseBasicParsing http://127.0.0.1:9000/admin/index.html | Select-Object StatusCode
Invoke-WebRequest -UseBasicParsing http://127.0.0.1:8085/api/doc.html | Select-Object StatusCode
```

### 7.8 查看日志

```powershell
Get-Content D:\Proj\5.29\deploy\runtime\iboot\logs\stdout.log -Tail 100
Get-Content D:\Proj\5.29\deploy\runtime\iboot\logs\stderr.log -Tail 100
Get-Content D:\Proj\5.29\deploy\runtime\jetlinks\logs\stdout.log -Tail 100
Get-Content D:\Proj\5.29\deploy\runtime\jetlinks\logs\stderr.log -Tail 100
```

### 7.9 截图用命令

建议截图以下命令结果：

```powershell
Get-Service MySQL,redis-service,elasticsearch-service-x64,nginx-service,iboot-jetlinks-service,jetlinks-standalone-service | Select-Object Status,Name,StartType
```

```powershell
Get-NetTCPConnection -State Listen | Where-Object { $_.LocalPort -in 80,9000,8085,8170,8848,3306,6379,9200 } | Select-Object LocalAddress,LocalPort,State
```

```powershell
Invoke-WebRequest -UseBasicParsing http://127.0.0.1/ | Select-Object StatusCode
```

```powershell
Invoke-WebRequest -UseBasicParsing http://127.0.0.1:9000/admin/index.html | Select-Object StatusCode
```

## 8. 服务注册详情

### 8.1 iboot-jetlinks-service

- 可执行文件：
  - `C:\Program Files\Zulu\zulu-8\bin\java.exe`
- 工作目录：
  - `D:\Proj\5.29\iboot-jetlinks\bootstrap\target`
- 启动参数：
  - `-jar D:\Proj\5.29\iboot-jetlinks\bootstrap\target\bootstrap.jar --spring.config.additional-location=file:///D:/Proj/5.29/deploy/iboot/config/ --spring.profiles.active=prod`

### 8.2 jetlinks-standalone-service

- 可执行文件：
  - `C:\Program Files\Zulu\zulu-8\bin\java.exe`
- 工作目录：
  - `D:\Proj\5.29\deploy\runtime\jetlinks`
- 启动参数：
  - `-jar D:\Proj\5.29\jetlinks-community-master\jetlinks-community-master\jetlinks-standalone\target\jetlinks-standalone.jar --spring.config.additional-location=file:///D:/Proj/5.29/deploy/jetlinks/config/ --spring.profiles.active=prod`

## 9. 重要说明

- JetLinks 首次启动会自动在 `jetlinks` 数据库中建表并初始化基础数据。
- `iboot-jetlinks` 当前可以在 JetLinks 设备绑定参数为空的情况下启动，因为配置为 `autoConnect=false`。
- 如果后续需要让 iBoot 连接到 JetLinks 设备，需要在 `deploy\iboot\config\application-prod.yml` 中补充以下参数：
  - `token`
  - `productId`
  - `deviceId`
- 修改外部配置文件后，需要重启对应服务才能生效。

## 10. 相关文件索引

- `D:\Proj\5.29\deploy\README.md`
- `D:\Proj\5.29\deploy\OPERATIONS.md`
- `D:\Proj\5.29\deploy\iboot\config\application-prod.yml`
- `D:\Proj\5.29\deploy\jetlinks\config\application-prod.yml`
- `D:\Proj\5.29\deploy\nginx\server.iboot.conf`
- `D:\Proj\5.29\deploy\nginx\server.jetlinks.conf`
