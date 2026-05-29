package com.iteaj.framework.consts;

/**
 * jdk：1.8
 *  功能点放置位置
 * @author iteaj
 * create time：2019/5/25
 */
public enum Position {

    M("主操作栏"), // Main
    MM("主操作栏-更多"), // Main More
    T("表格操作栏"), // Table Main
    TM("表格操作栏-更多"), // Table More
    AM("主操作栏和主表操作栏(包含M和T)"); // All Main

    public String desc;

    Position(String desc) {
        this.desc = desc;
    }
}
