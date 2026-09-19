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
 * OA对接日志表
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("integration_oa_log")
public class IntegrationOaLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 对接日志ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * OA对接ID
     */
    @TableField("oa_id")
    private Long oaId;

    /**
     * 日志类型：SYNC-同步，MESSAGE-消息推送，CALLBACK-回调
     */
    @TableField("log_type")
    private String logType;

    /**
     * 操作动作
     */
    @TableField("action")
    private String action;

    /**
     * 状态：SUCCESS-成功，FAILED-失败
     */
    @TableField("status")
    private String status;

    /**
     * 请求数据（JSON格式）
     */
    @TableField("request_data")
    private String requestData;

    /**
     * 响应数据（JSON格式）
     */
    @TableField("response_data")
    private String responseData;

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

    /**
     * 创建时间
     */
    @TableField("created_time")
    private LocalDateTime createdTime;
}
