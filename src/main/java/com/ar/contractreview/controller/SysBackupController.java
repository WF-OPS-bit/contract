package com.ar.contractreview.controller;


import com.ar.contractreview.entity.SysBackup;
import com.ar.contractreview.result.R;
import com.ar.contractreview.service.SysBackupService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.ar.contractreview.result.ResponseCode.PARAMETER_EXCEPTION;

/**
 * 系统备份控制器
 *
 * <p>基础路径：/api/backups</p>
 *
 * @author System
 * @date 2026-09-08
 */
@RestController
@RequestMapping("/backups")
public class SysBackupController {

    /**
     * 时间格式化器：把 LocalDateTime 格式化成接口文档里的样式（2026-07-06 02:00:00）
     * DateTimeFormatter 是线程安全的，做成常量全局复用
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private SysBackupService backupService;

    /**
     * 16.1 获取备份列表
     * GET /api/backups
     */
    @GetMapping
    public R list(@RequestParam(value = "page", defaultValue = "1") Integer page,
                  @RequestParam(value = "size", defaultValue = "20") Integer size,
                  @RequestParam(value = "backupName", required = false) String backupName,
                  @RequestParam(value = "backupType", required = false) String backupType,
                  @RequestParam(value = "backupStatus", required = false) String backupStatus) {

        Page<SysBackup> p = backupService.queryBackupPage(page, size, backupName, backupType, backupStatus);

        // 按文档 16.1 的字段清单手工组装，实体里的 createdBy / updatedTime 等不在文档里就不往外抛
        List<Map<String, Object>> list = new ArrayList<>();
        for (SysBackup b : p.getRecords()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", b.getId());
            item.put("backupName", b.getBackupName());
            item.put("backupType", b.getBackupType());
            item.put("backupPath", b.getBackupPath());
            item.put("backupSize", b.getBackupSize());
            item.put("backupStatus", b.getBackupStatus());
            // 实体是 LocalDateTime，这里转成文档要求的 "yyyy-MM-dd HH:mm:ss" 字符串；判空防 NPE
            item.put("backupTime", b.getBackupTime() == null ? null : b.getBackupTime().format(DATE_TIME_FORMATTER));
            list.add(item);
        }

        return R.ok()
                .data("list", list)
                .data("total", p.getTotal())
                .data("page", p.getCurrent())
                .data("size", p.getSize());
    }

    /**
     * 16.2 创建备份
     * POST /api/backups
     */
    @PostMapping
    public R create(@RequestBody(required = false) SysBackup backup) {
        // createBackup 会把 backupName / backupType / backupStatus 回填到这个对象上，
        // 所以下面可以直接用它组装响应，不用再查一次库（再查一次可能已经被异步线程改成 RUNNING 了）
        SysBackup request = backup == null ? new SysBackup() : backup;
        Long backupId = backupService.createBackup(request);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", backupId);
        data.put("backupName", request.getBackupName());
        data.put("backupType", request.getBackupType());
        data.put("backupStatus", request.getBackupStatus());
        data.put("message", "备份任务已创建，正在执行中");
        return R.ok().data(data);
    }

    /**
     * 根据ID删除备份
     * DELETE /api/backups/{id}
     */
    @DeleteMapping("/{id}")
    public R delete(@PathVariable("id") Long id) {
        boolean success = backupService.deleteBackupWithFile(id);
        return R.ok().data(success);
    }

    /**
     * 16.4 恢复备份
     * POST /api/backups/{id}/restore
     */
    @PostMapping("/{id}/restore")
    public R restore(@PathVariable("id") Long id) {
        SysBackup backup = backupService.getById(id);
        if (backup == null) {
            return R.fail(PARAMETER_EXCEPTION.getCode(), "备份不存在");
        }
        // 恢复本身是异步的，这里只负责把任务发出去
        backupService.restoreBackup(id);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", backup.getId());
        data.put("backupName", backup.getBackupName());
        // 表里没有 restore_status 列，RESTORING 只是这个响应里的瞬时状态
        data.put("status", "RESTORING");
        data.put("message", "数据恢复任务已启动，预计5分钟内完成");
        return R.ok().data(data);
    }

    /**
     * 获取最新的备份记录
     * GET /api/backups/latest
     */
    @GetMapping("/latest")
    public R getLatest(@RequestParam(value = "limit", defaultValue = "10") int limit) {
        List<SysBackup> backups = backupService.getLatestBackups(limit);
        return R.ok().data(backups);
    }

    /**
     * 获取最后一次成功的备份
     * GET /api/backups/last-success
     */
    @GetMapping("/last-success")
    public R getLastSuccess(@RequestParam("backupType") String backupType) {
        SysBackup backup = backupService.getLastSuccessBackup(backupType);
        return R.ok().data(backup);
    }

    /**
     * 清理过期的备份
     * DELETE /api/backups/clean
     */
    @DeleteMapping("/clean")
    public R cleanExpired(@RequestParam(value = "retentionDays", defaultValue = "30") int retentionDays) {
        int count = backupService.cleanExpiredBackups(retentionDays);
        return R.ok().data("cleanedCount", count);
    }

    /**
     * 获取备份文件大小
     * GET /api/backups/{id}/size
     */
    @GetMapping("/{id}/size")
    public R getFileSize(@PathVariable("id") Long id) {
        long size = backupService.getBackupFileSize(id);
        return R.ok().data("size", size);
    }

    /**
     * 验证备份完整性
     * GET /api/backups/{id}/verify
     */
    @GetMapping("/{id}/verify")
    public R verify(@PathVariable("id") Long id) {
        boolean valid = backupService.verifyBackupIntegrity(id);
        return R.ok().data("valid", valid);
    }

    /**
     * 取消正在运行的备份任务
     * POST /api/backups/{id}/cancel
     */
    @PostMapping("/{id}/cancel")
    public R cancel(@PathVariable("id") Long id) {
        boolean success = backupService.cancelRunningBackup(id);
        if (success) {
            return R.ok().data("message", "已发送取消信号");
        } else {
            // 方式2：使用枚举的 getCode() 方法
            return R.fail(PARAMETER_EXCEPTION.getCode(), "没有找到正在运行的备份任务");
        }
    }
}

