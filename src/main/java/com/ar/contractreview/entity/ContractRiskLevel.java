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
 * 风险等级配置表
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("contract_risk_level")
public class ContractRiskLevel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 风险等级ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 风险代码：NONE-无风险，LOW-低风险，MEDIUM-中风险，HIGH-高风险，CRITICAL-严重风险
     */
    @TableField("risk_code")
    private String riskCode;

    /**
     * 风险名称
     */
    @TableField("risk_name")
    private String riskName;

    /**
     * 风险颜色（十六进制）
     */
    @TableField("risk_color")
    private String riskColor;

    /**
     * 最低分数
     */
    @TableField("score_min")
    private Integer scoreMin;

    /**
     * 最高分数
     */
    @TableField("score_max")
    private Integer scoreMax;

    /**
     * 风险描述
     */
    @TableField("description")
    private String description;

    /**
     * 处理建议
     */
    @TableField("handling_suggestion")
    private String handlingSuggestion;

    /**
     * 状态：0-禁用，1-启用
     */
    @TableField("status")
    private Byte status;

    /**
     * 排序号
     */
    @TableField("sort_order")
    private Integer sortOrder;

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
