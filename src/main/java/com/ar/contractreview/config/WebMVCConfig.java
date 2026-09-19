package com.ar.contractreview.config;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * @author wyh
 * @title: WebMVCConfig
 * @projectName ar_26_springboot-parent
 * @description: TODO
 * @date 2026/8/25  15:17
 */
@SpringBootConfiguration
@EnableWebMvc
public class WebMVCConfig implements WebMvcConfigurer {

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {

    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 9.3 上传的模板文件统一放在运行目录 upload/ 下,
        // 通过 /api/upload/** 可直接访问下载(如 http://localhost:8081/api/upload/templates/xxx.docx)
        String uploadDir = System.getProperty("user.dir") + "/upload/";
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:" + uploadDir);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

    }
}
