package com.iteaj.framework.consts;

import com.baomidou.mybatisplus.core.enums.IEnum;

/**
 * 默认的功能点
 */
public enum FunctionType implements IEnum<String> {
    View("浏览")
    ,Query("查询")
    , Add("新增")
    , Del("删除")
    , Edit("更新")
    , Import("导入")
    , Export("导出");

    private String desc;

    FunctionType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }


    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public String getValue() {
        return this.getDesc();
    }
}
