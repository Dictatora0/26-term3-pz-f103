package com.iteaj.iboot.module.core.service;

import com.iteaj.framework.security.SecurityToken;
import com.iteaj.iboot.module.core.dto.AuthTokenDto;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface AuthTokenService {

    AuthTokenDto issueToken(SecurityToken token, HttpServletRequest request, HttpServletResponse response);
}
