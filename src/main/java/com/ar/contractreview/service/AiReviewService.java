package com.ar.contractreview.service;

import java.util.Map;

/**
 * <p>
 * AI 审核服务：把 Java 侧的数据（合同文件、审核规则、条款库）喂给 Python AI 服务，
 * 再把结果落回合同审核结果表 + 风险条款表。
 * </p>
 * <p>
 * 对应《接口文档》第 6 章：
 * <ul>
 *   <li>6.1 POST /ai/review/{contractId} —— 触发 AI 审核</li>
 *   <li>6.2 GET  /ai/review/result/{contractId} —— 获取审核结果</li>
 * </ul>
 * </p>
 *
 * @author wyh
 */
public interface AiReviewService {

    /**
     * 6.1 触发 AI 审核。
     * <p>
     * 审核本身耗时较长（真实 LLM 模式下可能数十秒），因此这里只做「受理」：
     * 把合同状态置为 AI_REVIEWING，然后丢到线程池异步执行，立即返回 PROCESSING。
     * </p>
     *
     * @param contractId 合同ID
     * @return {contractId, status, message}
     */
    Map<String, Object> triggerReview(Long contractId);

    /**
     * 6.2 获取 AI 审核结果（前端合同详情页「AI审核」标签页用）。
     *
     * @param contractId 合同ID
     * @return 完全对齐《接口文档》6.2 的响应结构
     */
    Map<String, Object> getReviewResult(Long contractId);

    /**
     * 同步执行一次 AI 审核（异步任务体，也可被测试直接调用）。
     *
     * @param contractId 合同ID
     */
    void analyzeContract(Long contractId);
}
