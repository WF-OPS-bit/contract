package com.ar.mp;


import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;
import com.baomidou.mybatisplus.generator.fill.Column;
import org.junit.Test;

import java.util.Collections;

public class CodeGenerator {
    
    @Test
    public void run() {
        FastAutoGenerator.create(
                "jdbc:mysql://127.0.0.1:3306/contract_review?useUnicode=true&characterEncoding=UTF-8&serverTimezone=GMT%2B8",
                "root",
                "root"
            )
            // 全局配置
            .globalConfig(builder -> {
                builder.author("wyh")           // 设置作者
                        //下面改的是 代码的生成位置
                       .outputDir("D:\\艾瑞3.0\\小队项目\\contract-approval-system\\src\\main\\java"); // 指定输出目录
            })
            // 包配置
            .packageConfig(builder -> {
                builder.parent("com.ar") // 设置父包名(整个根包的报名)
                       .moduleName("contractreview")      // 设置父包模块名
                       .pathInfo(Collections.singletonMap(OutputFile.xml, "D:\\艾瑞3.0\\小队项目\\contract-approval-system\\src\\main\\resources\\mapper")); // 设置mapperXml生成路径
            })
            // 策略配置
                .strategyConfig(builder -> {
                    builder.addInclude("sys_menu")
                            .addInclude("sys_role")
                            .addInclude("sys_user")
                            .addInclude("sys_role_menu")
                            .addInclude("sys_config")
                            .addInclude("sys_backup")
                            .addInclude("sys_operation_log")
                            .addInclude("contract_contract")
                            .addInclude("contract_review_result")
                            .addInclude("contract_risk_clause")
                            .addInclude("contract_comment")
                            .addInclude("contract_archive")
                            .addInclude("contract_template")
                            .addInclude("contract_clause")
                            .addInclude("contract_review_rule")
                            .addInclude("contract_risk_level")
                            .addInclude("process_config")
                            .addInclude("process_node")
                            .addInclude("process_instance")
                            .addInclude("process_task")
                            .addInclude("process_task_assign")
                            .addInclude("integration_oa")
                            .addInclude("integration_oa_log")
                            .addInclude("integration_sign")
                            .addInclude("integration_sign_record")
                            .addInclude("integration_api")
                            .addInclude("integration_api_log")
                           // Entity 策略配置
                           .entityBuilder()
                           .fileOverride()  // 覆盖已存在的文件
                           .enableLombok()                  // 开启 Lombok
                           .enableChainModel()              // 开启链式模型
                           .enableTableFieldAnnotation()    // 开启字段注解
                           .naming(NamingStrategy.underline_to_camel)    // 数据库表映射到实体的命名策略
                           .columnNaming(NamingStrategy.underline_to_camel) // 数据库表字段映射到实体的命名策略
                           .idType(IdType.AUTO)    // 主键策略
                           .logicDeleteColumnName("deleted") // 逻辑删除字段名
                           .versionColumnName("version")     // 乐观锁字段名
                           .addTableFills(
                               new Column("createTime", FieldFill.INSERT),
                               new Column("updateTime", FieldFill.INSERT_UPDATE)
                           )
                       
                       // Controller 策略配置
                       .controllerBuilder()
                           .fileOverride()
                       .enableRestStyle()               // 开启生成@RestController 控制器
                       .enableHyphenStyle()             // 开启驼峰转连字符
                       
                       // Service 策略配置
                       .serviceBuilder()
                           .fileOverride()
                       .formatServiceFileName("%sService") // 格式化 service 接口文件名称
                       .formatServiceImplFileName("%sServiceImpl") // 格式化 service 实现类文件名称
                       
                       // Mapper 策略配置
                       .mapperBuilder()
                           .fileOverride()
                       .enableBaseResultMap()           // 启用 BaseResultMap 生成
                       .enableBaseColumnList();         // 启用 BaseColumnList
            })
            // 使用 Velocity 引擎模板（对应你的依赖）
            .templateEngine(new VelocityTemplateEngine())
            .execute();
    }
}