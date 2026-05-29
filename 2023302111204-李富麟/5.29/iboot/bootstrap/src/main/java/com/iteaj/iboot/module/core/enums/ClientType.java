package com.iteaj.iboot.module.core.enums;

public enum ClientType {
    COMPUTER("电脑"),
    MOBILE("移动设备"),
    TABLET("平板"),
    GAME_CONSOLE("游戏设备"), // Game console
    DMR("数字媒体"), // Digital media receiver
    WEARABLE("可穿戴设备"), // Wearable computer
    UNKNOWN("未知设备"); // Unknown

    String name;

    ClientType(String name) {
        this.name = name;
    }
}
