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
     * 备份ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 备份名称
     */
    @TableField("backup_name")
    private String backupName;

    /**
     * 备份类型：FULL-全量备份，INCREMENTAL-增量备份
     */
    @TableField("backup_type")
    private String backupType;

    /**
     * 备份文件路径
     */
    @TableField("backup_path")
    private String backupPath;

    /**
     * 备份文件大小（字节）
     */
    @TableField("backup_size")
    private Long backupSize;

    /**
     * 备份状态：PENDING-待备份，RUNNING-备份中，SUCCESS-成功，FAILED-失败
     */
    @TableField("backup_status")
    private String backupStatus;

    /**
     * 备份时间
     */
    @TableField("backup_time")
    private LocalDateTime backupTime;

    /**
     * 备份文件数量
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
}
