package com.ar.contractreview.controller;

import com.ar.contractreview.entity.SysOperationLog;
import com.ar.contractreview.result.R;
import com.ar.contractreview.service.SysOperationLogService;
import com.ar.contractreview.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 操作日志表 前端控制器
 * </p>
 * <p>
 * 对应《接口文档.md》第 17 章「操作日志接口」（context-path 为 /api，浏览器实际访问带 /api 前缀）：
 * <pre>
 * 17.1 获取日志列表   GET   /api/logs   -> list()
 * </pre>
 * 前端使用位置：OperationLog.vue（操作日志页面）
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
@RestController
@RequestMapping("/logs")
public class SysOperationLogController {

    /**
     * 时间格式化器：把实体的 LocalDateTime（createdTime）格式化成文档要求的 createTime 样式
     * DateTimeFormatter 线程安全，定义为常量全局复用
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 操作日志 Service，继承 MyBatis-Plus 的 IService，page() 等通用方法已内置，无需自己写 SQL
     */
    @Autowired
    private SysOperationLogService operationLogService;

    /**
     * 17.1 获取日志列表（分页 + 条件查询）
     * <p>
     * 前端在操作日志页面加载时调用，展示操作日志列表表格。
     * 所有查询参数都是可选的：不传就查全部，传了就按条件过滤。
     * </p>
     * <p>
     * 参数冲突说明（已与需求方确认）：《接口文档.md》写的是 userId，《接口实现指南.md》写的是 userName，
     * 这里两个参数都支持——userId 按 user_id 精确匹配，userName 按 user_name 模糊匹配，可单独传也可一起传。
     * 时间范围 startTime/endTime 两种字符串格式都兼容（"yyyy-MM-dd" 或 "yyyy-MM-dd HH:mm:ss"），
     * 以参数化方式交给 MySQL 与 datetime 列比较，无需在后端写死解析格式。
     * </p>
     *
     * @param page          页码，默认 1
     * @param size          每页条数，默认 10
     * @param userId        操作人ID（精确匹配，对应接口文档）
     * @param userName      操作人姓名（模糊匹配，对应实现指南）
     * @param operationType 操作类型（精确匹配，如 LOGIN/CREATE/UPDATE/DELETE/QUERY）
     * @param module        操作模块（精确匹配，如 用户管理/合同管理）
     * @param startTime     开始时间（按操作时间 created_time 过滤，包含边界）
     * @param endTime       结束时间（按操作时间 created_time 过滤，包含边界）
     * @return 分页结果：list（日志列表）、total（总数）、page（页码）、size（每页条数）
     */
    @GetMapping
    public R list(
            // 注意：按《接口要求.md》，@RequestParam 必须写 value
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "userName", required = false) String userName,
            @RequestParam(value = "operationType", required = false) String operationType,
            @RequestParam(value = "module", required = false) String module,
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime) {

        // 1. 构建动态查询条件：哪个参数有值就拼哪个条件（QueryWrapper 里写数据库字段名，不是 Java 属性名）
        QueryWrapper<SysOperationLog> wrapper = new QueryWrapper<>();
        if (userId != null) {
            // 用户ID是数字主键，用 eq 精确匹配
            wrapper.eq("user_id", userId);
        }
        if (!StringUtils.isEmpty(userName)) {
            // 姓名是中文文本搜索，用 like 模糊匹配
            wrapper.like("user_name", userName);
        }
        if (!StringUtils.isEmpty(operationType)) {
            // 操作类型是固定枚举值（LOGIN/CREATE/...），用 eq 精确匹配
            wrapper.eq("operation_type", operationType);
        }
        if (!StringUtils.isEmpty(module)) {
            // 操作模块是固定名称（用户管理/合同管理/...），用 eq 精确匹配
            wrapper.eq("module", module);
        }
        if (!StringUtils.isEmpty(startTime)) {
            // ge = created_time >= startTime；值通过占位符参数化绑定，防 SQL 注入
            // MySQL 会自动把 '2026-09-10' 和 '2026-09-10 00:00:00' 两种字符串与 datetime 列比较
            wrapper.ge("created_time", startTime);
        }
        if (!StringUtils.isEmpty(endTime)) {
            // le = created_time <= endTime；只选日期（如 2026-09-10）时会包含当天 00:00:00 这一秒，
            // 若想查"当天全天"，前端应传 2026-09-10 23:59:59
            wrapper.le("created_time", endTime);
        }

        // 2. 分页查询：分页插件（MybatisPlusConfig 已配置 PaginationInnerInterceptor）会自动拼 LIMIT
        IPage<SysOperationLog> pageResult = operationLogService.page(new Page<>(page, size), wrapper);

        // 3. 把实体列表转换成文档要求的字段结构（不直接返回实体）：
        //    一是实体字段 createdTime 对应文档字段 createTime；
        //    二是实体里还有 requestParam/responseResult/userAgent/executionTime 等大字段，文档列表不需要，避免无谓传输
        List<Map<String, Object>> list = new ArrayList<>();
        for (SysOperationLog log : pageResult.getRecords()) {
            // LinkedHashMap 保证输出 JSON 的字段顺序和文档一致，便于对照调试
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", log.getId());
            item.put("userId", log.getUserId());
            item.put("userName", log.getUserName());
            item.put("operationType", log.getOperationType());
            item.put("module", log.getModule());
            item.put("description", log.getDescription());
            item.put("requestUrl", log.getRequestUrl());
            item.put("ipAddress", log.getIpAddress());
            // success 在实体里是 Byte（0-失败 1-成功），序列化成 JSON 后就是数字 0/1，直接放入即可
            item.put("success", log.getSuccess());
            item.put("errorMessage", log.getErrorMessage());
            // LocalDateTime → "yyyy-MM-dd HH:mm:ss" 字符串；判空防止 NPE
            item.put("createTime", log.getCreatedTime() == null ? null : log.getCreatedTime().format(DATE_TIME_FORMATTER));
            list.add(item);
        }

        // 4. 统一返回 R，按文档结构把分页信息放进 data（与条款库列表返回结构一致）
        return R.ok()
                .data("list", list)
                .data("total", pageResult.getTotal())
                .data("page", page)
                .data("size", size);
    }
}
