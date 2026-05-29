package com.iteaj.iboot.module.iot.dto;

import com.iteaj.iboot.module.iot.consts.DeviceStatus;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class ProductTree {

    /**
     * 产品、产品类型id
     */
    private Long id;

    /**
     * 父级id
     */
    private Long pid;

    /**
     * 树节点类型
     */
    private String type;

    /**
     * 唯一key
     */
    private String key;

    /**
     * 父级key
     */
    private String pkey;

    /**
     * 名称
     */
    private String name;

    /**
     * 是否叶子节点
     */
    private Boolean isLeaf;

    /**
     * 是否禁用
     */
    private Boolean disabled;

    /**
     * 子数据
     */
    private List<ProductTree> children;

    public ProductTree() { }

    protected ProductTree(Long id, Long pid, String type, String name) {
        this(id, pid, type, name, null);
    }

    protected ProductTree(Long id, Long pid, String type, String name, Boolean disabled) {
        this.id = id;
        this.pid = pid;
        this.type = type;
        this.name = name;
        this.disabled = disabled;
        this.key = type + ":" + id;
        this.pkey = type + ":" + pid;
    }

    public static ProductTree buildProduct(Long id, Long pid, String name) {
        return new ProductTree(id, pid, "product", name).setPkey("productType:"+pid).setIsLeaf(true);
    }

    public static ProductTree buildProductType(Long id, Long pid, String name) {
        return new ProductTree(id, pid, "productType", name).setIsLeaf(false);
    }

    public ProductTree addChildren(ProductTree tree) {
        if(children == null) {
            this.children = new ArrayList<>();
        }

        this.children.add(tree);
        return this;
    }
}
