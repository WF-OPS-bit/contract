package com.ar.contractreview.controller;

import com.ar.contractreview.entity.IntegrationOa;
import com.ar.contractreview.result.R;
import com.ar.contractreview.result.ResponseCode;
import com.ar.contractreview.service.IntegrationOaService;
import com.ar.contractreview.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * OA对接配置表 前端控制器
 * </p>
 * <p>
 * 对应《接口文档.md》第 18 章「OA对接接口」共 7 个接口（context-path 为 /api，浏览器实际访问带 /api 前缀）：
 * <pre>
 * 18.1 获取OA配置列表   GET    /api/integration/oa          -> list()
 * 18.2 获取OA配置详情   GET    /api/integration/oa/{id}     -> detail()
 * 18.3 新增OA配置       POST   /api/integration/oa          -> add()
 * 18.4 更新OA配置       PUT    /api/integration/oa/{id}     -> update()
 * 18.5 删除OA配置       DELETE /api/integration/oa/{id}     -> delete()
 * 18.6 获取OA对接日志   GET    /api/integration/oa/{id}/logs -> logs()        —— 待实现
 * 18.7 同步OA数据       POST   /api/integration/oa/{id}/sync -> sync()        —— 待实现
 * </pre>
 * 前端使用位置：OADocking.vue（OA对接页面）
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
@RestController
@RequestMapping("/integration/oa")
public class IntegrationOaController {

    /**
     * 时间格式化器：把 LocalDateTime 格式化成接口文档要求的样式（如 2026-07-06 08:00:00）
     * DateTimeFormatter 是线程安全的，定义为常量全局复用
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * OA对接 Service，继承 MyBatis-Plus 的 IService，
     * list()/getById() 等通用方法已内置，无需自己写 SQL
     */
    @Autowired
    private IntegrationOaService oaService;

    /**
     * 18.1 获取OA配置列表
     * <p>
     * 前端在 OA 对接页面加载时调用，展示 OA 配置列表。
     * 注意：文档规定此接口返回的是数组（非分页），没有 page/size 参数，直接查全部。
     * </p>
     *
     * @return data 为 OA 配置数组，每项含 id、oaType、oaName、status、syncStatus、lastSyncTime
     */
    @GetMapping
    public R list() {
        // 1. 全量查询：文档未提供分页参数和查询条件，直接查全部
        //    不额外排序，按数据库默认（id 升序），与文档示例一致
        QueryWrapper<IntegrationOa> wrapper = new QueryWrapper<>();
        List<IntegrationOa> oaList = oaService.list(wrapper);

        // 2. 把实体列表转换成文档要求的字段结构
        //    不直接返回实体，因为 lastSyncTime 是 LocalDateTime，需要格式化成字符串
        List<Map<String, Object>> result = new ArrayList<>();
        for (IntegrationOa oa : oaList) {
            // LinkedHashMap 保证输出 JSON 的字段顺序和文档一致
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", oa.getId());
            item.put("oaType", oa.getOaType());
            item.put("oaName", oa.getOaName());
            item.put("status", oa.getStatus());
            item.put("syncStatus", oa.getSyncStatus());
            // 时间格式化：LocalDateTime → "yyyy-MM-dd HH:mm:ss" 字符串；判空防止 NPE
            LocalDateTime lastSync = oa.getLastSyncTime();
            item.put("lastSyncTime", lastSync == null ? null : lastSync.format(DATE_TIME_FORMATTER));
            result.add(item);
        }

        // 3. 用 data(Object) 整体返回数组（R 类已改造支持此方法）
        return R.ok().data(result);
    }

    /**
     * 18.2 获取OA配置详情
     * <p>
     * 前端在 OA 配置列表中点击"查看详情"按钮时调用，展示完整配置信息（含 appId、描述等）。
     * </p>
     *
     * @param id OA对接ID（路径参数）
     * @return OA 配置详情对象；不存在时返回失败信息
     */
    @GetMapping("/{id}")
    public R detail(@PathVariable(value = "id") Long id) {
        // 1. 按主键查询，查不到返回失败
        IntegrationOa oa = oaService.getById(id);
        if (oa == null) {
            return R.fail(ResponseCode.PARAMETER_EXCEPTION.getCode(), "OA配置不存在");
        }

        // 2. 按文档要求手动组装响应字段（lastSyncTime 需格式化为字符串）
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", oa.getId());
        data.put("oaType", oa.getOaType());
        data.put("oaName", oa.getOaName());
        data.put("status", oa.getStatus());
        data.put("appId", oa.getAppId());
        data.put("syncStatus", oa.getSyncStatus());
        // 时间格式化
        LocalDateTime lastSync = oa.getLastSyncTime();
        data.put("lastSyncTime", lastSync == null ? null : lastSync.format(DATE_TIME_FORMATTER));
        data.put("description", oa.getDescription());

        // 3. data(Map) 会把 Map 内容合并进 R 的 data，最终输出 "data": {id:..., oaType:...}
        return R.ok().data(data);
    }

