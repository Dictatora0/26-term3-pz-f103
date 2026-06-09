# 设备接入jetlinks

### 使用jetlinks官方开源协议
通过http协议上报数据，通过websocket下发指令给设备https://hanta.yuque.com/px7kg1/yfac2l/ceenesef5ifta03y

### iboot对接实现
jetlinks-spring-boot-starter,已实现设备上/下线,属性上报,属性/指令下发
![image #60% #auto](https://drive.docs.qq.com/023a9c497a0e464c82580764877114d5?sign=ae76cd5c86d8ad574c33843946410029&ts=1749523010&response-content-disposition=attachment%3Bfilename%3D%22image.png%22%3Bfilename%2A%3DUTF-8%27%27image.png)
在配置文件中加入jetlinks相关配置
```yml
jetlinks:
  host: 192.168.20.42 
  port: 5060
  token: admin
  autoConnect: true
  productId: iboot01
  deviceId: device_iboot
```
![image #60% #auto](https://drive.docs.qq.com/226c6d1f8b3a411e8f9149c53b69e4a0?sign=a63e348e34d0444cfe9b3570a30648af&ts=1749523010&response-content-disposition=attachment%3Bfilename%3D%22image.png%22%3Bfilename%2A%3DUTF-8%27%27image.png)
获取jetlinks下发消息并发布到 Redis
`com/iteaj/iboot/module/iot/jetlinks/listener/DeviceEventListener.java`
多个iboot订阅redis,获取消息执行下发指令
`com.iteaj.iboot.module.iot.jetlinks.RedisMessageSubscriber`

### iboot关联jetlinks配置
1.设备上线启动时自动调用
2.指令下发
在物模型的功能定义中新增一条,标识对应iboot中的设备标识
![image #60% #auto](https://drive.docs.qq.com/3a802c2406714c08b870625fca2bf5c5?sign=3316dddc551f1d22a909b388c13e1795&ts=1749523010&response-content-disposition=attachment%3Bfilename%3D%22image.png%22%3Bfilename%2A%3DUTF-8%27%27image.png)
![image #60% #auto](https://drive.docs.qq.com/5a64f47fd19a4823a23d0b90c86d0931?sign=d9d72bdd0e21407588a472e6ef14bfd3&ts=1749523010&response-content-disposition=attachment%3Bfilename%3D%22image.png%22%3Bfilename%2A%3DUTF-8%27%27image.png)
配置输入参数,参数标识固定为'code'
![image #60% #auto](https://drive.docs.qq.com/614c4671a8ea4378a93a06c04bd5b060?sign=65eeaccc63fe8705e277d9ca2d02e3f1&ts=1749523010&response-content-disposition=attachment%3Bfilename%3D%22image.png%22%3Bfilename%2A%3DUTF-8%27%27image.png)
数据类型选择枚举,配置项对应iboot产品配置物模型的功能模型,枚举项的value对应接口代码
![image #60% #auto](https://drive.docs.qq.com/7384dda3558e47169b56040f0704769d?sign=35f1f633dee413e9b2e31a6b19e686d9&ts=1749523010&response-content-disposition=attachment%3Bfilename%3D%22image.png%22%3Bfilename%2A%3DUTF-8%27%27image.png)
![image #60% #auto](https://drive.docs.qq.com/9afdbb8adae7496d95cce5d0078b0659?sign=690765429fe8bd68ffbf6976f23ad858&ts=1749523010&response-content-disposition=attachment%3Bfilename%3D%22image.png%22%3Bfilename%2A%3DUTF-8%27%27image.png)
配置完成后在设备功能中使用
![image #60% #auto](https://drive.docs.qq.com/bfcf272035e540d28bdcbdd3360e48b8?sign=015ec19d73ae9abd57fcc6bd266abf0e&ts=1749523010&response-content-disposition=attachment%3Bfilename%3D%22image.png%22%3Bfilename%2A%3DUTF-8%27%27image.png)

