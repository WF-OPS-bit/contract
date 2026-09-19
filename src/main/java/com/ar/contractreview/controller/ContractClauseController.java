package com.ar.contractreview.controller;

import com.ar.contractreview.entity.ContractClause;
import com.ar.contractreview.result.R;
import com.ar.contractreview.result.ResponseCode;
import com.ar.contractreview.service.ContractClauseService;
import com.ar.contractreview.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 条款库表 前端控制器
 * </p>
 * <p>
 * 对应《接口文档.md》第 10 章「条款库接口」共 5 个接口（context-path 为 /api，所以浏览器实际访问带 /api 前缀）：
 * <pre>
 * 10.1 获取条款列表   GET    /api/clauses          -> list()
 * 10.2 获取条款详情   GET    /api/clauses/{id}     -> detail()
 * 10.3 新增条款       POST   /api/clauses          -> add()
 * 10.4 更新条款       PUT    /api/clauses/{id}     -> update()
 * 10.5 删除条款       DELETE /api/clauses/{id}     -> delete()
 * </pre>
 * 前端使用位置：ClauseLibrary.vue（条款库页面）
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
@RestController
@RequestMapping("/clauses")
public class ContractClauseController {

    /**
     * 时间格式化器：把 LocalDateTime 格式化成接口文档要求的样式（如 2026-07-01 10:00:00）
     * DateTimeFormatter 是线程安全的，定义为常量全局复用，避免每次请求重复创建
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 条款库 Service，继承 MyBatis-Plus 的 IService，
     * page()/getById()/save()/updateById()/removeById() 等通用方法已内置，无需自己写 SQL
     */
    @Autowired
    private ContractClauseService clauseService;

