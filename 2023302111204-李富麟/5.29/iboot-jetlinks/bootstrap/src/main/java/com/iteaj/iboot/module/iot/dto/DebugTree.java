package com.iteaj.iboot.module.iot.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iteaj.iboot.module.iot.consts.DeviceStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class DebugTree {

    /**
     * 设备、产品、产品类型id
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
     * 是否存在产品
     */
    private boolean product;

    /**
     * 设备状态
     */
    private DeviceStatus status;

    /**
     * 子数据
     */
    private List<DebugTree> children;

    @JsonIgnore
    private DebugTree parent;

    public DebugTree() { }

    protected DebugTree(Long id, Long pid, String type, String name) {
        this(id, pid, type, name, null);
    }

    protected DebugTree(Long id, Long pid, String type, String name, DeviceStatus status) {
        this.id = id;
        this.pid = pid;
        this.type = type;
        this.name = name;
        this.status = status;
        this.key = type + ":" + id;
        this.pkey = type + ":" + pid;
    }

    public static DebugTree buildProduct(Long id, Long pid, String name) {
        return new DebugTree(id, pid, "product", name).setPkey("productType:"+pid).setIsLeaf(false);
    }

    public static DebugTree buildProductType(Long id, Long pid, String name) {
        return new DebugTree(id, pid, "productType", name).setIsLeaf(true);
    }

    public static DebugTree buildDevice(Long id, Long pid, String name, DeviceStatus status) {
        return new DebugTree(id, pid, "device", name, status).setPkey("product:"+pid).setIsLeaf(true);
    }

    public DebugTree addChildren(DebugTree tree) {
        if(children == null) {
            this.children = new ArrayList<>();
        }

        this.children.add(tree.setParent(this));
        if(tree.getType().equals("product")) {
            buildProductTree(this);
        }
        return this;
    }

    private void buildProductTree(DebugTree debugTree) {
        debugTree.setProduct(true);
        debugTree.setIsLeaf(false);
        if(debugTree.getParent() != null) {
            buildProductTree(debugTree.getParent());
        }
    }
}
