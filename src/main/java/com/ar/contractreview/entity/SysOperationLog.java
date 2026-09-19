package com.ar.contractreview.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 操作日志表
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("sys_operation_log")
public class SysOperationLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 操作人ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 操作人姓名
     */
    @TableField("user_name")
    private String userName;

    /**
     * 操作类型：CREATE-新增，UPDATE-修改，DELETE-删除，QUERY-查询，LOGIN-登录，LOGOUT-退出
     */
    @TableField("operation_type")
    private String operationType;

    /**
     * 操作模块
     */
    @TableField("module")
    private String module;

    /**
     * 操作描述
     */
    @TableField("description")
    private String description;

    /**
     * 请求URL
     */
    @TableField("request_url")
    private String requestUrl;

    /**
     * 请求方法：GET，POST，PUT，DELETE
     */
    @TableField("request_method")
    private String requestMethod;

    /**
     * 请求参数（JSON格式）
     */
    @TableField("request_param")
    private String requestParam;

    /**
     * 响应结果
     */
    @TableField("response_result")
    private String responseResult;

    /**
     * IP地址
     */
    @TableField("ip_address")
    private String ipAddress;

    /**
     * 浏览器信息
     */
    @TableField("user_agent")
    private String userAgent;

    /**
     * 执行时间（毫秒）
     */
    @TableField("execution_time")
    private Integer executionTime;

    /**
     * 是否成功：0-失败，1-成功
     */
    @TableField("success")
    private Byte success;

    /**
     * 错误信息
     */
    @TableField("error_message")
    private String errorMessage;

    /**
     * 操作时间
     */
    @TableField("created_time")
    private LocalDateTime createdTime;
}
