package com.iteaj.framework.security;

public class JwtRequestContext {

    public static final String ATTR_PRINCIPAL = JwtRequestContext.class.getName() + ".PRINCIPAL";

    public static final String ATTR_AUTHENTICATED = JwtRequestContext.class.getName() + ".AUTHENTICATED";

    private JwtRequestContext() {
    }
}
