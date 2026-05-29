package com.iteaj.framework.spi.admin;

/**
 * create time: 2020/6/14
 *  菜單源
 * @author iteaj
 * @since 1.0
 */
public interface MenuResource extends UrlResource {

    String getIcon();

    MenuType getType();
}
