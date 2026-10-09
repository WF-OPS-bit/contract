package com.ar.contractreview.service;

import com.ar.contractreview.exception.PythonAiException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Python AI 服务（FastAPI）客户端。
 * <p>
 * 契约见 python-ai-service/README.md 与《接口文档》第 6 章：
 * 统一响应信封 {@code {code, message, data}}，成功 code=200；
 * 失败时 Python 返回 HTTP 4xx/5xx，但响应体里依然是同一个信封，
 * 因此这里必须把错误响应体读出来，才能把 Python 的 message 透传给前端，
 * 而不是笼统地抛一句“调用失败”。
 * </p>
 */
@Service
public class PythonAiClientService {

    private final RestClient pythonAiRestClient;
    private final ObjectMapper objectMapper;

    public PythonAiClientService(RestClient pythonAiRestClient, ObjectMapper objectMapper) {
        this.pythonAiRestClient = pythonAiRestClient;
        this.objectMapper = objectMapper;
    }

    public AiApiResponse<DocumentParseData> parseDocument(String fileContent, String fileType, Long contractId) {
        Map<String, Object> request = new HashMap<>();
        request.put("fileContent", fileContent);
        request.put("fileType", fileType);
        request.put("contractId", contractId);
        return post("/ai/document/parse", request, DocumentParseData.class);
    }

    public AiApiResponse<ReviewAnalyzeData> reviewAnalyze(
            Long contractId,
            String parsedText,
            Object clauses,
            Object rules,
            Object templateClauses
    ) {
        Map<String, Object> request = new HashMap<>();
        request.put("contractId", contractId);
        request.put("parsedText", parsedText);
        request.put("clauses", clauses);
        request.put("rules", rules);
        request.put("templateClauses", templateClauses);
        return post("/ai/review/analyze", request, ReviewAnalyzeData.class);
    }

    public AiApiResponse<ClauseCompareData> clauseCompare(Object contractClauses, Object templateClauses) {
        Map<String, Object> request = new HashMap<>();
        request.put("contractClauses", contractClauses);
        request.put("templateClauses", templateClauses);
        return post("/ai/clause/compare", request, ClauseCompareData.class);
    }

    public AiApiResponse<RiskStatisticsData> riskStatistics(Object riskData) {
        Map<String, Object> request = new HashMap<>();
        request.put("riskData", riskData);
        return post("/ai/statistics/risk", request, RiskStatisticsData.class);
    }

