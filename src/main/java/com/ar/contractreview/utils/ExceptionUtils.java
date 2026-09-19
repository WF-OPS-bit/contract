package com.ar.contractreview.utils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * 异常工具类，提供异常处理相关的静态方法
 *
 * @author wyh
 */
public class ExceptionUtils {

    /**
     * 获取异常的完整堆栈信息
     *
     * @param err 异常对象
     * @return 异常堆栈信息字符串
     */
    public static String getExceptionStackInfo(Exception err) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintStream ps = new PrintStream(baos)) {
            err.printStackTrace(ps);
            return baos.toString();
        }
    }
}