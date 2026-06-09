package com.iteaj.framework.security;

import com.iteaj.framework.spi.admin.auth.AuthenticationUser;

public interface AuthenticationService {

    AuthenticationUser getByAccount(String account);

    default AuthenticationUser getById(Object id) {
        return null;
    }
}
