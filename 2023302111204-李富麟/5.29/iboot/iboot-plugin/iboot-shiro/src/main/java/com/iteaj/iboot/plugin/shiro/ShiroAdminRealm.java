package com.iteaj.iboot.plugin.shiro;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.google.code.kaptcha.Constants;
import com.iteaj.framework.Entity;
import com.iteaj.framework.captcha.Captcha;
import com.iteaj.framework.captcha.CaptchaService;
import com.iteaj.framework.consts.CoreConst;
import com.iteaj.framework.security.AuthenticationService;
import com.iteaj.framework.security.AuthorizationService;
import com.iteaj.framework.security.SecurityException;
import com.iteaj.framework.security.SecurityUtil;
import com.iteaj.framework.spi.admin.auth.AuthenticationUser;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpSession;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * create time: 2021/3/27
 *  shiro 后台管理端认证和授权的实现
 * @author iteaj
 * @since 1.0
 */
public class ShiroAdminRealm extends AuthorizingRealm {

    @Autowired
    private CaptchaService captchaService;
    @Autowired
    private AuthorizationService authorizationService;
    @Autowired
    private AuthenticationService authenticationService;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token != null && ShiroSecurityToken.class.isAssignableFrom(token.getClass());
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        Entity admin = (Entity) principals.getPrimaryPrincipal();
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();

        Collection<String> permissions = this.authorizationService.getPermissions(admin.getId());
        authorizationInfo.addStringPermissions(permissions);

        return authorizationInfo;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        ShiroSecurityToken shiroToken = (ShiroSecurityToken) token;

        String code = shiroToken.getCode();
        if(StrUtil.isBlank(code)) {
            throw new SecurityException("验证码已失效");
        }

        // 获取校验码并判断是否过期
        Captcha serviceCaptcha = captchaService.removeCaptcha(code);
        if(serviceCaptcha == null || serviceCaptcha.isExpire()) {
            throw new SecurityException("验证码已失效");
        }

        /**
         * 获取校验码
         * @see com.iteaj.framework.autoconfigure.CaptchaAutoConfiguration#captcha(HttpSession, HttpServletResponse) 获取校验码接口
         */
        String captcha = shiroToken.getCaptcha();
        if(!Objects.equals(serviceCaptcha.getCaptcha(), captcha)) {
            throw new SecurityException("验证码校验失败");
        }

        String userName = shiroToken.getAccount();
        String password = shiroToken.getPassword();
        AuthenticationUser admin = authenticationService.getByAccount(userName);
        if(admin == null) {
            throw new SecurityException("账户不存在");
        }

        if(!admin.allowLogin()) {
            throw new SecurityException("账号已被禁用");
        }

        // 密码校验
        String passwordDigest = admin.getPassword().toUpperCase();
        String md5Hex = SecureUtil.md5(password).toUpperCase();

        if(!Objects.equals(passwordDigest, md5Hex)) {
            throw new SecurityException("密码校验失败");
        }

        return new SimpleAuthenticationInfo(admin, token.getCredentials(), this.getName());
    }

    /**
     * 超级管理员包含所有的权限
     * @param principals
     * @param permission
     * @return
     */
    @Override
    public boolean isPermitted(PrincipalCollection principals, String permission) {
        return SecurityUtil.isSuper() ||
                super.isPermitted(principals, permission);
    }

    /**
     * 超级管理员拥有所有的角色
     * @param principal
     * @param roleIdentifier
     * @return
     */
    @Override
    public boolean hasRole(PrincipalCollection principal, String roleIdentifier) {
        return SecurityUtil.isSuper() ||
            super.hasRole(principal, roleIdentifier);
    }
}
