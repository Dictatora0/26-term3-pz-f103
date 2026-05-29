package com.iteaj.iboot.module.core.controller;

import com.iteaj.framework.BaseController;
import com.iteaj.framework.logger.Logger;
import com.iteaj.framework.logger.LoggerType;
import com.iteaj.framework.result.Result;
import com.iteaj.framework.security.SecurityException;
import com.iteaj.framework.security.SecurityToken;
import com.iteaj.framework.security.SecurityUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用户管理中心接口
 * @author iteaj
 * @since 1.0
 */
@RestController
@RequestMapping("/core")
public class LoginController extends BaseController {

    /**
     * 系统登录
     * @return
     */
    @PostMapping("login")
    @Logger(value = "系统登录", type = LoggerType.Login)
    public Result login(@RequestBody SecurityToken token, HttpServletRequest request, HttpServletResponse response) {
        try {
            SecurityUtil.login(token, request, response);
        } catch (SecurityException e) {
            return fail(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return fail("登录失败");
        }

        return success("登录成功");
    }

    /**
     * 系统注销
     * @return
     */
    @PostMapping("logout")
    @Logger(value = "系统注销", type = LoggerType.Logout)
    public Result logout(HttpServletRequest request, HttpServletResponse response) {
        SecurityUtil.logout(request, response);
        return success("注销成功");
    }
}
