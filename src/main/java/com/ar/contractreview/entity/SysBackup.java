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
 * 数据备份表
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("sys_backup")
public class SysBackup implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 备份ID（主键自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 备份名称
     * 格式：backup_类型_时间戳，例如：backup_FULL_20260908_143022
     */
    @TableField("backup_name")
    private String backupName;

    /**
     * 备份类型
     * FULL：全量备份
     * INCREMENTAL：增量备份
     */
    @TableField("backup_type")
    private String backupType;

    /**
     * 备份文件路径
     * 存储备份文件的完整物理路径
     */
    @TableField("backup_path")
    private String backupPath;

    /**
     * 备份文件大小（字节）
     */
    @TableField("backup_size")
    private Long backupSize;

    /**
     * 备份状态
     * RUNNING：备份执行中
     * SUCCESS：备份成功
     * FAILED：备份失败
     */
    @TableField("backup_status")
    private String backupStatus;

    /**
     * 备份时间
     */
    @TableField("backup_time")
    private LocalDateTime backupTime;

    /**
     * 备份文件数量（备用字段）
     */
    @TableField("file_count")
    private Integer fileCount;

    /**
     * 备份描述
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
     * 更新时间
     */
    @TableField("updated_time")
    private LocalDateTime updatedTime;

    /**
     * 删除标记：0-正常，-1-已删除
     * <p>sys_backup 表里有这一列，全局配置 logic-delete-field 也是 deleted，
     * 实体少了它的话 MyBatis-Plus 不会走逻辑删除，removeById() 会变成物理删除。</p>
     */
    @TableLogic
    @TableField("deleted")
    private Byte deleted;

    // ==================== 状态常量 ====================

    /**
     * 备份状态常量
     */
    public static final class Status {
        /** 待备份（记录已建，异步任务还没跑起来） */
        public static final String PENDING = "PENDING";
        /** 备份执行中 */
        public static final String RUNNING = "RUNNING";
        /** 备份成功 */
        public static final String SUCCESS = "SUCCESS";
        /** 备份失败 */
        public static final String FAILED = "FAILED";
    }

    /**
     * 备份类型常量
     * <p>与 sys_backup.backup_type 的列注释保持一致：只有全量和增量两种。</p>
     */
    public static final class Type {
        /** 全量备份 */
        public static final String FULL = "FULL";
        /** 增量备份 */
        public static final String INCREMENTAL = "INCREMENTAL";
    }
}
