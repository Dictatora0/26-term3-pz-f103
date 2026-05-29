package com.iteaj.iboot.plugin.shiro;

import com.iteaj.framework.security.SecurityToken;
import lombok.Data;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.RememberMeAuthenticationToken;

@Data
public class ShiroSecurityToken extends SecurityToken implements RememberMeAuthenticationToken {

    @Override
    public Object getPrincipal() {
        return this.getAccount();
    }

    @Override
    public Object getCredentials() {
        return this.getPassword();
    }
}
