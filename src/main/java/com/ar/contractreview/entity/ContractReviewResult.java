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
 * 合同审核结果表
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("contract_review_result")
public class ContractReviewResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 审核结果ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 合同ID
     */
    @TableField("contract_id")
    private Long contractId;

    /**
     * 风险等级
     */
    @TableField("risk_level")
    private String riskLevel;

    /**
     * 风险评分
     */
    @TableField("risk_score")
    private Integer riskScore;

    /**
     * 风险总数
     */
    @TableField("total_risks")
    private Integer totalRisks;

    /**
     * 高风险数
     */
    @TableField("high_risks")
    private Integer highRisks;

    /**
     * 中风险数
     */
    @TableField("medium_risks")
    private Integer mediumRisks;

    /**
     * 低风险数
     */
    @TableField("low_risks")
    private Integer lowRisks;

    /**
     * 审核结论
     */
    @TableField("conclusion")
    private String conclusion;

    /**
     * 审核报告URL
     */
    @TableField("report_url")
    private String reportUrl;

    /**
     * 匹配条款数
     */
    @TableField("matching_count")
    private Integer matchingCount;

    /**
     * 修改条款数
     */
    @TableField("modified_count")
    private Integer modifiedCount;

    /**
     * 缺失条款数
     */
    @TableField("missing_count")
    private Integer missingCount;

    /**
     * 新增条款数
     */
    @TableField("added_count")
    private Integer addedCount;

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
