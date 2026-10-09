package com.ar.contractreview.controller;

import com.ar.contractreview.result.R;
import com.ar.contractreview.service.AiReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 合同审核结果表 前端控制器 —— 《接口文档》第 6 章「AI审核接口」
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
@RestController
@RequestMapping("/ai/review")
public class ContractReviewResultController {

    @Autowired
    private AiReviewService aiReviewService;

    /**
     * 6.1 触发AI审核
     * <p>
     * POST /api/ai/review/{contractId}<br>
     * 合同上传后由后台触发，或用户在合同详情页点「重新审核」。
     * 审核异步执行，本接口只返回受理结果 PROCESSING。
     * </p>
     *
     * @param contractId 合同ID
     * @return {contractId, status, message}
     */
    @PostMapping("/{contractId}")
    public R trigger(@PathVariable("contractId") Long contractId) {
        return R.ok().data(aiReviewService.triggerReview(contractId));
    }

    /**
     * 6.2 获取审核结果
     * <p>
     * GET /api/ai/review/result/{contractId}<br>
     * 合同详情页「AI审核」标签页加载时调用，返回风险汇总、关键信息、
     * 风险条款列表以及条款比对统计。
     * </p>
     *
     * @param contractId 合同ID
     * @return 对齐《接口文档》6.2 的结果结构
     */
    @GetMapping("/result/{contractId}")
    public R result(@PathVariable("contractId") Long contractId) {
        return R.ok().data(aiReviewService.getReviewResult(contractId));
    }
}
