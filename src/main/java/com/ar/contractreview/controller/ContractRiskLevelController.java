package com.ar.contractreview.controller;

import com.ar.contractreview.entity.ContractRiskLevel;
import com.ar.contractreview.result.R;
import com.ar.contractreview.result.ResponseCode;
import com.ar.contractreview.service.ContractRiskLevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 风险等级配置表 前端控制器
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
@RestController
@RequestMapping("/risk-levels")
public class ContractRiskLevelController {
    @Autowired
    private ContractRiskLevelService riskLevelService;

    /**
     * 12.1 获取风险等级列表
     * @return 风险等级列表（数组）
     */
    @GetMapping
    public R list() {
        List<ContractRiskLevel> list = riskLevelService.listRiskLevels();
        return R.ok().data(list);
    }

    /**
     * 12.2 获取风险等级详情
     * @param id 风险等级ID
     * @return 风险等级详情
     */
    @GetMapping("/{id}")
    public R detail(@PathVariable("id") Long id) {
        ContractRiskLevel riskLevel = riskLevelService.getRiskLevelById(id);
        if (null == riskLevel) {
            return R.fail(ResponseCode.PARAMETER_EXCEPTION.getCode(), "风险等级不存在");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("id", riskLevel.getId());
        data.put("riskCode", riskLevel.getRiskCode());
        data.put("riskName", riskLevel.getRiskName());
        data.put("riskColor", riskLevel.getRiskColor());
        data.put("scoreMin", riskLevel.getScoreMin());
        data.put("scoreMax", riskLevel.getScoreMax());
        data.put("description", riskLevel.getDescription());
        data.put("handlingSuggestion", riskLevel.getHandlingSuggestion());
        data.put("status", riskLevel.getStatus());
        data.put("sortOrder", riskLevel.getSortOrder());
        return R.ok().data(data);
    }

    /**
     * 12.3 新增风险等级
     * @param riskLevel 风险等级信息
     * @return 新增结果
     */
    @PostMapping
    public R add(@RequestBody ContractRiskLevel riskLevel) {
        // 必填字段校验
        if (null == riskLevel.getRiskCode() || riskLevel.getRiskCode().isEmpty()) {
            return R.fail(ResponseCode.PARAMETER_EXCEPTION.getCode(), "风险代码为必填项");
        }
        if (null == riskLevel.getRiskName() || riskLevel.getRiskName().isEmpty()) {
            return R.fail(ResponseCode.PARAMETER_EXCEPTION.getCode(), "风险名称为必填项");
        }
        if (null == riskLevel.getScoreMin()) {
            return R.fail(ResponseCode.PARAMETER_EXCEPTION.getCode(), "最低分数为必填项");
        }
        if (null == riskLevel.getScoreMax()) {
            return R.fail(ResponseCode.PARAMETER_EXCEPTION.getCode(), "最高分数为必填项");
        }

        ContractRiskLevel saved = riskLevelService.addRiskLevel(riskLevel);
        Map<String, Object> data = new HashMap<>();
        data.put("id", saved.getId());
        data.put("riskCode", saved.getRiskCode());
        data.put("riskName", saved.getRiskName());
        data.put("sortOrder", saved.getSortOrder());
        return R.ok().data(data);
    }

    /**
     * 12.4 更新风险等级
     * @param id 风险等级ID
     * @param riskLevel 风险等级信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public R update(@PathVariable("id") Long id, @RequestBody ContractRiskLevel riskLevel) {
        ContractRiskLevel exist = riskLevelService.getRiskLevelById(id);
        if (null == exist) {
            return R.fail(ResponseCode.PARAMETER_EXCEPTION.getCode(), "风险等级不存在");
        }

        riskLevel.setId(id);
        ContractRiskLevel updated = riskLevelService.updateRiskLevel(riskLevel);
        Map<String, Object> data = new HashMap<>();
        data.put("id", updated.getId());
        data.put("riskName", updated.getRiskName());
        return R.ok().data(data);
    }

    /**
     * 12.5 删除风险等级
     * @param id 风险等级ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public R delete(@PathVariable("id") Long id) {
        ContractRiskLevel exist = riskLevelService.getRiskLevelById(id);
        if (null == exist) {
            return R.fail(ResponseCode.PARAMETER_EXCEPTION.getCode(), "风险等级不存在");
        }
        boolean removed = riskLevelService.deleteRiskLevel(id);
        if (!removed) {
            return R.fail(ResponseCode.PARAMETER_EXCEPTION.getCode(), "风险等级不存在或已删除");
        }
        return R.ok().data(true);
    }

}
