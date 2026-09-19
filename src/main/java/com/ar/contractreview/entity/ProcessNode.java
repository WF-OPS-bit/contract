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
 * 流程节点表
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("process_node")
public class ProcessNode implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 流程节点ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 流程配置ID
     */
    @TableField("process_id")
    private Long processId;

    /**
     * 节点名称
     */
    @TableField("node_name")
    private String nodeName;

    /**
     * 节点类型：START-开始节点，REVIEW-审核节点，APPROVE-审批节点，END-结束节点
     */
    @TableField("node_type")
    private String nodeType;

    /**
     * 办理人类型：ROLE-按角色，USER-按用户，DEPARTMENT-按部门
     */
    @TableField("assignee_type")
    private String assigneeType;

    /**
     * 办理人ID（角色ID/用户ID/部门ID）
     */
    @TableField("assignee_id")
    private Long assigneeId;

    /**
     * 办理人名称
     */
    @TableField("assignee_name")
    private String assigneeName;

    /**
     * 排序号
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 超时天数（0表示不限制）
     */
    @TableField("timeout_days")
    private Integer timeoutDays;

    /**
     * 是否必选：0-否，1-是
     */
    @TableField("is_required")
    private Byte isRequired;

    /**
     * 节点描述
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
