package com.iteaj.iboot.module.core.dto;

import lombok.Data;

/**
 * create time: 2021/6/8
 *
 * @author iteaj
 * @since 1.0
 */
@Data
public class PasswordDto {

    /**
     * 用户唯一标识
     */
    private Long id;
    /**
     * 旧密码
     */
    private String oldPwd;
    /**
     * 新密码
     */
    private String password;
}
