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
 * 流程任务表
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("process_task")
public class ProcessTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 任务编码
     */
    @TableField("task_code")
    private String taskCode;

    /**
     * 流程实例ID
     */
    @TableField("instance_id")
    private Long instanceId;

    /**
     * 流程配置ID
     */
    @TableField("process_id")
    private Long processId;

    /**
     * 节点ID
     */
    @TableField("node_id")
    private Long nodeId;

    /**
     * 节点名称
     */
    @TableField("node_name")
    private String nodeName;

    /**
     * 节点类型
     */
    @TableField("node_type")
    private String nodeType;

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
     * 办理人ID
     */
    @TableField("assignee_id")
    private Long assigneeId;

    /**
     * 办理人姓名
     */
    @TableField("assignee_name")
    private String assigneeName;

    /**
     * 状态：PENDING-待办理，PROCESSING-办理中，COMPLETED-已完成
     */
    @TableField("status")
    private String status;

    /**
     * 优先级：LOW-低，NORMAL-中，HIGH-高，URGENT-紧急
     */
    @TableField("priority")
    private String priority;

    /**
     * 任务标题
     */
    @TableField("title")
    private String title;

    /**
     * 任务描述
     */
    @TableField("description")
    private String description;

    /**
     * 截止时间
     */
    @TableField("deadline")
    private LocalDateTime deadline;

    /**
     * 开始时间
     */
    @TableField("start_time")
    private LocalDateTime startTime;

    /**
     * 完成时间
     */
    @TableField("complete_time")
    private LocalDateTime completeTime;

    /**
     * 处理结果：APPROVE-通过，REJECT-拒绝，BACK-退回
     */
    @TableField("result")
    private String result;

    /**
     * 处理意见
     */
    @TableField("comment")
    private String comment;

    /**
     * 创建人ID
     */
    @TableField("creator_id")
    private Long creatorId;

    /**
     * 创建人姓名
     */
    @TableField("creator_name")
    private String creatorName;

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
