package com.iteaj.iboot.module.core.controller;

import com.iteaj.framework.BaseController;
import com.iteaj.framework.logger.Logger;
import com.iteaj.framework.logger.LoggerType;
import com.iteaj.framework.result.Result;
import com.iteaj.framework.security.SecurityException;
import com.iteaj.framework.security.SecurityToken;
import com.iteaj.framework.security.SecurityUtil;
import com.iteaj.iboot.module.core.dto.AuthTokenDto;
import com.iteaj.iboot.module.core.service.AuthTokenService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping
public class LoginController extends BaseController {

    private final AuthTokenService authTokenService;

    public LoginController(AuthTokenService authTokenService) {
        this.authTokenService = authTokenService;
    }

    @PostMapping("/core/login")
    @Logger(value = "系统登录", type = LoggerType.Login)
    public Result<AuthTokenDto> login(@RequestBody SecurityToken token,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {
        try {
            AuthTokenDto authToken = authTokenService.issueToken(token, request, response);
            return success(authToken, "登录成功");
        } catch (SecurityException e) {
            return fail(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return fail("登录失败");
        }
    }

    @PostMapping("/auth/token")
    public Result<AuthTokenDto> token(@RequestBody SecurityToken token,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {
        try {
            return success(authTokenService.issueToken(token, request, response), "令牌签发成功");
        } catch (SecurityException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            return fail("令牌签发失败");
        }
    }

    @PostMapping("/core/logout")
    @Logger(value = "系统注销", type = LoggerType.Logout)
    public Result logout(HttpServletRequest request, HttpServletResponse response) {
        SecurityUtil.logout(request, response);
        return success("注销成功");
    }
}
