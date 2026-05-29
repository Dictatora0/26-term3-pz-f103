package com.iteaj.framework.spi.auth;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * create time: 2021/3/24
 *
 * @author iteaj
 * @since 1.0
 */
public class AuthContext implements Serializable {

    private AuthType type;
    private String action;
    private Map<String, Serializable> params = new HashMap<>();
    protected AuthContext(AuthType type) {
        this.type = type;
    }

    /**
     * @param type 授权类型[wechat, gitee, githup, alipay, ...]
     * @return
     */
    public static AuthContext build(AuthType type) {
        return new AuthContext(type);
    }

    public AuthContext put(String key, Serializable value) {
        this.params.put(key, value);
        return this;
    }

    public Serializable get(String key) {
        return this.params.get(key);
    }

    public AuthType getType() {
        return type;
    }

    public AuthContext setType(AuthType type) {
        this.type = type;
        return this;
    }

    public Map<String, Serializable> getParams() {
        return params;
    }

    public String getAction() {
        return action;
    }

    public AuthContext setAction(String action) {
        this.action = action;
        return this;
    }

}
