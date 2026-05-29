# Windows Local Deployment Notes

## 1. Deployment scope

This machine has been configured with:

- MySQL
- Redis
- Elasticsearch
- Nginx
- iboot-jetlinks backend
- JetLinks backend
- iBoot frontend
- JetLinks frontend

Project root:

- `D:\Proj\5.29`

## 2. Service status

Configured as Windows auto-start services:

- `MySQL`
- `redis-service`
- `elasticsearch-service-x64`
- `nginx-service`
- `iboot-jetlinks-service`
- `jetlinks-standalone-service`

## 3. Access addresses

- iBoot homepage: `http://127.0.0.1/`
- iBoot backend API root: `http://127.0.0.1:8085/api`
- iBoot websocket: `ws://127.0.0.1/ws/`
- JetLinks admin page: `http://127.0.0.1:9000/admin/index.html`
- JetLinks backend port: `http://127.0.0.1:8848`

Notes:

- `http://127.0.0.1:8848/` returning `404` is normal.
- JetLinks UI entry is `/admin/index.html`.

## 4. Core configuration

### 4.1 MySQL

- Host: `127.0.0.1`
- Port: `3306`
- Username: `root`
- Password: `123456`
- Databases:
  - `iboot`
  - `jetlinks`

### 4.2 Redis

- Host: `127.0.0.1`
- Port: `6379`
- Password: empty

### 4.3 Elasticsearch

- Host: `127.0.0.1`
- Port: `9200`
- URI: `http://127.0.0.1:9200`

### 4.4 iBoot external config

Config file:

- `D:\Proj\5.29\deploy\iboot\config\application-prod.yml`

Key settings:

- server port: `8085`
- servlet context path: `/api`
- MySQL database: `iboot`
- Redis database: `0`
- upload path: `D:/Proj/5.29/deploy/runtime/iboot/upload`
- JetLinks integration:
  - `jetlinks.host=127.0.0.1`
  - `jetlinks.port=8848`
  - `autoConnect=false`

### 4.5 JetLinks external config

Config file:

- `D:\Proj\5.29\deploy\jetlinks\config\application-prod.yml`

Key settings:

- server port: `8848`
- MySQL database: `jetlinks`
- R2DBC URL:
  - `r2dbc:mysql://127.0.0.1:3306/jetlinks?sslMode=DISABLED&serverZoneId=Asia/Shanghai`
- Redis:
  - `127.0.0.1:6379`
- Elasticsearch URI:
  - `http://127.0.0.1:9200`
- EasyORM:
  - `dialect: mysql`
  - `default-schema: jetlinks`
- Static upload path:
  - `./static/upload`
- Static upload URL:
  - `http://127.0.0.1:9000/upload`
- API base path:
  - `http://127.0.0.1:9000`

### 4.6 Nginx routes

Nginx site config files:

- `D:\Proj\5.29\deploy\nginx\server.iboot.conf`
- `D:\Proj\5.29\deploy\nginx\server.jetlinks.conf`

Routing summary:

- Port `80`
  - static root: `D:/Proj/5.29/iboot-v3/dist`
  - `/api/` -> `127.0.0.1:8085`
  - `/img/` -> `127.0.0.1:8085`
  - `/ws/` -> `127.0.0.1:8170`
- Port `9000`
  - reverse proxy -> `127.0.0.1:8848`

## 5. Runtime paths

- iBoot logs:
  - `D:\Proj\5.29\deploy\runtime\iboot\logs`
- iBoot uploads:
  - `D:\Proj\5.29\deploy\runtime\iboot\upload`
- JetLinks logs:
  - `D:\Proj\5.29\deploy\runtime\jetlinks\logs`
- JetLinks static admin files:
  - `D:\Proj\5.29\deploy\runtime\jetlinks\static\admin`
- JetLinks uploads:
  - `D:\Proj\5.29\deploy\runtime\jetlinks\static\upload`

## 6. Build artifacts

- iBoot backend JAR:
  - `D:\Proj\5.29\iboot-jetlinks\bootstrap\target\bootstrap.jar`
- JetLinks backend JAR:
  - `D:\Proj\5.29\jetlinks-community-master\jetlinks-community-master\jetlinks-standalone\target\jetlinks-standalone.jar`
- iBoot frontend dist:
  - `D:\Proj\5.29\iboot-v3\dist`
- JetLinks frontend dist:
  - `D:\Proj\5.29\jetlinks-ui-vue\dist`

## 7. Common operations

### 7.1 Check service status

