package com.iteaj.iboot.plugin.code.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.iteaj.framework.BaseEntity;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * create time: 2020/4/27
 *
 * @author iteaj
 * @since 1.0
 */
@Data
@Accessors(chain = true)
@TableName("t_gen_filed")
public class GenField extends BaseEntity {

    private String name;
    private String type;
    private String ctype;
    private String title;
    private String remark;
    private String comment;
    private String dictType;
    private boolean keyFlag;
    private String validate;
    private String tableName;
    private String columnType;
    private Boolean editFlag;
    private Boolean searchFlag;
    private String propertyName;
    private boolean identityFlag;

    /**创建时间*/
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date createTime;

    /**更新时间*/
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date updateTime;

    public GenField() {

    }

    public GenField(boolean identityFlag, String columnType) {
        this.identityFlag = identityFlag;
        this.columnType = columnType;
    }

}
