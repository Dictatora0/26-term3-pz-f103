package com.iteaj.framework;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;

/**
 * 树结构的实体类.有父子之分的
 */
@Data
@Accessors(chain = true)
public abstract class TreeEntity extends BaseEntity {

    /**
     * 此节点的子节点
     */
    @TableField(exist = false)
    private Collection children;

    public abstract Long getPid();

    public TreeEntity addChild(TreeEntity entity) {
        if(CollectionUtils.isEmpty(this.children)) {
            this.children = new ArrayList<>();
        }

        this.children.add(entity);
        return this;
    }

    public Collection getChildren() {
        return children;
    }

    public void setChildren(Collection children) {
        this.children = children;
    }
}
