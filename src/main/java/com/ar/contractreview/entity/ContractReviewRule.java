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
 * 审核规则表
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("contract_review_rule")
public class ContractReviewRule implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 规则ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 规则编号
     */
    @TableField("rule_no")
    private String ruleNo;

    /**
     * 规则名称
     */
    @TableField("rule_name")
    private String ruleName;

    /**
     * 规则类型：VALUE_RANGE-数值范围，TEXT_CHECK-文本检查，REQUIRED-必填项检查，REGEX-正则匹配
     */
    @TableField("rule_type")
    private String ruleType;

    /**
     * 适用合同类型，为空则适用于所有类型
     */
    @TableField("contract_type")
    private String contractType;

    /**
     * 条款关键词
     */
    @TableField("clause_keyword")
    private String clauseKeyword;

    /**
     * 规则配置（JSON格式）
     */
    @TableField("rule_config")
    private String ruleConfig;

    /**
     * 风险等级
     */
    @TableField("risk_level")
    private String riskLevel;

    /**
     * 优先级（数字越小优先级越高）
     */
    @TableField("priority")
    private Integer priority;

    /**
     * 状态：0-禁用，1-启用
     */
    @TableField("status")
    private Byte status;

    /**
     * 规则描述
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

    /**
     * 删除标记：0-未删除，1-已删除
     */
    @TableLogic
    @TableField("deleted")
    private Byte deleted;
}
