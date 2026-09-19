package com.ar.contractreview;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 合同智能审核系统 - 启动类
 *
 * @author System
 * @since 2026-09-03
 */
@SpringBootApplication
@EnableAsync          // 启用异步执行，支持 @Async 注解（备份功能需要）
@MapperScan("com.ar.contractreview.mapper")  // 扫描 Mapper 接口
public class ContractReviewApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContractReviewApplication.class, args);
    }
}

