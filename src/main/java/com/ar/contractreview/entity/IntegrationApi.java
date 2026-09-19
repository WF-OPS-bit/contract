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
 * API开放配置表
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("integration_api")
public class IntegrationApi implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * API配置ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

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
     * HTTP方法：GET，POST，PUT，DELETE
     */
    @TableField("http_method")
    private String httpMethod;

    /**
     * API版本号（如 1.0）
     * 说明：《接口文档.md》20.1 响应要求返回 apiVersion，原实体/建表SQL漏了该字段，2026-09-11 补上；
     * 注意：数据库 integration_api 表也需要手动执行 ALTER TABLE 增加 api_version 列，否则查询会报 Unknown column
     */
    @TableField("api_version")
    private String apiVersion;

    /**
     * 状态：ACTIVE-启用，INACTIVE-禁用
     */
    @TableField("status")
    private String status;

    /**
     * 认证类型：NONE-无需认证，API_KEY-API密钥，TOKEN-令牌，OAUTH2-OAuth2
     */
    @TableField("auth_type")
    private String authType;

    /**
     * 认证配置（JSON格式）
     */
    @TableField("auth_config")
    private String authConfig;

    /**
     * 限流（次/分钟，0表示不限流）
     */
    @TableField("rate_limit")
    private Integer rateLimit;

    /**
     * 描述
     */
    @TableField("description")
    private String description;

    /**
     * 创建人
     */
    @TableField("created_by")
    private String createdBy;

    /**
     * 创建时间
     */
    @TableField("created_time")
    private LocalDateTime createdTime;

    /**
     * 更新人
     */
    @TableField("updated_by")
    private String updatedBy;

    /**
     * 更新时间
     */
    @TableField("updated_time")
    private LocalDateTime updatedTime;
}
