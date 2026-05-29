package com.iteaj.iboot.module.iot;

import com.iteaj.iboot.module.iot.debug.DebugHandle;
import com.iteaj.iboot.module.iot.debug.DebugHandleFactory;
import com.iteaj.iboot.module.iot.debug.dtu.DtuDebugHandle;
import com.iteaj.iboot.module.iot.debug.websocket.WebsocketDebugListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.List;

/**
 * 调试使用的配置类
 */
@Configuration
public class IotDebugConfiguration {

//    @Bean
//    public TcpDebugServerComponent debugServerComponent() {
//        return new TcpDebugServerComponent(new ConnectProperties(6158));
//    }

    /**
     * 各种调试用的处理器
     * @see DebugHandle
     * @see DtuDebugHandle dtu调试处理
     * @param handles
     * @return
     */
    @Bean
    public DebugHandleFactory debugHandleFactory(@Autowired(required = false) List<DebugHandle> handles) {
        return new DebugHandleFactory(handles);
    }

    /**
     * 调试需要用的websocket组件
     * @param iotTaskExecutor
     * @param handleFactory
     * @return
     */
    @Bean
    public WebsocketDebugListener websocketDebugListener(ThreadPoolTaskScheduler iotTaskExecutor, DebugHandleFactory handleFactory) {
        return new WebsocketDebugListener(handleFactory, iotTaskExecutor);
    }
}