    /**
     * 18.3 新增OA配置
     * <p>
     * 前端在 OA 对接页面点击"新增配置"按钮提交表单时调用。
     * 请求体直接用实体 IntegrationOa 接收（JSON 字段名与实体属性名一致，Spring 自动反序列化）。
     * </p>
     *
     * @param oa 请求体中的 OA 配置信息（oaType、oaName 必填）
     * @return 新增后的配置关键信息（id、oaType、oaName、status）
     */
    @PostMapping
    public R add(@RequestBody IntegrationOa oa) {
        // 1. 必填字段校验：文档 18.3 规定 oaType / oaName 是必填项
        if (StringUtils.isEmpty(oa.getOaType()) || StringUtils.isEmpty(oa.getOaName())) {
            return R.fail(ResponseCode.PARAMETER_EXCEPTION.getCode(), "OA类型、OA名称为必填项");
        }

        // 2. 补充默认值（请求体里没有这些字段，由后端定默认值）：
        //    status 默认 DISCONNECTED（文档 18.3 响应示例新增后 status 就是 DISCONNECTED，表示还没连接过）；
        //    syncStatus 默认 DISABLED（配置刚建还没验证连通性，不允许同步，等用户手动启用）
        if (oa.getStatus() == null) {
            oa.setStatus("DISCONNECTED");
        }
        if (oa.getSyncStatus() == null) {
            oa.setSyncStatus("DISABLED");
        }

        // 3. 审计字段：创建人/更新人取当前登录用户（SecurityContext 的 principal 就是用户名），时间由后端生成
        String username = currentUsername();
        oa.setCreatedBy(username);
        oa.setUpdatedBy(username);
        LocalDateTime now = LocalDateTime.now();
        oa.setCreatedTime(now);
        oa.setUpdatedTime(now);

        // 4. 入库：save 之后 MyBatis-Plus 会把自增主键回填到 oa.id
        oaService.save(oa);

        // 5. 按文档 18.3 要求返回新增配置的关键信息。
        //    注意不返回 appSecret/accessToken 等敏感字段（老师参考代码直接返回整个实体，会把密钥泄露给前端，这里按文档只返回 4 个字段）
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", oa.getId());
        data.put("oaType", oa.getOaType());
        data.put("oaName", oa.getOaName());
        data.put("status", oa.getStatus());
        return R.ok().data(data);
    }

    /**
     * 18.4 更新OA配置
     * <p>
     * 前端在 OA 配置列表中点击"编辑"按钮，修改后提交时调用。
     * 请求体同新增；只更新传了值的字段（MyBatis-Plus 的 updateById 默认忽略 null 字段）。
     * </p>
     *
     * @param id 要更新的OA配置ID（路径参数）
     * @param oa 请求体中的 OA 配置信息
     * @return 更新后的关键信息（id、oaName、status）
     */
    @PutMapping("/{id}")
    public R update(@PathVariable(value = "id") Long id, @RequestBody IntegrationOa oa) {
        // 1. 先查原记录，确认要更新的配置存在
        IntegrationOa exist = oaService.getById(id);
        if (exist == null) {
            return R.fail(ResponseCode.PARAMETER_EXCEPTION.getCode(), "OA配置不存在");
        }

        // 2. 必填字段校验（文档说请求体同新增，所以 oaType/oaName 必填规则一致）
        if (StringUtils.isEmpty(oa.getOaType()) || StringUtils.isEmpty(oa.getOaName())) {
            return R.fail(ResponseCode.PARAMETER_EXCEPTION.getCode(), "OA类型、OA名称为必填项");
        }

        // 3. 强制以路径上的 id 为准，防止请求体里携带 id 篡改到别的记录
        oa.setId(id);
        // 审计字段：更新人、更新时间由后端维护，不信任前端传值
        oa.setUpdatedBy(currentUsername());
        oa.setUpdatedTime(LocalDateTime.now());

        // 4. updateById 只更新非 null 字段（MP 默认策略），没传的字段（如 appSecret、syncStatus）保持原值
        oaService.updateById(oa);

        // 5. 按文档 18.4 要求返回更新后的关键信息；
        //    status 若请求体没传则保持原值（updateById 不会覆盖它）
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", id);
        data.put("oaName", oa.getOaName());
        data.put("status", oa.getStatus() == null ? exist.getStatus() : oa.getStatus());
        return R.ok().data(data);
    }

    /**
     * 18.5 删除OA配置
     * <p>
     * 前端在 OA 配置列表中点击"删除"按钮，确认后调用。
     * 注意：IntegrationOa 实体没有 @TableLogic 逻辑删除字段（对比条款库的 ContractClause 有），
     * 所以这里的 removeById 是物理删除（直接 DELETE 这行数据），删了就找不回来了。
     * </p>
     *
     * @param id 要删除的OA配置ID（路径参数）
     * @return data: true 表示删除成功；配置不存在时返回失败信息
     */
    @DeleteMapping("/{id}")
    public R delete(@PathVariable(value = "id") Long id) {
        // removeById 返回是否真的删掉了数据（记录不存在时返回 false）
        boolean removed = oaService.removeById(id);
        if (!removed) {
            return R.fail(ResponseCode.PARAMETER_EXCEPTION.getCode(), "OA配置不存在或已删除");
        }
        // 按文档 18.5 要求 data 直接返回布尔值 true（R 类已改造支持 data(Object) 整体赋值）
        return R.ok().data(true);
    }

    /**
     * 获取当前登录用户名
     * <p>
     * 登录成功后，Spring Security 会把用户名放在 Authentication 的 principal 中，
     * 从 SecurityContextHolder 随时可以取到当前请求的用户身份（与 ContractClauseController 中的同名方法逻辑一致）。
     * </p>
     *
     * @return 当前登录用户名；获取不到时返回 null（不影响主流程）
     */
    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 判空保护：认证上下文理论上一定有值（所有接口都需登录），防御一下避免 NPE
        return authentication == null || authentication.getPrincipal() == null
                ? null
                : String.valueOf(authentication.getPrincipal());
    }
}
