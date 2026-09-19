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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 合同表
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("contract_contract")
public class ContractContract implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 合同ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 合同编号
     */
    @TableField("contract_no")
    private String contractNo;

    /**
     * 合同名称
     */
    @TableField("contract_name")
    private String contractName;

    /**
     * 合同类型：采购合同、服务合同、租赁合同、劳动合同、销售合同等
     */
    @TableField("contract_type")
    private String contractType;

    /**
     * 甲方
     */
    @TableField("party_a")
    private String partyA;

    /**
     * 乙方
     */
    @TableField("party_b")
    private String partyB;

    /**
     * 合同金额
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 签订日期
     */
    @TableField("sign_date")
    private LocalDate signDate;

    /**
     * 开始日期
     */
    @TableField("start_date")
    private LocalDate startDate;

    /**
     * 结束日期
     */
    @TableField("end_date")
    private LocalDate endDate;

    /**
     * 合同文件URL
     */
    @TableField("file_url")
    private String fileUrl;

    /**
     * 文件类型：pdf、doc、docx等
     */
    @TableField("file_type")
    private String fileType;

    /**
     * 文件大小（字节）
     */
    @TableField("file_size")
    private Long fileSize;

    /**
     * 状态：UPLOADED-已上传，AI_REVIEWING-AI审核中，REVIEWING-审核中，APPROVED-已通过，REJECTED-已驳回，ARCHIVED-已归档
     */
    @TableField("status")
    private String status;

    /**
     * 风险等级：NONE-无风险，LOW-低风险，MEDIUM-中风险，HIGH-高风险
     */
    @TableField("risk_level")
    private String riskLevel;

    /**
     * 风险评分（0-100）
     */
    @TableField("risk_score")
    private Integer riskScore;

    /**
     * AI审核完成时间
     */
    @TableField("ai_review_time")
    private LocalDateTime aiReviewTime;

    /**
     * 审核人ID
     */
    @TableField("reviewer_id")
    private Long reviewerId;

    /**
     * 审核人姓名
     */
    @TableField("reviewer_name")
    private String reviewerName;

    /**
     * 审核完成时间
     */
    @TableField("review_time")
    private LocalDateTime reviewTime;

    /**
     * 审核意见
     */
    @TableField("review_comment")
    private String reviewComment;

    /**
     * 上传人ID
     */
    @TableField("upload_user_id")
    private Long uploadUserId;

    /**
     * 上传人姓名
     */
    @TableField("upload_user_name")
    private String uploadUserName;

    /**
     * 上传时间
     */
    @TableField("upload_time")
    private LocalDateTime uploadTime;

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

    /**
     * 删除标记：0-未删除，1-已删除
     */
    @TableLogic
    @TableField("deleted")
    private Byte deleted;
}
