package com.ar.contractreview.config;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
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
    public void addInterceptors(InterceptorRegistry registry) {

    }
}
