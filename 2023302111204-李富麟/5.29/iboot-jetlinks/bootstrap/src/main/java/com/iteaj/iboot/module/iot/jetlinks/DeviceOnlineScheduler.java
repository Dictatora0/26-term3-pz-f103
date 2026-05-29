package com.iteaj.iboot.module.iot.jetlinks;

import me.x1ao.jetlinks.service.JetLinksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DeviceOnlineScheduler {

    @Autowired
    private JetLinksService jetLinksService;

    /**
     * 定时执行设备上线操作，单位：毫秒
     * 每 5 分钟执行一次（初始延迟 10 秒）
     */
    // @Scheduled(fixedRate = 300000, initialDelay = 10000)
    // @PostConstruct
    public void scheduleDeviceOnline() {
        System.out.println("开始执行设备上线任务...");

        try {
            JetLinksService.ApiResponse response = jetLinksService.deviceOnline();

            if (response.isSuccess()) {
                System.out.println("设备上线成功！");
            } else {
                System.err.println("设备上线失败，原因: " + response.getMessage());
            }
        } catch (Exception e) {
            System.err.println("设备上线异常: " + e.getMessage());
            e.printStackTrace();
        }
    }


}
