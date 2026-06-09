package com.iteaj.framework.security;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class JwtTokenClaims {

    private Long userId;

    private String account;

    private String displayName;

    private String clientId;

    private String issuer;

    private String audience;

    private String jti;

    private long issuedAt;

    private long expiresAt;

    private List<String> roles = new ArrayList<>();

    private List<String> permissions = new ArrayList<>();

    private List<String> scopes = new ArrayList<>();
}
