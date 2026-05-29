package com.iteaj.framework.utils;

/**
 * create time: 2020/5/2
 *
 * @author iteaj
 * @since 1.0
 */
public class StringUtils {

    /**
     * 首字母小写
     * @param name
     * @return
     */
    public static String firstLowerCase(String name) {
        String first = String.valueOf(name.charAt(0));
        return name.replaceFirst(first, first.toLowerCase());
    }
}
