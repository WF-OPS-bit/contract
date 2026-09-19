package com.ar.contractreview.controller;

import com.ar.contractreview.entity.ContractClause;
import com.ar.contractreview.entity.ContractTemplate;
import com.ar.contractreview.result.R;
import com.ar.contractreview.result.ResponseCode;
import com.ar.contractreview.service.ContractClauseService;
import com.ar.contractreview.service.ContractTemplateService;
import com.ar.contractreview.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 合同模板表 前端控制器
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
@RestController
@RequestMapping("/templates")
public class ContractTemplateController {

    /** 模板文件上传保存目录(运行目录/upload/templates,自动创建) */
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/upload/templates/";

    /** 模板文件的对外访问路径前缀(由 WebMVCConfig 映射到 upload 目录) */
    private static final String UPLOAD_URL_PREFIX = "/upload/templates/";

    @Autowired
    private ContractTemplateService templateService;

    @Autowired
    private ContractClauseService clauseService;

    /**
     * 9.1 获取模板列表（分页，支持模板编号/名称模糊、合同类型/状态精确筛选）
     */
    @GetMapping
    public R list(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size,
            @RequestParam(value = "templateNo", required = false) String templateNo,
            @RequestParam(value = "templateName", required = false) String templateName,
            @RequestParam(value = "contractType", required = false) String contractType,
            @RequestParam(value = "status", required = false) String status
    ) {
        QueryWrapper<ContractTemplate> wrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(templateNo)) {
            wrapper.like("template_no", templateNo);
        }
        if (!StringUtils.isEmpty(templateName)) {
            wrapper.like("template_name", templateName);
        }
        if (!StringUtils.isEmpty(contractType)) {
            wrapper.eq("contract_type", contractType);
        }
        if (!StringUtils.isEmpty(status)) {
            wrapper.eq("status", status);
        }

        IPage<ContractTemplate> pageResult = templateService.page(new Page<>(page, size), wrapper);

        // 响应字段严格按接口文档:仅输出文档列出的字段(表列 version/created_time 映射为文档名 templateVersion/createTime)
        List<Map<String, Object>> list = pageResult.getRecords().stream()
                .map(this::toListVO)
                .collect(Collectors.toList());

