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
 * 合同批注表
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("contract_comment")
public class ContractComment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 批注ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 合同ID
     */
    @TableField("contract_id")
    private Long contractId;

    /**
     * 批注人ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 批注人姓名
     */
    @TableField("user_name")
    private String userName;

    /**
     * 批注人角色
     */
    @TableField("user_role")
    private String userRole;

    /**
     * 批注类型：TEXT-文本批注，REVIEW-审核意见，SUGGESTION-修改建议
     */
    @TableField("comment_type")
    private String commentType;

    /**
     * 批注内容
     */
    @TableField("content")
    private String content;

    /**
     * 页码
     */
    @TableField("page")
    private Integer page;

    /**
     * 位置坐标（JSON格式）
     */
    @TableField("position")
    private String position;

    /**
     * 状态：ACTIVE-有效，RESOLVED-已解决，DELETED-已删除
     */
    @TableField("status")
    private String status;

    /**
     * 解决时间
     */
    @TableField("resolve_time")
    private LocalDateTime resolveTime;

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
