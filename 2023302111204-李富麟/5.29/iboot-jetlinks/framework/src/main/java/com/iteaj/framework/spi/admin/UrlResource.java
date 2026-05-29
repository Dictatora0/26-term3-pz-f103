package com.iteaj.framework.spi.admin;

/**
 * 可以访问的
 */
public interface UrlResource extends TreeResource {

    String getUrl();

    UrlResource setUrl(String url);
}