        return R.ok()
                .data("list", list)
                .data("total", pageResult.getTotal());
    }

    /**
     * 9.2 获取模板详情（含该合同类型适用的条款列表）
     */
    @GetMapping("/{id}")
    public R detail(@PathVariable(value = "id") Long id) {
        ContractTemplate template = templateService.getById(id);
        if (template == null) {
            return R.fail(ResponseCode.PARAMETER_EXCEPTION);
        }

        // 条款库按合同类型关联：相同类型或类型为空（通用）的条款均适用
        QueryWrapper<ContractClause> clauseWrapper = new QueryWrapper<>();
        String contractType = template.getContractType();
        if (StringUtils.isEmpty(contractType)) {
            clauseWrapper.and(w -> w.isNull("contract_type").or().eq("contract_type", ""));
        } else {
            clauseWrapper.and(w -> w.eq("contract_type", contractType)
                    .or().isNull("contract_type")
                    .or().eq("contract_type", ""));
        }
        clauseWrapper.orderByAsc("sort_order").orderByAsc("id");

        // 详情响应按接口文档输出 no/title 两个字段
        List<Map<String, Object>> clauses = clauseService.list(clauseWrapper).stream()
                .map(clause -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("no", clause.getClauseCode());
                    item.put("title", clause.getClauseName());
                    return item;
                })
                .collect(Collectors.toList());

        // 响应字段严格按接口文档(表列 version/created_time 映射为文档名 templateVersion/createTime;
        // applicableScope 数据表中无对应列,暂无数据可输出)
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", template.getId());
        data.put("templateNo", template.getTemplateNo());
        data.put("templateName", template.getTemplateName());
        data.put("contractType", template.getContractType());
        data.put("templateVersion", template.getVersion());
        data.put("fileUrl", template.getFileUrl());
        data.put("description", template.getDescription());
        data.put("status", template.getStatus());
        data.put("clauses", clauses);

        return R.ok().data("data", data);
    }

    /**
     * 9.3 新增模板（multipart 表单：基本信息 + 模板文件上传）
     * 地址：POST /api/templates，Content-Type: multipart/form-data
     * 字段：templateName/contractType/templateVersion 必填，description 选填，file 必填
     * 说明：
     *  1. templateNo 由后端自动生成（TPL + 时间戳，保证唯一）；
     *  2. 模板文件保存到运行目录 upload/templates/ 下，库里只存 file_url 访问路径；
     *  3. 状态默认 ACTIVE（启用）。
     */
    @PostMapping
    public R add(
            @RequestParam(value = "templateName") String templateName,
            @RequestParam(value = "contractType") String contractType,
            @RequestParam(value = "templateVersion") String templateVersion,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "file") MultipartFile file
    ) {
        // ---- 1. 必填校验（不满足直接返回参数异常，前端好提示）----
        if (StringUtils.isEmpty(templateName)) {
            return R.fail(ResponseCode.PARAMETER_EXCEPTION.getCode(), "模板名称不能为空");
        }
        if (StringUtils.isEmpty(contractType)) {
            return R.fail(ResponseCode.PARAMETER_EXCEPTION.getCode(), "合同类型不能为空");
        }
        if (StringUtils.isEmpty(templateVersion)) {
            return R.fail(ResponseCode.PARAMETER_EXCEPTION.getCode(), "版本号不能为空");
        }
        if (file == null || file.isEmpty()) {
            return R.fail(ResponseCode.PARAMETER_EXCEPTION.getCode(), "请上传模板文件");
        }

        // ---- 2. 保存模板文件：目录不存在先创建；文件名加时间戳前缀防重名覆盖 ----
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists() && !dir.mkdirs()) {
            return R.fail(ResponseCode.PARAMETER_EXCEPTION.getCode(), "模板文件目录创建失败");
        }
        String originalName = file.getOriginalFilename();
        String fileName = System.currentTimeMillis() + "_"
                + (StringUtils.isEmpty(originalName) ? "template.docx" : originalName);
        try {
            file.transferTo(new File(dir, fileName));
        } catch (IOException e) {
            return R.fail(ResponseCode.PARAMETER_EXCEPTION.getCode(), "模板文件保存失败");
        }

        // ---- 3. 组装实体入库（注意：数据库列名是 version，接口文档叫 templateVersion）----
        ContractTemplate template = new ContractTemplate()
                .setTemplateNo("TPL" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()))
                .setTemplateName(templateName)
                .setContractType(contractType)
                .setVersion(templateVersion)
                .setDescription(description)
                .setFileUrl(UPLOAD_URL_PREFIX + fileName)
                .setStatus("ACTIVE");
        templateService.save(template);

        // 响应字段严格按接口文档:id/templateNo/templateName/templateVersion/status/createTime
        // (重新查询一次,取数据库回填的 createdTime)
        ContractTemplate saved = templateService.getById(template.getId());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", saved.getId());
        data.put("templateNo", saved.getTemplateNo());
        data.put("templateName", saved.getTemplateName());
        data.put("templateVersion", saved.getVersion());
        data.put("status", saved.getStatus());
        data.put("createTime", saved.getCreatedTime());
        return R.ok().data("data", data);
    }

    /**
     * 9.4 更新模板（只更新传入的字段，未传的字段保持原值）
     * 地址：PUT /api/templates/{id}，Content-Type: application/json
     * 请求体可带字段：templateName / description / status
     */
    @PutMapping("/{id}")
    public R update(@PathVariable(value = "id") Long id, @RequestBody ContractTemplate req) {
        // ---- 1. 记录必须存在 ----
        ContractTemplate template = templateService.getById(id);
        if (template == null) {
            return R.fail(ResponseCode.PARAMETER_EXCEPTION.getCode(), "模板不存在");
        }
        // ---- 2. 只应用请求体里传了的字段（文档约定可改：名称/描述/状态）----
        if (!StringUtils.isEmpty(req.getTemplateName())) {
            template.setTemplateName(req.getTemplateName());
        }
        if (!StringUtils.isEmpty(req.getDescription())) {
            template.setDescription(req.getDescription());
        }
        if (!StringUtils.isEmpty(req.getStatus())) {
            template.setStatus(req.getStatus());
        }
        // ---- 3. 更新：MyBatis-Plus 的 updateById 自动忽略 null 字段，所以没传的不会被动 ----
        templateService.updateById(template);

        // 响应字段严格按接口文档:id/templateName/status
        ContractTemplate latest = templateService.getById(id);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", latest.getId());
        data.put("templateName", latest.getTemplateName());
        data.put("status", latest.getStatus());
        return R.ok().data("data", data);
    }

    /**
     * 9.5 删除模板
     * 地址：DELETE /api/templates/{id}
     * 说明：contract_template 表有 deleted 字段，全局配置了逻辑删除，
     *      removeById 实际执行 UPDATE deleted，数据不物理删除（列表/详情自动查不到）。
     */
    @DeleteMapping("/{id}")
    public R delete(@PathVariable(value = "id") Long id) {
        boolean ok = templateService.removeById(id);
        return R.ok().data("data", ok);
    }

    /**
     * 列表项转换为接口文档约定字段(文档名 templateVersion/createTime ↔ 表列 version/created_time)
     */
    private Map<String, Object> toListVO(ContractTemplate template) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", template.getId());
        item.put("templateNo", template.getTemplateNo());
        item.put("templateName", template.getTemplateName());
        item.put("contractType", template.getContractType());
        item.put("templateVersion", template.getVersion());
        item.put("status", template.getStatus());
        item.put("createdBy", template.getCreatedBy());
        item.put("createTime", template.getCreatedTime());
        return item;
    }
}
