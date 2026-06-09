package com.iteaj.framework.security;

import cn.hutool.crypto.SecureUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordCodec {

    private static final BCryptPasswordEncoder BCRYPT = new BCryptPasswordEncoder();

    private PasswordCodec() {
    }

    public static String encode(String rawPassword) {
        return BCRYPT.encode(rawPassword);
    }

    public static boolean matches(String rawPassword, String encodedPassword) {
        if(rawPassword == null || encodedPassword == null || encodedPassword.isEmpty()) {
            return false;
        }

        if(encodedPassword.startsWith("$2a$") || encodedPassword.startsWith("$2b$") || encodedPassword.startsWith("$2y$")) {
            return BCRYPT.matches(rawPassword, encodedPassword);
        }

        // 兼容历史 md5 密码
        return SecureUtil.md5(rawPassword).equalsIgnoreCase(encodedPassword);
    }
}
