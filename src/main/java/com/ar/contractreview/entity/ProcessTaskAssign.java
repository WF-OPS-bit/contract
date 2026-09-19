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
 * 任务分配表
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("process_task_assign")
public class ProcessTaskAssign implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务分配ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 任务ID
     */
    @TableField("task_id")
    private Long taskId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 用户姓名
     */
    @TableField("user_name")
    private String userName;

    /**
     * 分配类型：PRIMARY-主办理人，SECONDARY-协办人
     */
    @TableField("assign_type")
    private String assignType;

    /**
     * 状态：ACTIVE-有效，COMPLETED-已完成，REVOKED-已撤销
     */
    @TableField("status")
    private String status;

    /**
     * 创建时间
     */
    @TableField("created_time")
    private LocalDateTime createdTime;
}
