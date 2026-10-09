package com.ar.contractreview.service.impl;

import com.ar.contractreview.entity.ContractContract;
import com.ar.contractreview.entity.ContractReviewResult;
import com.ar.contractreview.entity.ContractRiskClause;
import com.ar.contractreview.service.ContractContractService;
import com.ar.contractreview.service.ContractReviewResultService;
import com.ar.contractreview.service.ContractRiskClauseService;
import com.ar.contractreview.service.PythonAiClientService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * AI 审核结果的落库组件。
 * <p>
 * 单独拆成一个 Bean 而不是写在 {@link AiReviewServiceImpl} 里，是因为
 * {@code @Transactional} 依赖 Spring 代理，同类内部自调用不会走代理、事务不生效。
 * 写结果要「审核结果 + 风险条款 + 合同状态」三处一起成功，必须真事务。
 * </p>
 *
 * @author wyh
 */
@Service
public class AiReviewResultWriter {

    /** 合同状态：人工审核中 */
    private static final String STATUS_REVIEWING = "REVIEWING";

    @Autowired
    private ContractContractService contractService;
    @Autowired
    private ContractReviewResultService reviewResultService;
    @Autowired
    private ContractRiskClauseService riskClauseService;

    /**
     * 把 Python 的审核结果写进 contract_review_result 与 contract_risk_clause，
     * 并回写合同的风险等级/评分/状态。
     *
     * @param contract 合同实体（会被原地修改并更新）
     * @param result   Python 返回的审核数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void persist(ContractContract contract, PythonAiClientService.ReviewAnalyzeData result) {
        List<Map<String, Object>> riskItems = extractRiskItems(result.getRiskItems());

        int high = 0, medium = 0, low = 0;
        for (Map<String, Object> item : riskItems) {
            String level = str(item.get("riskLevel")).toUpperCase();
            switch (level) {
                case "HIGH", "CRITICAL" -> high++;
                case "MEDIUM" -> medium++;
                default -> low++;
            }
        }

        // contract_review_result 上有 uk_contract_id 唯一约束：一个合同只保留一份结果 → upsert
        ContractReviewResult r = reviewResultService.getOne(
                new LambdaQueryWrapper<ContractReviewResult>()
                        .eq(ContractReviewResult::getContractId, contract.getId())
                        .last("LIMIT 1"), false);
        boolean isNew = (r == null);
        if (isNew) {
            r = new ContractReviewResult();
            r.setContractId(contract.getId()).setCreatedTime(LocalDateTime.now());
        }

        Map<String, Object> comparison = asMap(result.getComparison());
        r.setRiskLevel(result.getRiskLevel())
                .setRiskScore(result.getRiskScore() == null ? 0 : result.getRiskScore())
                .setTotalRisks(riskItems.size())
                .setHighRisks(high).setMediumRisks(medium).setLowRisks(low)
                .setConclusion(result.getSummary())
                .setReportUrl("")
                .setMatchingCount(intOf(comparison.get("matchCount")))
                .setModifiedCount(intOf(comparison.get("modifyCount")))
                .setMissingCount(intOf(comparison.get("missingCount")))
                .setAddedCount(intOf(comparison.get("addedCount")))
                .setUpdatedTime(LocalDateTime.now());
        if (isNew) {
            reviewResultService.save(r);
        } else {
            reviewResultService.updateById(r);
        }

        // 风险条款：先清后插，保证同一合同不会残留上一次的风险
        riskClauseService.remove(new LambdaQueryWrapper<ContractRiskClause>()
                .eq(ContractRiskClause::getContractId, contract.getId()));
        for (Map<String, Object> item : riskItems) {
            int page = intOf(item.get("page"));
            ContractRiskClause rc = new ContractRiskClause()
                    .setContractId(contract.getId())
                    .setReviewResultId(r.getId())
                    .setClauseNo(str(item.get("clauseNo")))
                    .setClauseTitle(defaultIfBlank(str(item.get("clauseTitle")), "未命名条款"))
                    .setClauseContent(str(item.get("clauseContent")))
                    .setPage(page == 0 ? 1 : page)
                    .setRiskLevel(defaultIfBlank(str(item.get("riskLevel")), "LOW"))
                    .setRiskType(defaultIfBlank(str(item.get("riskType")), "AI识别风险"))
                    .setRiskDescription(str(item.get("riskDescription")))
                    .setSuggestion(str(item.get("suggestion")))
                    .setLegalBasis(str(item.get("legalBasis")))
                    .setStatus("PENDING")
                    .setCreatedTime(LocalDateTime.now()).setUpdatedTime(LocalDateTime.now());
            riskClauseService.save(rc);
        }

        // 回写合同：风险等级/评分 + 进入人工审核
        contract.setRiskLevel(result.getRiskLevel())
                .setRiskScore(r.getRiskScore())
                .setAiReviewTime(LocalDateTime.now())
                .setStatus(STATUS_REVIEWING)
                .setUpdatedTime(LocalDateTime.now());
        contractService.updateById(contract);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractRiskItems(Object raw) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (raw instanceof List<?> rawList) {
            for (Object o : rawList) {
                if (o instanceof Map<?, ?> m) {
                    list.add((Map<String, Object>) m);
                }
            }
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object raw) {
        return raw instanceof Map<?, ?> m ? (Map<String, Object>) m : Map.of();
    }

    private int intOf(Object v) {
        if (v == null) return 0;
        if (v instanceof Number n) return n.intValue();
        try {
            return (int) Double.parseDouble(v.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String str(Object v) {
        return v == null ? null : String.valueOf(v);
    }

    private String defaultIfBlank(String v, String fallback) {
        return StringUtils.hasText(v) ? v : fallback;
    }
}
