package com.ar.contractreview.security.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 审核规则管理 - 新增 / 更新规则请求对象
 * </p>
 * <p>
 * 对应接口：POST /rules（新增）、PUT /rules/{id}（更新）
 * </p>
 * <p>
 * 必填：ruleName / ruleNo / ruleType / clauseKeyword / riskLevel
 * </p>
 *
 * @author wyh
 * @since 2026-09-16
 */
@Data
public class RuleSaveRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 规则名称（必填）
     */
    private String ruleName;

    /**
     * 规则编号（必填，唯一）
     */
    private String ruleNo;

    /**
     * 规则类型：VALUE_RANGE-数值范围，TEXT_CHECK-文本检查，REQUIRED-必填项检查，REGEX-正则匹配（必填）
     */
    private String ruleType;

    /**
     * 适用合同类型，为空则适用于所有类型
     */
    private String contractType;

    /**
     * 条款关键词（必填）
     */
    private String clauseKeyword;

    /**
     * 规则配置（JSON对象，如 {"min":5,"max":30,"unit":"%"}）
     */
    private Object ruleConfig;

    /**
     * 风险等级（必填）
     */
    private String riskLevel;

    /**
     * 优先级（数字越小优先级越高）
     */
    private Integer priority;

    /**
     * 规则描述
     */
    private String description;
}
