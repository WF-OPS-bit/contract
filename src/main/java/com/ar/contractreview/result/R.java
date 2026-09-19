package com.ar.contractreview.result;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 统一响应结果封装类
 *
 * @author wyh
 */
@Data
public class R {

    /**
     * 响应状态码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private Map<String, Object> data = new HashMap<>();

    /**
     * 私有构造函数，不允许外部直接实例化
     */
    private R() {
    }

    /**
     * 创建成功响应
     *
     * @return 成功响应对象
     */
    public static R ok() {
        R r = new R();
        r.setCode(ResponseCode.SUCCESS.getCode());
        r.setMessage(ResponseCode.SUCCESS.getMessage());
        return r;
    }

    /**
     * 添加响应数据（单个键值对）
     *
     * @param key   数据键
     * @param value 数据值
     * @return 当前响应对象，支持链式调用
     */
    public R data(String key, Object value) {
        this.setCode(ResponseCode.SUCCESS.getCode());
        this.setMessage(ResponseCode.SUCCESS.getMessage());
        this.data.put(key, value);
        return this;
    }

    /**
     * 添加响应数据（批量键值对）
     *
     * @param dataMap 数据映射
     * @return 当前响应对象，支持链式调用
     */
    public R data(Map<String, Object> dataMap) {
        this.setCode(ResponseCode.SUCCESS.getCode());
        this.setMessage(ResponseCode.SUCCESS.getMessage());
        this.data.putAll(dataMap);
        return this;
    }

    /**
     * 创建失败响应（默认失败码）
     *
     * @return 失败响应对象
     */
    public static R fail() {
        R r = new R();
        r.setCode(ResponseCode.FAIL.getCode());
        r.setMessage(ResponseCode.FAIL.getMessage());
        return r;
    }

    /**
     * 创建失败响应（自定义错误码和消息）
     *
     * @param code    错误码
     * @param message 错误消息
     * @return 失败响应对象
     */
    public static R fail(Integer code, String message) {
        R r = new R();
        r.setCode(code);
        r.setMessage(message);
        return r;
    }

    /**
     * 创建失败响应（使用预定义响应码）
     *
     * @param responseCode 预定义响应码
     * @return 失败响应对象
     */
    public static R fail(ResponseCode responseCode) {
        R r = new R();
        r.setCode(responseCode.getCode());
        r.setMessage(responseCode.getMessage());
        return r;
    }
}