package com.ar.contractreview.service.impl;

import com.ar.contractreview.entity.ContractClause;
import com.ar.contractreview.entity.ContractContract;
import com.ar.contractreview.entity.ContractReviewResult;
import com.ar.contractreview.entity.ContractReviewRule;
import com.ar.contractreview.entity.ContractRiskClause;
import com.ar.contractreview.exception.BusinessException;
import com.ar.contractreview.exception.PythonAiException;
import com.ar.contractreview.result.ResponseCode;
import com.ar.contractreview.service.AiReviewService;
import com.ar.contractreview.service.ContractClauseService;
import com.ar.contractreview.service.ContractContractService;
import com.ar.contractreview.service.ContractReviewResultService;
import com.ar.contractreview.service.ContractReviewRuleService;
import com.ar.contractreview.service.ContractRiskClauseService;
import com.ar.contractreview.service.PythonAiClientService;
import com.ar.contractreview.utils.ThreadPoolUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 审核服务实现。
 * <p>
 * 完整链路：读合同文件 → Base64 → Python /ai/document/parse 解析条款
 * → 组装本地规则 + 条款库标准条款 → Python /ai/review/analyze 出风险
 * → 写 contract_review_result + contract_risk_clause → 回写合同风险等级/评分。
 * </p>
 *
 * @author wyh
 */
@Service
public class AiReviewServiceImpl implements AiReviewService {

    private static final Logger log = LoggerFactory.getLogger(AiReviewServiceImpl.class);

    /** 合同状态：AI 审核中 */
    private static final String STATUS_AI_REVIEWING = "AI_REVIEWING";

    @Autowired
    private ContractContractService contractService;
    @Autowired
    private ContractReviewResultService reviewResultService;
    @Autowired
    private ContractRiskClauseService riskClauseService;
    @Autowired
    private ContractReviewRuleService reviewRuleService;
    @Autowired
    private ContractClauseService clauseService;
    @Autowired
    private PythonAiClientService pythonAiClientService;
    @Autowired
    private AiReviewResultWriter resultWriter;

    /**
     * 合同文件落盘目录，与 ContractContractController / WebMVCConfig 保持一致
     */
    @Value("${contract.upload.dir:${user.dir}/upload/contracts/}")
    private String contractUploadDir;

    // =====================================================================
    // 6.1 触发审核
    // =====================================================================

    @Override
    public Map<String, Object> triggerReview(Long contractId) {
        ContractContract c = contractService.getById(contractId);
        if (c == null) {
            throw new BusinessException(ResponseCode.DATA_NOT_EXIST.getCode(), "合同不存在");
        }

        // 受理：先占住状态，避免用户连点触发多次审核
        c.setStatus(STATUS_AI_REVIEWING).setUpdatedTime(LocalDateTime.now());
        contractService.updateById(c);

        // 审核耗时不可控，异步执行；失败时把状态退回，不让合同卡在 AI_REVIEWING
        ThreadPoolUtils.getNotifyThreadPoolExecutor().execute(() -> {
            try {
                analyzeContract(contractId);
            } catch (Exception e) {
                log.error("AI 审核失败：contractId={}", contractId, e);
                markReviewFailed(contractId, e);
            }
        });

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("contractId", contractId);
        data.put("status", "PROCESSING");
        data.put("message", "AI审核已触发，预计5分钟内完成");
        return data;
    }

    // =====================================================================
    // 审核主流程
    // =====================================================================

    @Override
    public void analyzeContract(Long contractId) {
        ContractContract c = contractService.getById(contractId);
        if (c == null) {
            throw new BusinessException(ResponseCode.DATA_NOT_EXIST.getCode(), "合同不存在");
        }
        long startedAt = System.currentTimeMillis();

        // ---------- 1. 解析合同文档 ----------
        String parsedText = null;
        Object clauses = List.of();
        try {
            PythonAiClientService.DocumentParseData parseData = parseContractFile(c);
            if (parseData != null) {
                parsedText = parseData.getParsedText();
                clauses = parseData.getClauses() == null ? List.of() : parseData.getClauses();
            }
        } catch (Exception e) {
            // 解析失败不阻断流程：规则引擎对全文关键词的检查仍能给出结果
            log.warn("合同文档解析失败，降级为合同元数据文本：contractId={}, 原因={}", contractId, e.getMessage());
        }
        if (!StringUtils.hasText(parsedText)) {
            parsedText = buildFallbackText(c);
        }

        // ---------- 2. 组装审核规则 + 条款库标准条款 ----------
        List<Map<String, Object>> rules = loadRules(c.getContractType());
        List<Map<String, Object>> templateClauses = loadTemplateClauses(c.getContractType());

        log.info("开始 AI 审核：contractId={}, 规则 {} 条, 参考条款 {} 条, 文本 {} 字",
                contractId, rules.size(), templateClauses.size(), parsedText.length());

        // ---------- 3. 调用 Python 审核 ----------
        PythonAiClientService.AiApiResponse<PythonAiClientService.ReviewAnalyzeData> response =
                pythonAiClientService.reviewAnalyze(contractId, parsedText, clauses, rules, templateClauses);
        PythonAiClientService.ReviewAnalyzeData result = response == null ? null : response.getData();
        if (result == null) {
            throw new PythonAiException("Python AI 服务未返回审核结果");
        }

        // ---------- 4. 落库（独立 Bean，走真事务）----------
        resultWriter.persist(c, result);

        log.info("AI 审核完成：contractId={}, 评分={}, 等级={}, 风险 {} 条, 耗时 {}ms",
                contractId, result.getRiskScore(), result.getRiskLevel(),
                result.getRiskItems() instanceof List<?> l ? l.size() : 0,
                System.currentTimeMillis() - startedAt);
    }

