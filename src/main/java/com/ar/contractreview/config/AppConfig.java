package com.ar.contractreview.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * @author wyh
 * @title: AppConfig
 * @projectName ar_26_springboot-parent
 * @description: TODO
 * @date 2026/8/25  14:56
 */
@SpringBootConfiguration
@ComponentScan(basePackages = {"com.ar.contractreview"})
@EnableAspectJAutoProxy    //使用AOP自动代理
@EnableTransactionManagement  //开始事务注解
@MapperScan(basePackages = {"com.ar.contractreview.mapper", "com.ar.contractreview.security.mapper"})
public class AppConfig {
}
