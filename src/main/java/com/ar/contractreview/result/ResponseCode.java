package com.ar.contractreview.result;

/**
 * 响应码枚举，定义系统中所有业务状态码和对应的提示消息
 *
 * @author wyh
 */
public enum ResponseCode {

    /**
     * 请求成功
     */
    SUCCESS(200, "请求成功"),

    /**
     * 请求失败
     */
    FAIL(-1, "请求失败"),

    /**
     * 参数异常
     */
    PARAMETER_EXCEPTION(100, "参数有误"),

    /**
     * 用户名不正确
     */
    USERNAME_EXCEPTION(101, "用户名不对"),

    /**
     * 密码不正确
     */
    PASSWORD_EXCEPTION(102, "密码不对"),

    /**
     * 用户状态异常
     */
    USER_STATUS_EXCEPTION(103, "用户状态有误"),

    /**
     * SQL异常
     */
    SQL_EXCEPTION(104, "SQL异常"),

    /**
     * 系统异常
     */
    SYSTEM_EXCEPTION(120, "系统异常"),
    /**
     * 用户名重复
     */
    USERNAME_REPEAT__EXCEPTION(122, "用户名重复"),

    /**
     * 身份不合法
     */
    USER_INVALIDATE_EXCEPTION(401, "身份不合法"),

    /**
     * 登录的json信息获取错误
     */
    LOGIN_JSON_GET__EXCEPTION(124, "登录的json信息获取错误"),

    /**
     * 登录失败
     */
    LOGIN_EXCEPTION(125, "登录失败"),

    /**
     * 没有权限访问
     */
    NO_PERM_EXCEPTION(403, "没有权限访问"),
    /**
     * 菜单不存在
     */
    NO_MENU_EXCEPTION(404, "菜单不存在");

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 错误提示信息
     */
    private final String message;

    ResponseCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}