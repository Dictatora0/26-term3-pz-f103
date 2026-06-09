### iboot(物联网网关)
iboot是基于java1.8、springboot2.7、netty等框架开发的物联网网关，支持的网络协议包括tcp、udp、mqtt、http、串口等，工业协议包括modbus、plc、dlt645-2007、opcua，实现的plc包括西门子、欧姆龙、三菱、松下、汇川、罗克韦尔
- [开发文档](http://doc.iteaj.com)
- [项目前端仓库(vue3+antdv3+vite2)](https://gitee.com/iteaj/ivzone)
- [演示地址(支持DTU、PLC、MODBUS、串口等调试)](https://iot.iteaj.com/#/login)
### 加入社区
> 加群申请请备注: iot

QQ3群 - 272518000 <br>
QQ2群 - 616124620 (已满)<br>
QQ1群 - 552167793 (已满)<br>
### 商务合作(qq号：97235681)
### 视频教程
1. [Iboot系统简介](https://www.bilibili.com/video/BV15NtieWE9W/?vd_source=32a176fca70a1cee8222bff9ac0846ab)<br>
2. [Iboot设备类型介绍](https://www.bilibili.com/video/BV13kx7ebEYM/?vd_source=32a176fca70a1cee8222bff9ac0846ab)
2. [Iboot序列教程之Mqtt篇](https://www.bilibili.com/video/BV1k9t5e3EtK/?vd_source=32a176fca70a1cee8222bff9ac0846ab)<br>
3. [Iboot序列教程之Modbus篇](https://www.bilibili.com/video/BV1dgtQeNEyP/?vd_source=32a176fca70a1cee8222bff9ac0846ab) 
### 已实现的协议驱动
| 协议 | 实现方式 | 开源 |其他| 说明 |
|----|--------|-----|-------|------------|
|  Mqtt  |   系统内置   |   是   |-|  支持设备、第三方业务和iboot系统的双向数据对接  |
|  DTU+Modbus Tcp  |   系统内置   |   是   |-|  支持设备以modbus tcp协议对接并通过dtu连接iboot云网关  |
|  DTU+Modbus Rtu  |   系统内置   |   是   |-|  支持设备通过485和dtu串口以modbus rtu协议对接上传到iboot云网关  |
|  Modbus Tcp  |   系统内置   |   是   |-|  支持设备在内网里和iboot网关(上位机)以modbus tcp协议对接  |
|  Modbus Rtu  |   系统内置   |   是   |-|  支持设备通过串口和iboot网关(上位机)以modbus rtu协议对接  |
|  Opc Ua  |   系统内置   |   否   |-|  支持iboot网关(上位机)以opcua服务器对接采集和控制设备  |
|  dlt645-2007  |   系统内置   |   否   |-|  支持以内网以串口或者云服务器以dtu方式采集电表数据  |
|  西门子S7  |   系统内置   |   否   |-|  支持西门子S7协议的序列PLC  |
|  欧姆龙  |   系统内置   |   否   |-|  支持欧姆龙Fins协议的序列PLC  |
|  三菱  |   系统内置   |   否   |-|  支持三菱A1E和Qna-3E协议的序列PLC  |
|  汇川  |   系统内置   |   否   |-|  以Modbus Tcp方式实现  |
|  松下  |   系统内置   |   否   |-|  松下PLC  |
|  罗克韦尔  |   系统内置   |   否   |-|  以CIP协议实现，适用1756，1769等型号  |
### 平台架构图
![平台架构](https://iot.iteaj.com/show/IBoot网络架构图.jpg)
### 项目介绍
1. ##### 这个项目能用来做什么<br>
      iboot是一款通用的物联网关平台，用来连接业务系统和物联网设备的网关；对南向主要用于采集和控制设备对北向主要是提供接口给第三方业务平台操作设备，使得对物联网不熟的个人开发者或者公司可以更加关注业务需求，将设备相关部分交由iboot物联网关
2. ##### 有朋友问我们已经有系统了，集成你们的网关会不会很麻烦
      - 首先 iboot是一个独立的物联网网关，不建议将你们的系统和iboot进行代码层面的集成，各个系统独立部署是最优解
      - 其实 如果你们是微服务，那么可以将iboot作为一个物联网网关服务嵌入到你们微服务架构中
3. ##### 既然设备的采集和控制交给iboot，那业务系统怎么和设备交互呢<br>
      iboot支持通过http协议以同步的方式或通过mqtt异步的方式和业务系统交互，业务系统可以间接通过iboot网关采集和控制设备
4. ##### 我们设备种类很多，iboot支持自定义设备接入吗<br>
      iboot支持以自定义jar包的方式提供设备驱动, 理论上可以支持任何类型设备
5. ##### 我们设备量大，采集的数据量多就一台mysql肯定扛不住怎么办<br>
      iboot支持多数据源，支持采集的数据切换到时序数据库(默认TDengine)
6. ##### 有朋友说对TDengine不熟，想使用其他的时序数据源好切换吗 如. influxdb
      iboot系统关键部位架构在插件化的形式所以可以很方便的进行切换和自定义
7. ##### 如果客户对实时性要求很高，业务系统怎么和iboot网关对接
      - 首先 iboot支持将采集的实时数据(数据、告警、设备上下线等)通过RabbitMq队列的方式向业务系统提供数据
      - 其次 iboot支持将采集的实时数据(数据、告警、设备上下线等)通过websocket实时向前端推送(方便web前端实时展示或给组态系统提供实时数据)
8. ##### 支持将系统部署在边缘的网关服务器吗
      iboot使用java跨平台的语言开发，原则上只要边缘服务器支持linux系统即可
9. ##### 你们的开源产品不会和其他开源产品一样是个半成品吧(害怕用了之后各种问题无奈只能使用高级版)
      iboot的开源产品是收费产品的基础，因为我们只对部分功能收费，收费版本是开发版本的底层分支，核心的功能收费分支都是直接合并的开发分支，所以不需要担心，因为他们是同一套代码，只是收费版本多了一些高级功能
10. ##### 你们的项目目前提供了哪些协议
      
#### 核心功能页面
![产品页面](https://iot.iteaj.com/show/product.png)
![联动页面](https://iot.iteaj.com/show/linkage.png)
![mqtt协议页面](https://iot.iteaj.com/show/mqtt.png)
![告警页面](https://iot.iteaj.com/show/warn.png)
![网络页面](https://iot.iteaj.com/show/network.png)
![地图页面](https://iot.iteaj.com/show/map.png)
![调试页面](https://iot.iteaj.com/show/debug.png)
#### 系统安装环境
  iboot是一个springboot单体架构，只需要一台云服务器；安装java运行环境、mysql数据库、nginx代理服务即可
#### 服务器配置
  云服务器最低配置在2核8G，cpu密集型、硬盘看采集的数据量(120G+)
      

#### 语言和框架
1. 后端：java1.8、spring boot2.7、、mybatis、mybatis-plus、shiro、satoken、thymeleaf、hikaricp
2. 前端：vue3、antd、axios、qs、moment、validate
3. 数据： mysql8.0+
4. 开发工具：idea

#### 参与贡献

1. Fork 本项目
2. 新建 Feat_xxx 分支
3. 提交代码
4. 新建 Pull Request
