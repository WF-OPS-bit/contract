package com.ar.contractreview.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 审核规则管理 - 响应对象
 * </p>
 * <p>
 * 对应接口：GET /rules（列表）、GET /rules/{id}（详情）
 * </p>
 * <p>
 * ruleConfig 以 JSON 对象返回（存储为字符串，响应时解析为对象）
 * </p>
 *
 * @author wyh
 * @since 2026-09-16
 */
@Data
public class RuleVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 规则ID
     */
    private Long id;

    /**
     * 规则编号
     */
    private String ruleNo;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 规则类型：VALUE_RANGE-数值范围，TEXT_CHECK-文本检查，REQUIRED-必填项检查，REGEX-正则匹配
     */
    private String ruleType;

    /**
     * 适用合同类型，为空则适用于所有类型
     */
    private String contractType;

    /**
     * 条款关键词
     */
    private String clauseKeyword;

    /**
     * 规则配置（JSON对象）
     */
    private Object ruleConfig;

    /**
     * 风险等级
     */
    private String riskLevel;

    /**
     * 优先级（数字越小优先级越高）
     */
    private Integer priority;

    /**
     * 状态：0-禁用，1-启用
     */
    private Byte status;

    /**
     * 规则描述
     */
    private String description;
}