    // =====================================================================
    // 6.2 查询审核结果
    // =====================================================================

    @Override
    public Map<String, Object> getReviewResult(Long contractId) {
        ContractContract c = contractService.getById(contractId);
        if (c == null) {
            throw new BusinessException(ResponseCode.DATA_NOT_EXIST.getCode(), "合同不存在");
        }

        ContractReviewResult r = reviewResultService.getOne(
                new LambdaQueryWrapper<ContractReviewResult>()
                        .eq(ContractReviewResult::getContractId, contractId)
                        .last("LIMIT 1"), false);

        List<ContractRiskClause> risks = riskClauseService.list(
                new LambdaQueryWrapper<ContractRiskClause>()
                        .eq(ContractRiskClause::getContractId, contractId)
                        .orderByAsc(ContractRiskClause::getId));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("contractId", contractId);
        // 前端用它区分「还没跑过 AI 审核」和「审核结果是 0 风险」，避免把空态渲染成无风险
        data.put("hasResult", r != null);
        data.put("riskLevel", r != null ? r.getRiskLevel() : c.getRiskLevel());
        data.put("riskScore", r != null ? r.getRiskScore() : c.getRiskScore());

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalRisks", r == null ? risks.size() : nvl(r.getTotalRisks(), risks.size()));
        summary.put("highRisks", r == null ? countByLevel(risks, "HIGH") : nvl(r.getHighRisks(), 0));
        summary.put("mediumRisks", r == null ? countByLevel(risks, "MEDIUM") : nvl(r.getMediumRisks(), 0));
        summary.put("lowRisks", r == null ? countByLevel(risks, "LOW") : nvl(r.getLowRisks(), 0));
        data.put("summary", summary);

        Map<String, Object> keyInfo = new LinkedHashMap<>();
        keyInfo.put("partyA", c.getPartyA());
        keyInfo.put("partyB", c.getPartyB());
        keyInfo.put("amount", c.getAmount());
        keyInfo.put("signDate", c.getSignDate() == null ? null : c.getSignDate().toString());
        data.put("keyInfo", keyInfo);

        List<Map<String, Object>> riskList = new ArrayList<>();
        for (ContractRiskClause rc : risks) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", rc.getId());
            item.put("clauseNo", rc.getClauseNo());
            item.put("clauseTitle", rc.getClauseTitle());
            item.put("clauseContent", rc.getClauseContent());
            item.put("page", rc.getPage());
            item.put("riskLevel", rc.getRiskLevel());
            item.put("riskType", rc.getRiskType());
            item.put("riskDescription", rc.getRiskDescription());
            item.put("suggestion", rc.getSuggestion());
            item.put("legalBasis", rc.getLegalBasis());
            item.put("status", rc.getStatus());
            riskList.add(item);
        }
        data.put("risks", riskList);

        // 《接口文档》6.2 给前端的字段是 matching/modified/missing/added，
        // 而库里（和 Python 侧）是 matchingCount/modifiedCount/... ，这里做一次改名映射
        Map<String, Object> comparison = new LinkedHashMap<>();
        comparison.put("matching", r == null ? 0 : nvl(r.getMatchingCount(), 0));
        comparison.put("modified", r == null ? 0 : nvl(r.getModifiedCount(), 0));
        comparison.put("missing", r == null ? 0 : nvl(r.getMissingCount(), 0));
        comparison.put("added", r == null ? 0 : nvl(r.getAddedCount(), 0));
        data.put("comparison", comparison);

