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
 * API调用日志表
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("integration_api_log")
public class IntegrationApiLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 调用日志ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * API配置ID
     */
    @TableField("api_id")
    private Long apiId;

    /**
     * API名称
     */
    @TableField("api_name")
    private String apiName;

    /**
     * API路径
     */
    @TableField("api_path")
    private String apiPath;

    /**
     * HTTP方法
     */
    @TableField("http_method")
    private String httpMethod;

    /**
     * 请求时间
     */
    @TableField("request_time")
    private LocalDateTime requestTime;

    /**
     * 请求IP
     */
    @TableField("request_ip")
    private String requestIp;

    /**
     * 请求参数（JSON格式）
     */
    @TableField("request_param")
    private String requestParam;

    /**
     * 响应状态码
     */
    @TableField("response_code")
    private Integer responseCode;

    /**
     * 响应数据
     */
    @TableField("response_data")
    private String responseData;

    /**
     * 状态：SUCCESS-成功，FAILED-失败
     */
    @TableField("status")
    private String status;

    /**
     * 错误信息
     */
    @TableField("error_message")
    private String errorMessage;

    /**
     * 执行时间（毫秒）
     */
    @TableField("execution_time")
    private Integer executionTime;
}
