package com.ar.contractreview.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Python AI 服务（FastAPI）的 HTTP 客户端配置。
 * <p>
 * 只被 Java 后端内部调用，前端不直连。超时是必须显式设置的：
 * 合同文本动辄数万字，LLM 审核耗时较长，读超时给足；但连接超时必须短，
 * 否则 Python 服务没启动时请求会长时间挂起。
 * </p>
 */
@Configuration
public class PythonAiClientConfig {

    @Bean
    public RestClient pythonAiRestClient(
            @Value("${python.ai.base-url:http://localhost:8000}") String baseUrl,
            @Value("${python.ai.connect-timeout-seconds:5}") int connectTimeoutSeconds,
            @Value("${python.ai.read-timeout-seconds:300}") int readTimeoutSeconds) {

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(connectTimeoutSeconds));
        factory.setReadTimeout(Duration.ofSeconds(readTimeoutSeconds));

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
