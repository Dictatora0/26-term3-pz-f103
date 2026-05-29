package com.iteaj.iboot.module.core.enums;

import com.baomidou.mybatisplus.annotation.IEnum;

public enum Sex implements IEnum<String> {
    man, woman, non;

    @Override
    public String getValue() {
        return this.name();
    }
}
