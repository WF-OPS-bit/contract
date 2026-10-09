package com.ar.contractreview.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.util.List;

/**
 * @author wyh
 * @title: WebMVCConfig
 * @projectName ar_26_springboot-parent
 * @description: WebMvc 静态资源映射（模板文件、合同文件下载）
 * @date 2026/8/25  15:17
 */
@SpringBootConfiguration
@EnableWebMvc
public class WebMVCConfig implements WebMvcConfigurer {

    /**
     * 模板文件上传目录（与 ContractTemplateController 保持一致）
     */
    @Value("${template.upload.dir:${user.dir}/upload/templates/}")
    private String templateUploadDir;

    /**
     * 合同文件上传目录（与 ContractContractController 保持一致）
     */
    @Value("${contract.upload.dir:${user.dir}/upload/contracts/}")
    private String contractUploadDir;

    /**
     * Spring Boot 自动配置好的 ObjectMapper（含 JavaTimeModule，日期序列化为 ISO 字符串）。
     */
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {

    }

    /**
     * 把 Jackson 转换器换成使用 Spring Boot 自动配置的 ObjectMapper。
     * <p>
     * ⚠️ 类上的 {@code @EnableWebMvc} 会关闭 Spring Boot 的 WebMvcAutoConfiguration，
     * 于是 {@link org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport}
     * 会自己 new 一个 Jackson2ObjectMapperBuilder，**拿不到** Spring Boot 的定制
     * （spring.jackson.* 全部失效）。后果是 LocalDate/LocalDateTime 被序列化成
     * {@code [2026,7,1]} 这样的数组，前端拿到的日期完全不可用。
     * 这里显式替换成上下文里的 ObjectMapper 即可修好，同时让 REST 层与
     * PythonAiClientService 用的是同一个配置。
     * </p>
     */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter jacksonConverter) {
                jacksonConverter.setObjectMapper(objectMapper);
            }
        }
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 9.3 上传的模板文件统一放在运行目录 upload/templates/ 下,
        // 通过 /api/upload/** 可直接访问下载(如 http://localhost:8081/api/upload/templates/xxx.docx)
        registry.addResourceHandler("/upload/**")
                .addResourceLocations(toFileLocation(templateUploadDir));

        // 5.1 上传的合同文件通过 /api/files/contracts/** 访问下载，
        // 与 ContractContractController 落库的 fileUrl("/files/contracts/{uuid}.{ext}") 对应。
        // 之前缺失这条映射，导致合同详情页的“下载合同”必然 404。
        registry.addResourceHandler("/files/contracts/**")
                .addResourceLocations(toFileLocation(contractUploadDir));
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

    }

    /**
     * 把目录路径转成 Spring 资源定位串，统一补上 file: 前缀和结尾斜杠，
     * 避免配置里少写斜杠导致拼出的路径非法。
     */
    private String toFileLocation(String dir) {
        if (!StringUtils.hasText(dir)) return "file:./";
        String normalized = dir.replace(File.separatorChar, '/');
        if (!normalized.endsWith("/")) normalized = normalized + "/";
        return "file:" + normalized;
    }
}
