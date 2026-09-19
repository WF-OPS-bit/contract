package com.ar.contractreview.controller;

import com.ar.contractreview.entity.ContractReviewRule;
import com.ar.contractreview.security.dto.RuleSaveRequest;
import com.ar.contractreview.result.R;
import com.ar.contractreview.service.ContractReviewRuleService;
import com.ar.contractreview.vo.RuleVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
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

import java.util.List;

/**
 * <p>
 * 审核规则表 前端控制器
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
@RestController
@RequestMapping("/rules")
public class ContractReviewRuleController {

    @Autowired
    private ContractReviewRuleService contractReviewRuleService;

    /**
     * 11.1 获取规则列表（分页，支持多条件筛选）
     * GET /rules
     */
    @GetMapping
    public R list(@RequestParam(value = "page", defaultValue = "1") Integer page,
                  @RequestParam(value = "size", defaultValue = "10") Integer size,
                  @RequestParam(value = "ruleName", required = false) String ruleName,
                  @RequestParam(value = "ruleType", required = false) String ruleType,
                  @RequestParam(value = "contractType", required = false) String contractType,
                  @RequestParam(value = "riskLevel", required = false) String riskLevel,
                  @RequestParam(value = "status", required = false) Integer status) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }
        if (size > 100) {
            size = 100;
        }
        IPage<RuleVO> pageResult = contractReviewRuleService.pageRules(page, size,
                ruleName, ruleType, contractType, riskLevel, status);
        List<RuleVO> list = pageResult.getRecords();
        long total = pageResult.getTotal();
        return R.ok()
                .data("list", list)
                .data("total", total)
                .data("page", page)
                .data("size", size);
    }

    /**
     * 11.2 获取规则详情
     * GET /rules/{id}
     */
    @GetMapping("/{id}")
    public R detail(@PathVariable Long id) {
        RuleVO vo = contractReviewRuleService.getRuleDetail(id);
        return R.ok()
                .data("id", vo.getId())
                .data("ruleNo", vo.getRuleNo())
                .data("ruleName", vo.getRuleName())
                .data("ruleType", vo.getRuleType())
                .data("contractType", vo.getContractType())
                .data("clauseKeyword", vo.getClauseKeyword())
                .data("ruleConfig", vo.getRuleConfig())
                .data("riskLevel", vo.getRiskLevel())
                .data("priority", vo.getPriority())
                .data("status", vo.getStatus())
                .data("description", vo.getDescription());
    }

    /**
     * 11.3 新增规则
     * POST /rules
     */
    @PostMapping
    public R add(@RequestBody RuleSaveRequest request) {
        ContractReviewRule rule = contractReviewRuleService.addRule(request);
        return R.ok()
                .data("id", rule.getId())
                .data("ruleNo", rule.getRuleNo())
                .data("ruleName", rule.getRuleName())
                .data("status", rule.getStatus());
    }

    /**
     * 11.4 更新规则
     * PUT /rules/{id}
     */
    @PutMapping("/{id}")
    public R update(@PathVariable Long id, @RequestBody RuleSaveRequest request) {
        ContractReviewRule rule = contractReviewRuleService.updateRule(id, request);
        return R.ok()
                .data("id", rule.getId())
                .data("ruleName", rule.getRuleName())
                .data("status", rule.getStatus());
    }

    /**
     * 11.5 删除规则
     * DELETE /rules/{id}
     */
    @DeleteMapping("/{id}")
    public R delete(@PathVariable Long id) {
        contractReviewRuleService.deleteRule(id);
        return R.ok().data(true);
    }
}