    /**
     * 10.1 获取条款列表（分页 + 条件查询）
     * <p>
     * 前端在条款库页面加载时调用，展示条款列表表格。
     * 所有查询参数都是可选的：不传就查全部，传了就按条件过滤。
     * </p>
     *
     * @param page           页码，默认 1
     * @param size           每页条数，默认 10
     * @param clauseName     条款名称（模糊查询）
     * @param clauseCategory 条款类别（精确匹配，如 COMMON/SPECIAL/RISK）
     * @param contractType   适用合同类型（精确匹配）
     * @param riskLevel      风险等级（精确匹配，如 HIGH/MEDIUM/LOW）
     * @param status         状态（精确匹配，0-禁用 1-启用）
     * @return 分页结果：list（条款列表）、total（总数）、page（页码）、size（每页条数）
     */
    @GetMapping
    public R list(
            // 注意：按《接口要求.md》，@RequestParam 必须写 value，否则报 120 系统异常
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "clauseName", required = false) String clauseName,
            @RequestParam(value = "clauseCategory", required = false) String clauseCategory,
            @RequestParam(value = "contractType", required = false) String contractType,
            @RequestParam(value = "riskLevel", required = false) String riskLevel,
            @RequestParam(value = "status", required = false) Byte status) {

        // 1. 构建动态查询条件：哪个参数有值就拼哪个条件（QueryWrapper 的列名写数据库字段名，不是 Java 属性名）
        QueryWrapper<ContractClause> wrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(clauseName)) {
            // 名称类搜索用 like 模糊匹配
            wrapper.like("clause_name", clauseName);
        }
        if (!StringUtils.isEmpty(clauseCategory)) {
            // 类别/类型/等级是固定枚举值，用 eq 精确匹配
            wrapper.eq("clause_category", clauseCategory);
        }
        if (!StringUtils.isEmpty(contractType)) {
            wrapper.eq("contract_type", contractType);
        }
        if (!StringUtils.isEmpty(riskLevel)) {
            wrapper.eq("risk_level", riskLevel);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        // 按创建时间倒序，保证最新添加的条款排在前面
        wrapper.orderByDesc("created_time");

        // 2. 分页查询：分页插件（MybatisPlusConfig 已配置 PaginationInnerInterceptor）会自动拼 LIMIT
        IPage<ContractClause> pageResult = clauseService.page(new Page<>(page, size), wrapper);

        // 3. 把实体列表转换成文档要求的字段结构。
        //    不直接返回实体，因为实体字段叫 createdTime，而接口文档约定叫 createTime（团队已确认手动组装 Map）
        List<Map<String, Object>> list = new ArrayList<>();
        for (ContractClause clause : pageResult.getRecords()) {
            // LinkedHashMap 保证输出 JSON 的字段顺序和文档一致，便于对照调试
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", clause.getId());
            item.put("clauseCode", clause.getClauseCode());
            item.put("clauseName", clause.getClauseName());
            item.put("clauseCategory", clause.getClauseCategory());
            item.put("contractType", clause.getContractType());
            item.put("riskLevel", clause.getRiskLevel());
            item.put("description", clause.getDescription());
            item.put("status", clause.getStatus());
            // 实体的 createdTime 是 LocalDateTime，这里转成文档要求的 "yyyy-MM-dd HH:mm:ss" 字符串；判空防止 NPE
            item.put("createTime", clause.getCreatedTime() == null ? null : clause.getCreatedTime().format(DATE_TIME_FORMATTER));
            list.add(item);
        }

        // 4. 统一返回 R，按文档结构把分页信息放进 data
        return R.ok()
                .data("list", list)
                .data("total", pageResult.getTotal())
                .data("page", page)
                .data("size", size);
    }

    /**
     * 10.2 获取条款详情
     * <p>
     * 前端在条款列表中点击"查看详情"按钮时调用，弹窗展示完整信息（含条款内容、标准内容、法律依据）。
     * </p>
     *
     * @param id 条款ID（路径参数）
     * @return 条款详情对象；条款不存在时返回失败信息
     */
    @GetMapping("/{id}")
    public R detail(@PathVariable(value = "id") Long id) {
        // getById 自带逻辑删除过滤：已被删除（deleted != 0）的记录查不到，等同"不存在"
        ContractClause clause = clauseService.getById(id);
        if (clause == null) {
            // 查不到直接返回失败，不再往下走（R.fail 不要链式 .data()，会把状态码改回成功）
            return R.fail(ResponseCode.PARAMETER_EXCEPTION.getCode(), "条款不存在");
        }

        // 按文档要求手动组装响应字段（字段名以文档为准：createTime 而不是实体的 createdTime）
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", clause.getId());
        data.put("clauseCode", clause.getClauseCode());
        data.put("clauseName", clause.getClauseName());
        data.put("clauseCategory", clause.getClauseCategory());
        data.put("contractType", clause.getContractType());
        data.put("content", clause.getContent());
        data.put("standardContent", clause.getStandardContent());
        data.put("riskLevel", clause.getRiskLevel());
        data.put("description", clause.getDescription());
        data.put("legalBasis", clause.getLegalBasis());
        data.put("status", clause.getStatus());

        // .data(Map) 会把 Map 内容合并进 R 的 data，最终输出 "data": {id:..., clauseCode:...}，与文档一致
        return R.ok().data(data);
    }

    /**
     * 10.3 新增条款
     * <p>
     * 前端在条款库页面点击"新增条款"按钮提交表单时调用。
     * 请求体直接用实体 ContractClause 接收（JSON 字段名与实体属性名一致，Spring 自动反序列化）。
     * </p>
     *
     * @param clause 请求体中的条款信息（clauseName、clauseCode、clauseCategory 必填）
     * @return 新增后的条款关键信息（id、clauseCode、clauseName、status、createTime）
     */
    @PostMapping
    public R add(@RequestBody ContractClause clause) {
        // 1. 必填字段校验：文档规定 clauseName / clauseCode / clauseCategory 是必填项
        if (StringUtils.isEmpty(clause.getClauseName())
                || StringUtils.isEmpty(clause.getClauseCode())
                || StringUtils.isEmpty(clause.getClauseCategory())) {
            return R.fail(ResponseCode.PARAMETER_EXCEPTION.getCode(), "条款名称、条款编码、条款类别为必填项");
        }

        // 2. 条款编码唯一性校验：同编码不允许重复添加（文档未明确要求，属于合理业务约束）
        Long count = clauseService.count(new QueryWrapper<ContractClause>().eq("clause_code", clause.getClauseCode()));
        if (count > 0) {
            return R.fail(ResponseCode.PARAMETER_EXCEPTION.getCode(), "条款编码已存在：" + clause.getClauseCode());
        }

        // 3. 补充默认值和审计字段（请求体里没有这些字段，需要后端自己填）
        if (clause.getStatus() == null) {
            // 文档要求新增后状态默认是 1（启用）
            clause.setStatus((byte) 1);
        }
        // 逻辑删除标记默认 0（未删除），显式设置避免入库为 null
        clause.setDeleted((byte) 0);
        // 创建人/更新人取当前登录用户（登录后 SecurityContext 的 principal 就是用户名）
        String username = currentUsername();
        clause.setCreatedBy(username);
        clause.setUpdatedBy(username);
        LocalDateTime now = LocalDateTime.now();
        clause.setCreatedTime(now);
        clause.setUpdatedTime(now);

        // 4. 入库：save 之后 MyBatis-Plus 会把自增主键回填到 clause.id
        clauseService.save(clause);

        // 5. 按文档要求返回新增条款的关键信息
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", clause.getId());
        data.put("clauseCode", clause.getClauseCode());
        data.put("clauseName", clause.getClauseName());
        data.put("status", clause.getStatus());
        data.put("createTime", now.format(DATE_TIME_FORMATTER));
        return R.ok().data(data);
    }

    /**
     * 10.4 更新条款
     * <p>
     * 前端在条款列表中点击"编辑"按钮，修改后提交时调用。
     * 请求体同新增条款；只更新传了值的字段（MyBatis-Plus 的 updateById 默认忽略 null 字段）。
     * </p>
     *
     * @param id     要更新的条款ID（路径参数）
     * @param clause 请求体中的条款信息
     * @return 更新后的关键信息（id、clauseName、status）
     */
    @PutMapping("/{id}")
    public R update(@PathVariable(value = "id") Long id, @RequestBody ContractClause clause) {
        // 1. 先查原记录，确认要更新的条款存在
        ContractClause exist = clauseService.getById(id);
        if (exist == null) {
            return R.fail(ResponseCode.PARAMETER_EXCEPTION.getCode(), "条款不存在");
        }

        // 2. 必填字段校验（文档说请求体同新增条款，所以必填规则也一致）
        if (StringUtils.isEmpty(clause.getClauseName())
                || StringUtils.isEmpty(clause.getClauseCode())
                || StringUtils.isEmpty(clause.getClauseCategory())) {
            return R.fail(ResponseCode.PARAMETER_EXCEPTION.getCode(), "条款名称、条款编码、条款类别为必填项");
        }

        // 3. 条款编码唯一性校验：排除自己（自己改名字不改编码是允许的）
        Long count = clauseService.count(new QueryWrapper<ContractClause>()
                .eq("clause_code", clause.getClauseCode())
                // ne 即 not equal：编码相同但 id 不是当前这条的，才算重复
                .ne("id", id));
        if (count > 0) {
            return R.fail(ResponseCode.PARAMETER_EXCEPTION.getCode(), "条款编码已存在：" + clause.getClauseCode());
        }

        // 4. 强制以路径上的 id 为准，防止请求体里携带 id 篡改到别的记录
        clause.setId(id);
        // 审计字段：更新人、更新时间由后端维护，不信任前端传值
        clause.setUpdatedBy(currentUsername());
        clause.setUpdatedTime(LocalDateTime.now());

        // 5. updateById 只更新非 null 字段（MP 默认策略），所以没传的字段（如 contractType）保持原值
        clauseService.updateById(clause);

        // 6. 按文档要求返回更新后的关键信息
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", id);
        data.put("clauseName", clause.getClauseName());
        data.put("status", clause.getStatus() == null ? exist.getStatus() : clause.getStatus());
        return R.ok().data(data);
    }

    /**
     * 10.5 删除条款
     * <p>
     * 前端在条款列表中点击"删除"按钮，确认后调用。
     * 这里是逻辑删除：实体 deleted 字段标了 @TableLogic，removeById 只会把删除标记改成
     * 全局配置的删除值（application.yml 中 logic-delete-value: -1），数据并不会物理删除。
     * </p>
     *
     * @param id 要删除的条款ID（路径参数）
     * @return data: true 表示删除成功；条款不存在时返回失败信息
     */
    @DeleteMapping("/{id}")
    public R delete(@PathVariable(value = "id") Long id) {
        // removeById 执行逻辑删除，返回是否真的删掉了数据（记录不存在或已删过则返回 false）
        boolean removed = clauseService.removeById(id);
        if (!removed) {
            return R.fail(ResponseCode.PARAMETER_EXCEPTION.getCode(), "条款不存在或已删除");
        }
        // 按文档要求 data 直接返回布尔值 true（R 类已改造支持 data(Object) 整体赋值）
        return R.ok().data(true);
    }

    /**
     * 获取当前登录用户名
     * <p>
     * 登录成功后，Spring Security 会把用户名放在 Authentication 的 principal 中，
     * 从 SecurityContextHolder 随时可以取到当前请求的用户身份。
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

