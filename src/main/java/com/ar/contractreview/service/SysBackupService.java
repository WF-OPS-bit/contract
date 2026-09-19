package com.ar.contractreview.service;



/**
 * <p>
 * 数据备份表 服务类
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */



import com.ar.contractreview.entity.SysBackup;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 系统备份服务接口
 *
 * <p>定义备份相关的所有业务操作</p>
 *
 * @author System
 * @date 2026-09-08
 */
public interface SysBackupService extends IService<SysBackup> {

    // ==================== 查询方法 ====================

    /**
     * 分页查询备份列表
     *
     * @param page         当前页码
     * @param size         每页大小
     * @param backupName   备份名称（模糊查询）
     * @param backupType   备份类型
     * @param backupStatus 备份状态
     * @return 分页对象
     */
    Page<SysBackup> queryBackupPage(Integer page, Integer size, String backupName,
                                    String backupType, String backupStatus);

    /**
     * 获取最新的备份记录
     *
     * @param limit 限制数量
     * @return 备份列表
     */
    List<SysBackup> getLatestBackups(int limit);

    /**
     * 根据备份类型获取最后一次成功备份
     *
     * @param backupType 备份类型
     * @return 备份信息
     */
    SysBackup getLastSuccessBackup(String backupType);

    /**
     * 获取备份的物理文件大小
     *
     * @param backupId 备份ID
     * @return 文件大小（字节）
     */
    long getBackupFileSize(Long backupId);

    /**
     * 验证备份文件完整性
     *
     * @param backupId 备份ID
     * @return 是否完整
     */
    boolean verifyBackupIntegrity(Long backupId);

    // ==================== 备份操作方法 ====================

    /**
     * 创建并执行系统备份（异步）
     *
     * @param backup 备份信息
     * @return 备份ID
     */
    Long createBackup(SysBackup backup);

    /**
     * 执行实际的备份操作
     *
     * @param backupId 备份ID
     */
    void executeBackup(Long backupId);

    /**
     * 恢复指定的备份
     *
     * @param backupId 备份ID
     */
    void restoreBackup(Long backupId);

    /**
     * 删除备份（同时删除物理文件）
     *
     * @param backupId 备份ID
     * @return 是否删除成功
     */
    boolean deleteBackupWithFile(Long backupId);

    /**
     * 更新备份状态
     *
     * @param backupId     备份ID
     * @param status       新状态
     * @param errorMessage 失败原因（sys_backup 表没有 error_message 列，会写进 description）
     */
    void updateBackupStatus(Long backupId, String status, String errorMessage);

    /**
     * 清理过期的备份文件
     *
     * @param retentionDays 保留天数
     * @return 清理数量
     */
    int cleanExpiredBackups(int retentionDays);

    /**
     * 取消正在运行的备份任务
     *
     * @param backupId 备份ID
     * @return 是否取消成功
     */
    boolean cancelRunningBackup(Long backupId);
}
