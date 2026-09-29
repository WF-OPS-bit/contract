package com.ar.contractreview.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.Map;

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
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            (req, res) -> {
                                throw new RestClientException("Python AI service call failed: " + res.getStatusText());
                            })
                    .body(Map.class);

            if (body == null) {
                throw new RestClientException("Python AI service returned empty response");
            }

            Integer code = body.get("code") == null ? null : Integer.valueOf(body.get("code").toString());
            String message = body.get("message") == null ? null : body.get("message").toString();
            Object rawData = body.get("data");

            T data = rawData == null ? null : objectMapper.convertValue(rawData, dataType);
            return new AiApiResponse<>(code, message, data);
        } catch (Exception e) {
            throw new RuntimeException("调用 Python AI 服务失败: " + uri, e);
        }
    }

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

    public static class DocumentParseData {
        private String parsedText;
        private Object entities;
        private Object clauses;
        private Integer pageCount;
        private Boolean textTruncated;

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

    public static class ReviewAnalyzeData {
        private Integer riskScore;
        private String riskLevel;
        private String summary;
        private Object riskItems;
        private Object comparison;
        private String analyzedBy;
        private Long durationMs;

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

    public static class RiskStatisticsData {
        private Object riskDistribution;
        private Object levelDistribution;
        private Object trendData;
        private Object analysisResult;

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
