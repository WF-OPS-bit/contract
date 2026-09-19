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
 * OA对接配置表
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("integration_oa")
public class IntegrationOa implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * OA对接ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * OA类型：WECOM-企业微信，DINGTALK-钉钉，FEISHU-飞书，OTHER-其他
     */
    @TableField("oa_type")
    private String oaType;

    /**
     * OA名称
     */
    @TableField("oa_name")
    private String oaName;

    /**
     * 状态：CONNECTED-已连接，DISCONNECTED-已断开，ERROR-连接异常
     */
    @TableField("status")
    private String status;

    /**
     * 应用ID
     */
    @TableField("app_id")
    private String appId;

    /**
     * 应用密钥
     */
    @TableField("app_secret")
    private String appSecret;

    /**
     * 访问令牌
     */
    @TableField("access_token")
    private String accessToken;

    /**
     * 令牌过期时间
     */
    @TableField("token_expire_time")
    private LocalDateTime tokenExpireTime;

    /**
     * 同步状态：ENABLED-已启用，DISABLED-已禁用
     */
    @TableField("sync_status")
    private String syncStatus;

    /**
     * 最后同步时间
     */
    @TableField("last_sync_time")
    private LocalDateTime lastSyncTime;

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