        data.put("conclusion", r == null ? null : r.getConclusion());
        data.put("reportUrl", r == null ? "" : r.getReportUrl());
        return data;
    }

    // =====================================================================
    // 内部方法
    // =====================================================================

    /**
     * 读取磁盘上的合同文件并交给 Python 解析。
     * 文件不存在时返回 null，由调用方降级处理（种子数据里的合同没有真实文件）。
     */
    private PythonAiClientService.DocumentParseData parseContractFile(ContractContract c) throws Exception {
        String fileUrl = c.getFileUrl();
        if (!StringUtils.hasText(fileUrl)) return null;

        String filename = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
        Path path = Path.of(contractUploadDir, filename);
        if (!Files.exists(path)) {
            log.warn("合同文件不存在，跳过解析：{}", path);
            return null;
        }

        byte[] bytes = Files.readAllBytes(path);
        String fileType = StringUtils.hasText(c.getFileType())
                ? c.getFileType()
                : filename.substring(filename.lastIndexOf('.') + 1);

        String base64 = Base64.getEncoder().encodeToString(bytes);
        PythonAiClientService.AiApiResponse<PythonAiClientService.DocumentParseData> resp =
                pythonAiClientService.parseDocument(base64, fileType, c.getId());
        return resp == null ? null : resp.getData();
    }

    /**
     * 没有合同文件时的兜底文本：用合同元数据拼一段，让规则引擎至少能跑起来。
     */
    private String buildFallbackText(ContractContract c) {
        return "合同名称：" + nvlStr(c.getContractName())
                + "\n合同编号：" + nvlStr(c.getContractNo())
                + "\n合同类型：" + nvlStr(c.getContractType())
                + "\n甲方：" + nvlStr(c.getPartyA())
                + "\n乙方：" + nvlStr(c.getPartyB())
                + "\n合同金额：" + (c.getAmount() == null ? "" : c.getAmount().toPlainString())
                + "\n签订日期：" + (c.getSignDate() == null ? "" : c.getSignDate());
    }

    /**
     * 取启用中的规则；合同类型为空的规则对所有类型生效。
     */
    private List<Map<String, Object>> loadRules(String contractType) {
        List<ContractReviewRule> list = reviewRuleService.list(
                new LambdaQueryWrapper<ContractReviewRule>()
                        .eq(ContractReviewRule::getStatus, 1)
                        .and(w -> w.isNull(ContractReviewRule::getContractType)
                                .or().eq(ContractReviewRule::getContractType, "")
                                .or().eq(StringUtils.hasText(contractType),
                                        ContractReviewRule::getContractType, contractType))
                        .orderByAsc(ContractReviewRule::getPriority));

        List<Map<String, Object>> rules = new ArrayList<>();
        for (ContractReviewRule r : list) {
            Map<String, Object> m = new LinkedHashMap<>();
            // 字段名必须和 Python schemas.RuleIn 对齐（camelCase），否则会被静默丢弃
            m.put("name", r.getRuleName());
            m.put("keyword", r.getClauseKeyword());
            // Python 侧用 riskType 承载「规则类型」：REQUIRED / VALUE_RANGE / REGEX / TEXT_CHECK
            m.put("riskType", r.getRuleType());
            m.put("riskLevel", r.getRiskLevel());
            m.put("config", r.getRuleConfig());
            m.put("priority", r.getPriority() == null ? 100 : r.getPriority());
            m.put("description", r.getDescription());
            rules.add(m);
        }
        return rules;
    }

    /**
     * 取条款库里的标准条款作为比对基准。
     */
    private List<Map<String, Object>> loadTemplateClauses(String contractType) {
        List<ContractClause> list = clauseService.list(
                new LambdaQueryWrapper<ContractClause>()
                        .eq(ContractClause::getStatus, 1)
                        .and(w -> w.isNull(ContractClause::getContractType)
                                .or().eq(ContractClause::getContractType, "")
                                .or().eq(StringUtils.hasText(contractType),
                                        ContractClause::getContractType, contractType))
                        .orderByAsc(ContractClause::getSortOrder));

        List<Map<String, Object>> clauses = new ArrayList<>();
        for (ContractClause cl : list) {
            Map<String, Object> m = new LinkedHashMap<>();
            // 对应 Python schemas.ClauseIn：clauseNumber / title / content
            m.put("clauseNumber", cl.getClauseCode());
            m.put("title", cl.getClauseName());
            m.put("content", StringUtils.hasText(cl.getStandardContent()) ? cl.getStandardContent() : cl.getContent());
            clauses.add(m);
        }
        return clauses;
    }

    /**
     * 审核失败时把合同从 AI_REVIEWING 退回 UPLOADED，允许用户重新触发。
     */
    private void markReviewFailed(Long contractId, Exception e) {
        try {
            ContractContract c = contractService.getById(contractId);
            if (c == null) return;
            c.setStatus("UPLOADED")
                    .setReviewComment("AI审核失败：" + e.getMessage())
                    .setUpdatedTime(LocalDateTime.now());
            contractService.updateById(c);
        } catch (Exception ignore) {
            log.warn("回退合同状态失败：contractId={}", contractId);
        }
    }

    private int countByLevel(List<ContractRiskClause> risks, String level) {
        int n = 0;
        for (ContractRiskClause r : risks) {
            if (level.equalsIgnoreCase(r.getRiskLevel())) n++;
        }
        return n;
    }

    private Integer nvl(Integer v, int fallback) {
        return v == null ? fallback : v;
    }

    private String nvlStr(String v) {
        return v == null ? "" : v;
    }
}
