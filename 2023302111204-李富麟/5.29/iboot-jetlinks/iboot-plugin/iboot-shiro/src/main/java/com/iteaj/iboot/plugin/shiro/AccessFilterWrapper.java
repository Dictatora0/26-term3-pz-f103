package com.iteaj.iboot.plugin.shiro;

import org.apache.shiro.web.filter.AccessControlFilter;

/**
 * create time: 2021/3/24
 *
 * @author iteaj
 * @since 1.0
 */
public class AccessFilterWrapper {

    private String name;

    private AccessControlFilter filter;

    public AccessFilterWrapper(String name, AccessControlFilter filter) {
        this.name = name;
        this.filter = filter;
        filter.setName(name);
    }

    public String getName() {
        return name;
    }

    public AccessFilterWrapper setName(String name) {
        this.name = name;
        return this;
    }

    public AccessControlFilter getFilter() {
        return filter;
    }

    public AccessFilterWrapper setFilter(AccessControlFilter filter) {
        this.filter = filter;
        return this;
    }
}
