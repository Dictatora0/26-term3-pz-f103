package com.iteaj.iboot.module.core.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class AuthTokenDto {

    private String accessToken;

    private String refreshToken;

    private String tokenType = "Bearer";

    private long expiresIn;

    private long refreshExpiresIn;

    private String clientId;

    private List<String> scopes = new ArrayList<>();

    private Long userId;

    private String username;

    private String displayName;

    private List<String> roles = new ArrayList<>();
}
