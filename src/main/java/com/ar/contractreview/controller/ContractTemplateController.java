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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @Autowired
    private ContractTemplateService templateService;

    @Autowired
    private ContractClauseService clauseService;

    /**
     * 9.1 获取模板列表（分页，支持模板编号/名称模糊、合同类型/状态精确筛选）
     */
    @GetMapping("/list")
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

        return R.ok()
                .data("list", pageResult.getRecords())
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

        Map<String, Object> data = new ObjectMapper().convertValue(
                template, new TypeReference<LinkedHashMap<String, Object>>() {
                });
        data.put("clauses", clauses);

        return R.ok().data("data", data);
    }
}
