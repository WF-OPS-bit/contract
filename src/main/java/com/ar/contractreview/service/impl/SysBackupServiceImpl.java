package com.ar.contractreview.service.impl;

import com.ar.contractreview.entity.SysBackup;
import com.ar.contractreview.exception.BusinessException;
import com.ar.contractreview.mapper.SysBackupMapper;
import com.ar.contractreview.result.ResponseCode;
import com.ar.contractreview.service.SysBackupService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * 系统备份服务实现类
 *
 * <p>数据库连接信息不再写死在这里，统一从 application.yml 的 spring.datasource.* 读取，
 * 避免以后改库地址要改两处、也避免密码硬编码在代码里。</p>
 *
 * @author System
 * @date 2026-09-08
 */
@Slf4j
@Service
public class SysBackupServiceImpl extends ServiceImpl<SysBackupMapper, SysBackup>
        implements SysBackupService {

    /** 备份文件名里的时间戳样式：20260908_143022 */
    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    /** 从 spring.datasource.url 里解析 主机 / 端口 / 库名 */
    private static final Pattern JDBC_URL_PATTERN =
            Pattern.compile("jdbc:mysql://([^:/]+)(?::(\\d+))?/([^?]+)");
    /** description 列的长度上限（VARCHAR(500)），失败原因要截断后再写 */
    private static final int DESCRIPTION_MAX_LENGTH = 500;

    /**
     * 备份文件存放目录。
     * <p>默认是运行目录下的 backups；Windows 上原来的 /data/backups 会落到 C:\data\backups，
     * 一般没有权限创建，所以改成相对路径并允许用 backup.dir 覆盖。</p>
     */
    @Value("${backup.dir:./backups}")
    private String backupDir;

    /**
     * mysqldump 可执行文件。
     * <p>默认按 PATH 查找；如果本机装了 MySQL 但没配 PATH（Windows 上很常见），
     * 用 backup.mysqldump-path 指定绝对路径，例如
     * C:\Program Files\MySQL\MySQL Server 8.4\bin\mysqldump.exe</p>
     */
    @Value("${backup.mysqldump-path:mysqldump}")
    private String mysqldumpPath;

    /** mysql 可执行文件（恢复时回放 SQL 用），规则同 mysqldump */
    @Value("${backup.mysql-path:mysql}")
    private String mysqlPath;

    // ==================== 数据库连接信息（从 spring.datasource 读取） ====================

    /** 形如 jdbc:mysql://<DB_HOST>:3306/contract_review?... */
    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    /** 数据库用户名 */
    @Value("${spring.datasource.username}")
    private String dbUsername;

    /** 数据库密码 */
    @Value("${spring.datasource.password:}")
    private String dbPassword;

    /**
     * 自己的代理对象。
     * <p>createBackup() 里要调用带 @Async 的 executeBackup()，如果直接写 this.executeBackup()，
     * 调用不会经过 Spring 代理，@Async 就是个摆设——备份会退回成同步执行，把请求线程占住。</p>
     */
    @Autowired
    @Lazy
    private SysBackupService self;

    /**
     * 记录正在运行的备份任务
     */
    private final ConcurrentMap<Long, Thread> runningBackupTasks = new ConcurrentHashMap<>();

    // ==================== 查询方法 ====================

    @Override
    public Page<SysBackup> queryBackupPage(Integer page, Integer size, String backupName,
                                           String backupType, String backupStatus) {
        LambdaQueryWrapper<SysBackup> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(backupName), SysBackup::getBackupName, backupName)
                .eq(StringUtils.hasText(backupType), SysBackup::getBackupType, backupType)
                .eq(StringUtils.hasText(backupStatus), SysBackup::getBackupStatus, backupStatus)
                .orderByDesc(SysBackup::getBackupTime);
        return this.page(new Page<>(page, size), wrapper);
    }

    @Override
    public List<SysBackup> getLatestBackups(int limit) {
        LambdaQueryWrapper<SysBackup> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SysBackup::getBackupTime)
                .last("LIMIT " + limit);
        return this.list(wrapper);
    }

    @Override
    public SysBackup getLastSuccessBackup(String backupType) {
        LambdaQueryWrapper<SysBackup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysBackup::getBackupType, backupType)
                .eq(SysBackup::getBackupStatus, "SUCCESS")
                .orderByDesc(SysBackup::getBackupTime)
                .last("LIMIT 1");
        return this.getOne(wrapper);
    }

    @Override
    public long getBackupFileSize(Long backupId) {
        SysBackup backup = this.getById(backupId);
        if (backup == null || !StringUtils.hasText(backup.getBackupPath())) {
            return 0;
        }
        try {
            Path path = Paths.get(backup.getBackupPath());
            if (Files.exists(path)) {
                return Files.size(path);
            }
        } catch (IOException e) {
            log.warn("获取备份文件大小失败: backupId={}", backupId, e);
        }
        return 0;
    }

    @Override
    public boolean verifyBackupIntegrity(Long backupId) {
        SysBackup backup = this.getById(backupId);
        if (backup == null) {
            return false;
        }
        String backupPath = backup.getBackupPath();
        if (!StringUtils.hasText(backupPath)) {
            return false;
        }
        try {
            Path path = Paths.get(backupPath);
            if (!Files.exists(path)) {
                return false;
            }
            if (backupPath.endsWith(".zip")) {
                try (ZipInputStream zis = new ZipInputStream(new FileInputStream(backupPath))) {
                    ZipEntry entry = zis.getNextEntry();
                    return entry != null;
                }
            }
            return Files.isReadable(path);
        } catch (Exception e) {
            log.warn("验证备份完整性失败: backupId={}", backupId, e);
            return false;
        }
    }

    // ==================== 备份创建和执行 ====================

    @Override
    public Long createBackup(SysBackup backup) {
        SysBackup entity = backup == null ? new SysBackup() : backup;

        // 1. 备份类型只能是 FULL / INCREMENTAL（与 sys_backup.backup_type 的列注释一致）
        String backupType = StringUtils.hasText(entity.getBackupType())
                ? entity.getBackupType().trim().toUpperCase()
                : SysBackup.Type.FULL;
        if (!SysBackup.Type.FULL.equals(backupType) && !SysBackup.Type.INCREMENTAL.equals(backupType)) {
            throw new BusinessException(ResponseCode.PARAMETER_EXCEPTION.getCode(),
                    "备份类型只能是 FULL 或 INCREMENTAL，收到：" + entity.getBackupType());
        }

        // 2. 备份名称：文档 16.2 里 backupName 是可选的，传了就用传的，没传按 backup_类型_时间戳 生成
        if (!StringUtils.hasText(entity.getBackupName())) {
            entity.setBackupName(String.format("backup_%s_%s", backupType,
                    LocalDateTime.now().format(TIMESTAMP_FORMATTER)));
        }
        entity.setBackupType(backupType);

        // 3. 先落一条 PENDING 记录（文档 16.2 响应里 backupStatus 就是 PENDING），
        //    真正的 mysqldump 交给异步线程去做。
        //    这里不能加 @Transactional：异步线程是另一个事务/连接，读不到未提交的记录，
        //    会直接报「备份记录不存在」，所以必须让这条记录先提交。
        entity.setBackupStatus(SysBackup.Status.PENDING);
        entity.setBackupTime(LocalDateTime.now());
        this.save(entity);

        // 4. 通过代理调用，@Async 才会生效
        self.executeBackup(entity.getId());

        return entity.getId();
    }

    @Async
    @Override
    public void executeBackup(Long backupId) {
        SysBackup backup = this.getById(backupId);
        if (backup == null) {
            log.error("备份记录不存在: {}", backupId);
            return;
        }

        Thread currentThread = Thread.currentThread();
        runningBackupTasks.put(backupId, currentThread);

        try {
            log.info("开始执行备份: backupId={}, backupName={}, type={}",
                    backupId, backup.getBackupName(), backup.getBackupType());
            log.info("数据库: {}", datasourceUrl);

            // 先把状态推到 RUNNING 落库，列表接口轮询时能看到「备份中」
            backup.setBackupStatus(SysBackup.Status.RUNNING);
            this.updateById(backup);

            Path backupDirPath = Paths.get(backupDir);
            if (!Files.exists(backupDirPath)) {
                Files.createDirectories(backupDirPath);
                log.info("创建备份目录: {}", backupDirPath.toAbsolutePath());
            }

            String filePath;
            if (SysBackup.Type.INCREMENTAL.equalsIgnoreCase(backup.getBackupType())) {
                filePath = executeIncrementalBackup(backup);
            } else {
                filePath = executeDatabaseBackup(backup);
            }

            backup.setBackupPath(filePath);
            backup.setBackupStatus(SysBackup.Status.SUCCESS);

            File backupFile = new File(filePath);
            if (backupFile.exists()) {
                backup.setBackupSize(backupFile.length());
            }

            this.updateById(backup);
            log.info("备份完成: backupId={}, filePath={}, size={} bytes",
                    backupId, filePath, backupFile.exists() ? backupFile.length() : 0);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("备份任务被取消: backupId={}", backupId);
            backup.setBackupStatus(SysBackup.Status.FAILED);
            backup.setDescription(appendFailureReason(backup.getDescription(), "备份任务被取消"));
            this.updateById(backup);
        } catch (Exception e) {
            log.error("备份失败: backupId={}", backupId, e);
            backup.setBackupStatus(SysBackup.Status.FAILED);
            backup.setDescription(appendFailureReason(backup.getDescription(), e.getMessage()));
            this.updateById(backup);
        } finally {
            runningBackupTasks.remove(backupId);
        }
    }

    /**
     * 执行数据库全量备份
     * <p>流程：mysqldump 导出 .sql -> 压缩成 .zip -> 删掉中间的 .sql，最终只留一个 zip，
     * 恢复时解压回放即可。</p>
     */
    private String executeDatabaseBackup(SysBackup backup) throws Exception {
        DbInfo db = dbInfo();
        String fileName = sanitizeFileName(backup.getBackupName())
                + "_" + LocalDateTime.now().format(TIMESTAMP_FORMATTER) + ".sql";
        Path sqlFile = Paths.get(backupDir, fileName);

        try {
            runDump(db, sqlFile);
            if (!Files.exists(sqlFile) || Files.size(sqlFile) == 0) {
                throw new BusinessException(ResponseCode.BACKUP_OPERATION_FAILED.getCode(),
                        "mysqldump 没有产出任何内容，请检查数据库连接和 mysqldump 路径");
            }
            return compressFile(sqlFile.toString());
        } finally {
            // 成功失败都清掉中间文件，避免备份目录里堆一堆 .sql
            Files.deleteIfExists(sqlFile);
        }
    }

    /**
     * 执行一次 mysqldump，标准输出写到 target
     *
     * <p>这里有两个坑：</p>
     * <ul>
     *   <li>不能同时用 redirectOutput(文件) 和 redirectErrorStream(true)：那样 mysqldump 的报错
     *       会被一起写进 .sql 备份文件里，文件本身就是坏的，还会被当成备份成功存下来。
     *       所以把 stderr 单独收进临时文件，只在退出码非 0 时取出来看。</li>
     *   <li>密码要写成 -p密码（紧贴），写成 -p 空格接密码会被 mysqldump 当成库名。</li>
     * </ul>
     */
    private void runDump(DbInfo db, Path target) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add(mysqldumpPath);
        command.add("-h");
        command.add(db.host());
        command.add("-P");
        command.add(db.port());
        command.add("-u");
        command.add(dbUsername);
        if (StringUtils.hasText(dbPassword)) {
            command.add("-p" + dbPassword);
        }
        command.add("--single-transaction");
        command.add("--routines");
        command.add("--triggers");
        command.add(db.database());

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectOutput(target.toFile());

        Path errorFile = Files.createTempFile("mysqldump-", ".err");
        processBuilder.redirectError(errorFile.toFile());

        try {
            Process process;
            try {
                process = processBuilder.start();
            } catch (IOException e) {
                throw new BusinessException(ResponseCode.BACKUP_OPERATION_FAILED.getCode(),
                        "无法启动 " + mysqldumpPath + "，请把 backup.mysqldump-path 配成 mysqldump 的绝对路径"
                                + "（例如 C:\\Program Files\\MySQL\\MySQL Server 8.4\\bin\\mysqldump.exe）");
            }
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new BusinessException(ResponseCode.BACKUP_OPERATION_FAILED.getCode(),
                        "mysqldump 执行失败（退出码 " + exitCode + "）：" + readErrorBriefly(errorFile));
            }
        } finally {
            Files.deleteIfExists(errorFile);
        }
    }

    /**
     * 执行增量备份
     *
     * <p>原来的实现是 TODO 占位：只 Files.createFile() 建了个 0 字节空文件，却把状态标成 SUCCESS，
     * 点「恢复」必然报「ZIP文件为空」。这里改成明确失败，不再产生假的成功记录。</p>
     */
    private String executeIncrementalBackup(SysBackup backup) {
        throw new BusinessException(ResponseCode.BACKUP_OPERATION_FAILED.getCode(),
                "增量备份暂未实现，目前只支持全量备份（backupType=FULL）");
    }

    /**
     * 压缩文件
     */
    private String compressFile(String sourcePath) throws IOException {
        String zipPath = sourcePath + ".zip";
        try (FileOutputStream fos = new FileOutputStream(zipPath);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            Path source = Paths.get(sourcePath);
            String fileName = source.getFileName().toString();

            try (FileInputStream fis = new FileInputStream(sourcePath)) {
                ZipEntry zipEntry = new ZipEntry(fileName);
                zos.putNextEntry(zipEntry);

                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }
                zos.closeEntry();
            }
        }
        return zipPath;
    }

    /**
     * 解压 ZIP 文件
     */
    private String extractZipFile(String zipPath) throws IOException {
        String destPath = zipPath.replace(".zip", ".sql");
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipPath))) {
            ZipEntry entry = zis.getNextEntry();
            if (entry == null) {
                throw new IOException("ZIP文件为空");
            }
            try (FileOutputStream fos = new FileOutputStream(destPath)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }
            }
            zis.closeEntry();
        }
        return destPath;
    }

    // ==================== 恢复方法 ====================

    @Async
    @Override
    public void restoreBackup(Long backupId) {
        SysBackup backup = this.getById(backupId);
        if (backup == null) {
            log.error("备份不存在: {}", backupId);
            return;
        }

        try {
            log.info("开始恢复备份: backupId={}", backupId);

            String backupPath = backup.getBackupPath();
            if (!StringUtils.hasText(backupPath) || !Files.exists(Paths.get(backupPath))) {
                throw new BusinessException(ResponseCode.BACKUP_FILE_NOT_EXIST);
            }

            restoreDatabaseBackup(backupPath);

            log.info("备份恢复完成: backupId={}", backupId);

        } catch (Exception e) {
            // 这个方法是 @Async 的，请求早就返回「恢复任务已启动」了，异常只能落到日志里
            log.error("备份恢复失败: backupId={}", backupId, e);
        }
    }

    /**
     * 恢复数据库备份：解压 zip 拿到 .sql，再用 mysql 命令回放
     */
    private void restoreDatabaseBackup(String backupPath) throws Exception {
        DbInfo db = dbInfo();
        String sqlPath = extractZipFile(backupPath);
        Path errorFile = Files.createTempFile("mysql-", ".err");

        try {
            List<String> command = new ArrayList<>();
            command.add(mysqlPath);
            command.add("-h");
            command.add(db.host());
            command.add("-P");
            command.add(db.port());
            command.add("-u");
            command.add(dbUsername);
            if (StringUtils.hasText(dbPassword)) {
                command.add("-p" + dbPassword);
            }
            command.add(db.database());

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectInput(new File(sqlPath));
            processBuilder.redirectError(errorFile.toFile());

            Process process;
            try {
                process = processBuilder.start();
            } catch (IOException e) {
                throw new BusinessException(ResponseCode.BACKUP_OPERATION_FAILED.getCode(),
                        "无法启动 " + mysqlPath + "，请把 backup.mysql-path 配成 mysql 的绝对路径");
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new BusinessException(ResponseCode.BACKUP_OPERATION_FAILED.getCode(),
                        "数据库恢复失败（退出码 " + exitCode + "）：" + readErrorBriefly(errorFile));
            }
            log.info("数据库恢复完成");
        } finally {
            Files.deleteIfExists(errorFile);
            Files.deleteIfExists(Paths.get(sqlPath));
        }
    }

    // ==================== 删除和清理 ====================

    @Override
    @Transactional
    public boolean deleteBackupWithFile(Long backupId) {
        SysBackup backup = this.getById(backupId);
        if (backup == null) {
            return false;
        }

        if ("RUNNING".equals(backup.getBackupStatus())) {
            throw new IllegalStateException("备份正在运行中，无法删除");
        }

        String backupPath = backup.getBackupPath();
        if (StringUtils.hasText(backupPath)) {
            try {
                Path path = Paths.get(backupPath);
                if (Files.exists(path)) {
                    Files.delete(path);
                    log.info("备份文件已删除: {}", backupPath);
                }
            } catch (IOException e) {
                log.warn("删除备份文件失败: {}", backupPath, e);
            }
        }

        return this.removeById(backupId);
    }

    @Override
    public void updateBackupStatus(Long backupId, String status, String errorMessage) {
        SysBackup backup = new SysBackup();
        backup.setId(backupId);
        backup.setBackupStatus(status);
        // sys_backup 表里没有 error_message 列（那是三张日志表的字段），失败原因统一写进 description
        if (SysBackup.Status.FAILED.equals(status) && StringUtils.hasText(errorMessage)) {
            backup.setDescription(truncate(errorMessage, DESCRIPTION_MAX_LENGTH));
        }
        this.updateById(backup);
    }

    @Override
    @Transactional
    public int cleanExpiredBackups(int retentionDays) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(retentionDays);

        LambdaQueryWrapper<SysBackup> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(SysBackup::getBackupTime, threshold)
                .ne(SysBackup::getBackupStatus, "RUNNING");

        List<SysBackup> expiredBackups = this.list(wrapper);
        int count = 0;

        for (SysBackup backup : expiredBackups) {
            try {
                this.deleteBackupWithFile(backup.getId());
                count++;
            } catch (Exception e) {
                log.error("清理过期备份失败: backupId={}", backup.getId(), e);
            }
        }

        log.info("清理过期备份完成，共清理 {} 个备份", count);
        return count;
    }

    @Override
    public boolean cancelRunningBackup(Long backupId) {
        Thread taskThread = runningBackupTasks.get(backupId);
        if (taskThread != null && taskThread.isAlive()) {
            taskThread.interrupt();
            log.info("已发送取消信号给备份任务: backupId={}", backupId);
            return true;
        }
        log.warn("没有找到正在运行的备份任务: backupId={}", backupId);
        return false;
    }

    // ==================== 内部工具 ====================

    /**
     * 从 spring.datasource.url 解析出 mysqldump / mysql 命令要用的主机、端口、库名
     *
     * @return 解析结果
     */
    private DbInfo dbInfo() {
        Matcher matcher = JDBC_URL_PATTERN.matcher(datasourceUrl == null ? "" : datasourceUrl);
        if (!matcher.find()) {
            throw new BusinessException(ResponseCode.BACKUP_OPERATION_FAILED.getCode(),
                    "解析不了 spring.datasource.url：" + datasourceUrl);
        }
        return new DbInfo(matcher.group(1),
                matcher.group(2) == null ? "3306" : matcher.group(2),
                matcher.group(3));
    }

    /**
     * 备份名称会拼进文件名，先把文件名里的非法字符（含空格）换成下划线，
     * 否则「2026-07-06 手动全量备份」这种名字在 Windows 上很容易出问题
     */
    private String sanitizeFileName(String name) {
        String safe = StringUtils.hasText(name) ? name.trim() : "backup";
        return safe.replaceAll("[\\\\/:*?\"<>|\\s]+", "_");
    }

    /**
     * 把失败原因追加到 description 上（保留原有的描述）
     */
    private String appendFailureReason(String description, String message) {
        String reason = StringUtils.hasText(message) ? message.trim() : "未知错误";
        String merged = StringUtils.hasText(description)
                ? description + "；失败原因：" + reason
                : "失败原因：" + reason;
        return truncate(merged, DESCRIPTION_MAX_LENGTH);
    }

    /**
     * 按列长度截断
     */
    private String truncate(String text, int maxLength) {
        if (text == null) {
            return null;
        }
        return text.length() <= maxLength ? text : text.substring(0, maxLength);
    }

    /**
     * 读命令的 stderr，只取前几行，避免整段报错塞进响应
     */
    private String readErrorBriefly(Path errorFile) {
        try {
            String text = Files.readString(errorFile).trim();
            if (!StringUtils.hasText(text)) {
                return "命令没有输出错误信息";
            }
            String[] lines = text.split("\\R");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < lines.length && i < 3; i++) {
                if (StringUtils.hasText(lines[i])) {
                    sb.append(lines[i].trim()).append(' ');
                }
            }
            return truncate(sb.toString().trim(), DESCRIPTION_MAX_LENGTH);
        } catch (IOException e) {
            return "无法读取命令的错误输出";
        }
    }

    /**
     * 从 spring.datasource.url 里解析出来的连接信息
     */
    private record DbInfo(String host, String port, String database) {
    }
}