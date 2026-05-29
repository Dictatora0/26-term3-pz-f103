package com.iteaj.framework.spi.auth;

import com.iteaj.framework.Entity;

import java.util.Collection;

public class DefaultAuthToken implements AuthToken{
    private Entity principal;
    private Object credentials;

    private Collection<String> roles;
    private Collection<String> permissions;

    public DefaultAuthToken(Entity principal, Object credentials
            , Collection<String> roles, Collection<String> permissions) {
        this.principal = principal;
        this.credentials = credentials;
        this.roles = roles;
        this.permissions = permissions;
    }

    @Override
    public Entity getPrincipal() {
        return this.principal;
    }

    @Override
    public Object getCredentials() {
        return this.credentials;
    }

    @Override
    public Collection<String> getRoles() {
        return this.roles;
    }

    @Override
    public Collection<String> getPermissions() {
        return this.permissions;
    }
}