    private <T> AiApiResponse<T> post(String uri, Object requestBody, Class<T> dataType) {
        try {
            Map<String, Object> body = pythonAiRestClient.post()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .onStatus(status -> status.isError(), (req, res) -> {
                        // Python 出错时也返回 {code,message,data} 信封，把 message 取出来透传，
                        // 否则排查问题只能看到一句 “500 Internal Server Error”。
                        String detail = readErrorMessage(res.getBody());
                        throw new PythonAiException(
                                "Python AI 服务返回错误：" + detail,
                                res.getStatusCode().value(), null, null);
                    })
                    .body(Map.class);

            if (body == null) {
                throw new PythonAiException("Python AI 服务返回空响应: " + uri);
            }

            Integer code = body.get("code") == null ? null : Integer.valueOf(body.get("code").toString());
            String message = body.get("message") == null ? null : body.get("message").toString();
            Object rawData = body.get("data");

            // 业务码非 200 时（Python 用 HTTP 200 但 code 表示失败的场景）也要报错，避免上层拿到半个结果
            if (code != null && code != 200) {
                throw new PythonAiException("Python AI 服务业务异常: " + message, 200, code, null);
            }

            T data = rawData == null ? null : objectMapper.convertValue(rawData, dataType);
            return new AiApiResponse<>(code, message, data);
        } catch (PythonAiException e) {
            throw e;
        } catch (RestClientException e) {
            throw new PythonAiException(
                    "调用 Python AI 服务失败(" + uri + ")，请确认 Python 服务已启动: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new PythonAiException("调用 Python AI 服务失败: " + uri + "，原因: " + e.getMessage(), e);
        }
    }

    /**
     * 从 Python 的错误响应体里抽出可读的错误信息。
     * 优先取信封里的 message，其次把 data.detail 拼上，最后兜底为原始文本截断。
     */
    private String readErrorMessage(InputStream errorBody) {
        try {
            if (errorBody == null) return "无响应体";
            String text = new String(errorBody.readAllBytes(), StandardCharsets.UTF_8);
            if (text.isEmpty()) return "无响应体";
            Map<?, ?> parsed = objectMapper.readValue(text, Map.class);
            Object message = parsed.get("message");
            Object data = parsed.get("data");
            String detail = null;
            if (data instanceof Map<?, ?> dataMap) {
                Object d = dataMap.get("detail");
                if (d != null) detail = String.valueOf(d);
            }
            String result = String.valueOf(message == null ? text : message);
            return detail == null ? result : result + "（" + detail + "）";
        } catch (Exception ignore) {
            return "无法解析错误响应体";
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AiApiResponse<T> {
        private Integer code;
        private String message;
        private T data;

        public AiApiResponse() {
        }

        public AiApiResponse(Integer code, String message, T data) {
            this.code = code;
            this.message = message;
            this.data = data;
        }

        public Integer getCode() {
            return code;
        }

        public void setCode(Integer code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public T getData() {
            return data;
        }

        public void setData(T data) {
            this.data = data;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DocumentParseData {
        private String parsedText;
        private Object entities;
        private Object clauses;
        private Integer pageCount;
        private Boolean textTruncated;
        /** 解析方式：local / offline（Python 侧固定为 local，纯本地解析不调模型） */
        private String parseMode;

        public String getParseMode() {
            return parseMode;
        }

        public void setParseMode(String parseMode) {
            this.parseMode = parseMode;
        }

        public String getParsedText() {
            return parsedText;
        }

        public void setParsedText(String parsedText) {
            this.parsedText = parsedText;
        }

        public Object getEntities() {
            return entities;
        }

        public void setEntities(Object entities) {
            this.entities = entities;
        }

        public Object getClauses() {
            return clauses;
        }

        public void setClauses(Object clauses) {
            this.clauses = clauses;
        }

        public Integer getPageCount() {
            return pageCount;
        }

        public void setPageCount(Integer pageCount) {
            this.pageCount = pageCount;
        }

        public Boolean getTextTruncated() {
            return textTruncated;
        }

        public void setTextTruncated(Boolean textTruncated) {
            this.textTruncated = textTruncated;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ReviewAnalyzeData {
        private Integer riskScore;
        private String riskLevel;
        private String summary;
        private Object riskItems;
        private Object comparison;
        private String analyzedBy;
        private Long durationMs;
        /** 参与审核的条款总数 */
        private Integer totalClauses;

        public Integer getTotalClauses() {
            return totalClauses;
        }

        public void setTotalClauses(Integer totalClauses) {
            this.totalClauses = totalClauses;
        }

        public Integer getRiskScore() {
            return riskScore;
        }

        public void setRiskScore(Integer riskScore) {
            this.riskScore = riskScore;
        }

        public String getRiskLevel() {
            return riskLevel;
        }

        public void setRiskLevel(String riskLevel) {
            this.riskLevel = riskLevel;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public Object getRiskItems() {
            return riskItems;
        }

        public void setRiskItems(Object riskItems) {
            this.riskItems = riskItems;
        }

        public Object getComparison() {
            return comparison;
        }

        public void setComparison(Object comparison) {
            this.comparison = comparison;
        }

        public String getAnalyzedBy() {
            return analyzedBy;
        }

        public void setAnalyzedBy(String analyzedBy) {
            this.analyzedBy = analyzedBy;
        }

        public Long getDurationMs() {
            return durationMs;
        }

        public void setDurationMs(Long durationMs) {
            this.durationMs = durationMs;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ClauseCompareData {
        private Integer matchCount;
        private Integer modifyCount;
        private Integer missingCount;
        private Integer addedCount;
        private Object differences;
        private Object suggestions;
        private String comparedBy;

        public Integer getMatchCount() {
            return matchCount;
        }

        public void setMatchCount(Integer matchCount) {
            this.matchCount = matchCount;
        }

        public Integer getModifyCount() {
            return modifyCount;
        }

        public void setModifyCount(Integer modifyCount) {
            this.modifyCount = modifyCount;
        }

        public Integer getMissingCount() {
            return missingCount;
        }

        public void setMissingCount(Integer missingCount) {
            this.missingCount = missingCount;
        }

        public Integer getAddedCount() {
            return addedCount;
        }

        public void setAddedCount(Integer addedCount) {
            this.addedCount = addedCount;
        }

        public Object getDifferences() {
            return differences;
        }

        public void setDifferences(Object differences) {
            this.differences = differences;
        }

        public Object getSuggestions() {
            return suggestions;
        }

        public void setSuggestions(Object suggestions) {
            this.suggestions = suggestions;
        }

        public String getComparedBy() {
            return comparedBy;
        }

        public void setComparedBy(String comparedBy) {
            this.comparedBy = comparedBy;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RiskStatisticsData {
        private Object riskDistribution;
        private Object levelDistribution;
        private Object trendData;
        private Object analysisResult;
        /** 统计来源：LLM / LOCAL */
        private String analyzedBy;

        public String getAnalyzedBy() {
            return analyzedBy;
        }

        public void setAnalyzedBy(String analyzedBy) {
            this.analyzedBy = analyzedBy;
        }

        public Object getRiskDistribution() {
            return riskDistribution;
        }

        public void setRiskDistribution(Object riskDistribution) {
            this.riskDistribution = riskDistribution;
        }

        public Object getLevelDistribution() {
            return levelDistribution;
        }

        public void setLevelDistribution(Object levelDistribution) {
            this.levelDistribution = levelDistribution;
        }

        public Object getTrendData() {
            return trendData;
        }

        public void setTrendData(Object trendData) {
            this.trendData = trendData;
        }

        public Object getAnalysisResult() {
            return analysisResult;
        }

        public void setAnalysisResult(Object analysisResult) {
            this.analysisResult = analysisResult;
        }
    }
}
