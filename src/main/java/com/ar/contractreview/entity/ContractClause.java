package com.ar.contractreview.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 条款库表
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("contract_clause")
public class ContractClause implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 条款ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 条款编码
     */
    @TableField("clause_code")
    private String clauseCode;

    /**
     * 条款名称
     */
    @TableField("clause_name")
    private String clauseName;

    /**
     * 条款类别：COMMON-通用条款，SPECIAL-特殊条款，RISK-风险条款
     */
    @TableField("clause_category")
    private String clauseCategory;

    /**
     * 适用合同类型，为空则适用于所有类型
     */
    @TableField("contract_type")
    private String contractType;

    /**
     * 条款内容
     */
    @TableField("content")
    private String content;

    /**
     * 标准条款内容
     */
    @TableField("standard_content")
    private String standardContent;

    /**
     * 风险等级
     */
    @TableField("risk_level")
    private String riskLevel;

    /**
     * 排序号
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 状态：0-禁用，1-启用
     */
    @TableField("status")
    private Byte status;

    /**
     * 条款描述
     */
    @TableField("description")
    private String description;

    /**
     * 法律依据
     */
    @TableField("legal_basis")
    private String legalBasis;

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

    /**
     * 删除标记：0-未删除，1-已删除
     */
    @TableLogic
    @TableField("deleted")
    private Byte deleted;
}
