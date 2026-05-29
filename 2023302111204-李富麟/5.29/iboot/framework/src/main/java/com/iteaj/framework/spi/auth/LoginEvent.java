package com.iteaj.framework.spi.auth;

import com.iteaj.framework.spi.event.FrameworkEvent;

/**
 * create time: 2021/4/3
 *
 * @author iteaj
 * @since 1.0
 */
public class LoginEvent implements FrameworkEvent {

    private AuthToken source;

    public LoginEvent(AuthToken source) {
        this.source = source;
    }

    @Override
    public Object getSource() {
        return this.source;
    }
}