```powershell
Get-Service MySQL,redis-service,elasticsearch-service-x64,nginx-service,iboot-jetlinks-service,jetlinks-standalone-service | Select-Object Status,Name,StartType
```

### 7.2 Start services

```powershell
Start-Service MySQL,redis-service,elasticsearch-service-x64,nginx-service,jetlinks-standalone-service,iboot-jetlinks-service
```

### 7.3 Restart services

```powershell
Restart-Service MySQL
Restart-Service redis-service
Restart-Service elasticsearch-service-x64
Restart-Service nginx-service
Restart-Service jetlinks-standalone-service
Restart-Service iboot-jetlinks-service
```

### 7.4 Stop services

```powershell
Stop-Service iboot-jetlinks-service
Stop-Service jetlinks-standalone-service
Stop-Service nginx-service
Stop-Service elasticsearch-service-x64
Stop-Service redis-service
Stop-Service MySQL
```

Recommended stop/start order:

- Stop order:
  - `iboot-jetlinks-service`
  - `jetlinks-standalone-service`
  - `nginx-service`
  - `elasticsearch-service-x64`
  - `redis-service`
  - `MySQL`
- Start order:
  - `MySQL`
  - `redis-service`
  - `elasticsearch-service-x64`
  - `jetlinks-standalone-service`
  - `iboot-jetlinks-service`
  - `nginx-service`

### 7.5 Check listening ports

```powershell
Get-NetTCPConnection -State Listen | Where-Object { $_.LocalPort -in 80,9000,8085,8170,8848,3306,6379,9200 } | Select-Object LocalAddress,LocalPort,State
```

### 7.6 Connectivity test

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

### 7.7 HTTP page checks

```powershell
Invoke-WebRequest -UseBasicParsing http://127.0.0.1/ | Select-Object StatusCode
Invoke-WebRequest -UseBasicParsing http://127.0.0.1:9000/admin/index.html | Select-Object StatusCode
Invoke-WebRequest -UseBasicParsing http://127.0.0.1:8085/api/doc.html | Select-Object StatusCode
```

### 7.8 View logs

```powershell
Get-Content D:\Proj\5.29\deploy\runtime\iboot\logs\stdout.log -Tail 100
Get-Content D:\Proj\5.29\deploy\runtime\iboot\logs\stderr.log -Tail 100
Get-Content D:\Proj\5.29\deploy\runtime\jetlinks\logs\stdout.log -Tail 100
Get-Content D:\Proj\5.29\deploy\runtime\jetlinks\logs\stderr.log -Tail 100
```

### 7.9 Screenshot commands

Recommended screenshot commands:

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

## 8. Service registration details

### 8.1 iboot-jetlinks-service

- Executable:
  - `C:\Program Files\Zulu\zulu-8\bin\java.exe`
- Working directory:
  - `D:\Proj\5.29\iboot-jetlinks\bootstrap\target`
- Start arguments:
  - `-jar D:\Proj\5.29\iboot-jetlinks\bootstrap\target\bootstrap.jar --spring.config.additional-location=file:///D:/Proj/5.29/deploy/iboot/config/ --spring.profiles.active=prod`

### 8.2 jetlinks-standalone-service

- Executable:
  - `C:\Program Files\Zulu\zulu-8\bin\java.exe`
- Working directory:
  - `D:\Proj\5.29\deploy\runtime\jetlinks`
- Start arguments:
  - `-jar D:\Proj\5.29\jetlinks-community-master\jetlinks-community-master\jetlinks-standalone\target\jetlinks-standalone.jar --spring.config.additional-location=file:///D:/Proj/5.29/deploy/jetlinks/config/ --spring.profiles.active=prod`

## 9. Important notes

- JetLinks first startup will auto-create tables and initialize base data in `jetlinks`.
- `iboot-jetlinks` can start even when JetLinks device binding parameters are empty because `autoConnect=false`.
- If later you need iBoot to bind to a JetLinks device, fill these fields in `deploy\iboot\config\application-prod.yml`:
  - `token`
  - `productId`
  - `deviceId`
- After modifying external config files, restart the related service to apply changes.

## 10. File index

- `D:\Proj\5.29\deploy\README.md`
- `D:\Proj\5.29\deploy\OPERATIONS.md`
- `D:\Proj\5.29\deploy\iboot\config\application-prod.yml`
- `D:\Proj\5.29\deploy\jetlinks\config\application-prod.yml`
- `D:\Proj\5.29\deploy\nginx\server.iboot.conf`
- `D:\Proj\5.29\deploy\nginx\server.jetlinks.conf`
