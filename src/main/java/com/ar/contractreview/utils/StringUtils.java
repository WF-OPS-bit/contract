package com.ar.contractreview.utils;

/**
 * 字符串工具类，提供字符串常用操作的静态方法
 *
 * @author wyh
 */
public class StringUtils {

    /**
     * 校验字符串是否为空
     *
     * @param str 待校验的字符串
     * @return 如果为null或空字符串返回true，否则返回false
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }
}