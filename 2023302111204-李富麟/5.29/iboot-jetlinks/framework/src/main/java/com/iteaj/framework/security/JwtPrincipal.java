package com.iteaj.framework.security;

import com.iteaj.framework.BaseEntity;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class JwtPrincipal extends BaseEntity {

    private String account;

    private String name;

    private String clientId;

    private String jti;

    private String issuer;

    private List<String> roles = new ArrayList<>();

    private List<String> permissions = new ArrayList<>();

    private List<String> scopes = new ArrayList<>();
}
