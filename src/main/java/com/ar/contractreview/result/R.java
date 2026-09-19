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
     * <p>
     * 支持两种用法（2026-09-06 条款库接口改造）：
     * 1. 键值对形式：R.ok().data("key", value)，内部仍是 Map，可以链式放多个 key（历史用法，完全兼容）；
     * 2. 整体形式：R.ok().data(true) / R.ok().data(任意对象)，直接替换整个 data，
     *    用于文档要求 data 直接是布尔值/对象的情况（如删除条款接口返回 data: true）。
     * 默认初始化为空 Map，保证不设置数据时返回 "data": {}，与历史行为一致。
     * </p>
     */
    private Object data = new HashMap<String, Object>();

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
     * <p>内部把 data 当作 Map 使用；若 data 之前被 data(Object) 整体替换过，则重新初始化为 Map</p>
     *
     * @param key   数据键
     * @param value 数据值
     * @return 当前响应对象，支持链式调用
     */
    @SuppressWarnings("unchecked")
    public R data(String key, Object value) {
        this.setCode(ResponseCode.SUCCESS.getCode());
        this.setMessage(ResponseCode.SUCCESS.getMessage());
        // data 默认是 Map；万一之前被 data(Object) 替换成过非 Map，这里重建 Map 保证不报 ClassCastException
        if (!(this.data instanceof Map)) {
            this.data = new HashMap<>();
        }
        ((Map<String, Object>) this.data).put(key, value);
        return this;
    }

    /**
     * 添加响应数据（批量键值对）
     *
     * @param dataMap 数据映射
     * @return 当前响应对象，支持链式调用
     */
    @SuppressWarnings("unchecked")
    public R data(Map<String, Object> dataMap) {
        this.setCode(ResponseCode.SUCCESS.getCode());
        this.setMessage(ResponseCode.SUCCESS.getMessage());
        if (!(this.data instanceof Map)) {
            this.data = new HashMap<>();
        }
        ((Map<String, Object>) this.data).putAll(dataMap);
        return this;
    }

    /**
     * 直接设置响应数据（整体替换），用于返回非 Map 结构的数据
     * <p>
     * 例如：删除条款接口文档要求返回 "data": true，此时用 R.ok().data(true)；
     * 注意：调用本方法后 data 不再是 Map，再调 data("key", value) 会重建新 Map 并覆盖原数据
     * </p>
     *
     * @param value 响应数据（布尔值、对象、集合等任意类型）
     * @return 当前响应对象，支持链式调用
     */
    public R data(Object value) {
        this.setCode(ResponseCode.SUCCESS.getCode());
        this.setMessage(ResponseCode.SUCCESS.getMessage());
        this.data = value;
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