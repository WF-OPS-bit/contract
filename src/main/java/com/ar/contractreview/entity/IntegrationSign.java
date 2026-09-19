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
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 电子签章配置表
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("integration_sign")
public class IntegrationSign implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 电子签章ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 签章类型：ALIPAY-支付宝，WECHAT-微信，CA-CA证书，OTHER-其他
     */
    @TableField("sign_type")
    private String signType;

    /**
     * 签章名称
     */
    @TableField("sign_name")
    private String signName;

    /**
     * 状态：ACTIVE-启用，INACTIVE-禁用
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
    @TableField("app_key")
    private String appKey;

    /**
     * 证书路径
     */
    @TableField("certificate_path")
    private String certificatePath;

    /**
     * 证书编号
     */
    @TableField("certificate_number")
    private String certificateNumber;

    /**
     * 证书到期日期
     */
    @TableField("certificate_expire_date")
    private LocalDate certificateExpireDate;

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
