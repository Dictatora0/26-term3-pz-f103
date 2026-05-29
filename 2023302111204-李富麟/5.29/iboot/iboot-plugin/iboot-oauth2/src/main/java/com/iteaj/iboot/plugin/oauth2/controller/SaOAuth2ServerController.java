package com.iteaj.iboot.plugin.oauth2.controller;

import cn.dev33.satoken.oauth2.config.SaOAuth2Config;
import cn.dev33.satoken.oauth2.logic.SaOAuth2Handle;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

@RequestMapping
@RestController
public class SaOAuth2ServerController {

    // 处理所有OAuth相关请求
    @RequestMapping("/oauth2/*")
    public Object request() {
        return SaOAuth2Handle.serverRequest();
    }

    // Sa-OAuth2 定制化配置
    @Autowired
    public void setSaOAuth2Config(SaOAuth2Config cfg) {
        // 配置：未登录时返回的View
        cfg.setNotLoginView(() -> new ModelAndView("/login.html"))
                .setDoLoginHandle((name, pwd) -> {// 配置：登录处理函数
                    if("sa".equals(name) && "123456".equals(pwd)) {
                        StpUtil.login(10001);
                        return SaResult.ok();
                    }
                    return SaResult.error("账号名或密码错误");
                }).setConfirmView((clientId, scope) -> {// 配置：确认授权时返回的View
                    Map<String, Object> map = new HashMap<>();
                    map.put("clientId", clientId);
                    map.put("scope", scope);
                    return new ModelAndView("/confirm.html", map);
                });
    }

    // 全局异常拦截
    @ExceptionHandler
    public SaResult handlerException(Exception e) {
        e.printStackTrace();
        return SaResult.error(e.getMessage());
    }
}
