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
 * 风险条款表
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("contract_risk_clause")
public class ContractRiskClause implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 风险条款ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 合同ID
     */
    @TableField("contract_id")
    private Long contractId;

    /**
     * 审核结果ID
     */
    @TableField("review_result_id")
    private Long reviewResultId;

    /**
     * 条款编号
     */
    @TableField("clause_no")
    private String clauseNo;

    /**
     * 条款标题
     */
    @TableField("clause_title")
    private String clauseTitle;

    /**
     * 条款内容
     */
    @TableField("clause_content")
    private String clauseContent;

    /**
     * 页码
     */
    @TableField("page")
    private Integer page;

    /**
     * 风险等级
     */
    @TableField("risk_level")
    private String riskLevel;

    /**
     * 风险类型
     */
    @TableField("risk_type")
    private String riskType;

    /**
     * 风险描述
     */
    @TableField("risk_description")
    private String riskDescription;

    /**
     * 修改建议
     */
    @TableField("suggestion")
    private String suggestion;

    /**
     * 法律依据
     */
    @TableField("legal_basis")
    private String legalBasis;

    /**
     * 处理状态：PENDING-待处理，ACCEPTED-已采纳，REJECTED-已拒绝
     */
    @TableField("status")
    private String status;

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
