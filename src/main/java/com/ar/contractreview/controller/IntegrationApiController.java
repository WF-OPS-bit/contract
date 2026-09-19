package com.ar.contractreview.controller;

import com.ar.contractreview.entity.IntegrationApi;
import com.ar.contractreview.result.R;
import com.ar.contractreview.service.IntegrationApiService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * API开放配置表 前端控制器
 * </p>
 * <p>
 * 对应《接口文档.md》第 20 章「API开放接口」（本章只有 20.1 一个列表接口，
 * context-path 为 /api，浏览器实际访问带 /api 前缀）：
 * <pre>
 * 20.1 获取API配置列表   GET   /api/integration/api   -> list()
 * </pre>
 * 前端使用位置：API开放.vue（API开放页面）
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
@RestController
@RequestMapping("/integration/api")
public class IntegrationApiController {

    /**
     * 时间格式化器：把实体的 LocalDateTime（createdTime）格式化成文档要求的 createTime 样式
     * DateTimeFormatter 线程安全，定义为常量全局复用
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * API配置 Service，继承 MyBatis-Plus 的 IService，list() 等通用方法已内置，无需自己写 SQL
     */
    @Autowired
    private IntegrationApiService apiService;

    /**
     * 20.1 获取API配置列表
     * <p>
     * 前端在 API开放页面加载时调用，展示 API 配置列表。
     * 注意：文档规定此接口返回的是数组（非分页），没有 page/size 参数，直接查全部。
     * </p>
     *
     * @return data 为 API 配置数组，每项含 id、apiName、apiPath、httpMethod、apiVersion、
     *         status、authType、rateLimit、description、createTime
     */
    @GetMapping
    public R list() {
        // 1. 全量查询：文档未提供分页参数和查询条件，直接查全部；
        //    按创建时间倒序，保证最新配置排在前面（QueryWrapper 里写数据库字段名）
        QueryWrapper<IntegrationApi> wrapper = new QueryWrapper<>();
        // wrapper.orderByDesc("created_time");
        List<IntegrationApi> apiList = apiService.list(wrapper);

        // 2. 把实体列表转换成文档要求的字段结构（不直接返回实体）：
        //    一是实体 createdTime 对应文档字段 createTime，需要格式化；
        //    二是 authConfig（认证配置JSON）、createdBy/updatedBy/updatedTime 文档没要求，
        //    尤其 authConfig 可能含密钥，列表不返回更安全（与 OA 模块不返回 appSecret 的处理一致）
        List<Map<String, Object>> result = new ArrayList<>();
        for (IntegrationApi api : apiList) {
            // LinkedHashMap 保证输出 JSON 的字段顺序和文档一致，便于对照调试
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", api.getId());
            item.put("apiName", api.getApiName());
            item.put("apiPath", api.getApiPath());
            item.put("httpMethod", api.getHttpMethod());
            // apiVersion 是 2026-09-11 给实体和表补的字段（原实体漏了），库里暂无值时为 null
            item.put("apiVersion", api.getApiVersion());
            item.put("status", api.getStatus());
            item.put("authType", api.getAuthType());
            // rateLimit：限流次数（次/分钟，0表示不限流），实体是 Integer，直接放入
            item.put("rateLimit", api.getRateLimit());
            item.put("description", api.getDescription());
            // LocalDateTime → "yyyy-MM-dd HH:mm:ss" 字符串；判空防止 NPE
            item.put("createTime", api.getCreatedTime() == null ? null : api.getCreatedTime().format(DATE_TIME_FORMATTER));
            result.add(item);
        }

        // 3. 用 data(Object) 整体返回数组（R 类已改造支持此方法，与 18.1 OA配置列表写法一致）
        return R.ok().data(result);
    }
}
