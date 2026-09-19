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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 合同归档表
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("contract_archive")
public class ContractArchive implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 归档ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

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
     * 合同名称
     */
    @TableField("contract_name")
    private String contractName;

    /**
     * 合同类型
     */
    @TableField("contract_type")
    private String contractType;

    /**
     * 甲方
     */
    @TableField("party_a")
    private String partyA;

    /**
     * 乙方
     */
    @TableField("party_b")
    private String partyB;

    /**
     * 合同金额
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 归档编号
     */
    @TableField("archive_code")
    private String archiveCode;

    /**
     * 归档位置
     */
    @TableField("archive_location")
    private String archiveLocation;

    /**
     * 归档时间
     */
    @TableField("archive_time")
    private LocalDateTime archiveTime;

    /**
     * 归档人ID
     */
    @TableField("archive_user_id")
    private Long archiveUserId;

    /**
     * 归档人姓名
     */
    @TableField("archive_user_name")
    private String archiveUserName;

    /**
     * 存储类型：ELECTRONIC-电子归档，PHYSICAL-纸质归档，HYBRID-混合归档
     */
    @TableField("storage_type")
    private String storageType;

    /**
     * 保留期限（月）
     */
    @TableField("retention_period")
    private Integer retentionPeriod;

    /**
     * 到期日期
     */
    @TableField("expire_date")
    private LocalDate expireDate;

    /**
     * 归档说明
     */
    @TableField("description")
    private String description;

    /**
     * 创建时间
     */
    @TableField("created_time")
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @TableField("updated_time")
    private LocalDateTime updatedTime;
}
