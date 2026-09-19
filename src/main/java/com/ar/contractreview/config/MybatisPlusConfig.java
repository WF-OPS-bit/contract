package com.ar.contractreview.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * MyBatis-Plus 配置
 * <p>
 * 注册分页插件：MP 的 page() 分页查询依赖 PaginationInnerInterceptor，
 * 不配置时 selectPage 不会真正分页（列表接口会查出全量数据或报错）。
 * 注意：MP 3.5.9+ 起分页插件在 mybatis-plus-jsqlparser 模块中（pom 已引入）。
 * </p>
 *
 * @author wyh
 */
@SpringBootConfiguration
public class MybatisPlusConfig {

    /**
     * MP 拦截器：加入 MySQL 分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
