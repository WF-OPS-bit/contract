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
 * 电子签章记录表
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("integration_sign_record")
public class IntegrationSignRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 签章记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 电子签章ID
     */
    @TableField("sign_id")
    private Long signId;

    /**
     * 合同ID
     */
    @TableField("contract_id")
    private Long contractId;

    /**
     * 合同编号
     */
    @TableField("contract_no")
    private String contractNo;

    /**
     * 签章时间
     */
    @TableField("sign_time")
    private LocalDateTime signTime;

    /**
     * 签章人ID
     */
    @TableField("sign_user_id")
    private Long signUserId;

    /**
     * 签章人姓名
     */
    @TableField("sign_user_name")
    private String signUserName;

    /**
     * 签章位置（JSON格式）
     */
    @TableField("sign_position")
    private String signPosition;

    /**
     * 签章图片URL
     */
    @TableField("signature_image")
    private String signatureImage;

    /**
     * 验证状态：PENDING-待验证，VERIFIED-已验证，FAILED-验证失败
     */
    @TableField("verify_status")
    private String verifyStatus;

    /**
     * 验证时间
     */
    @TableField("verify_time")
    private LocalDateTime verifyTime;

    /**
     * 验证结果
     */
    @TableField("verify_result")
    private String verifyResult;

    /**
     * 创建时间
     */
    @TableField("created_time")
    private LocalDateTime createdTime;
}
