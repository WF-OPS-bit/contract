package com.ar.contractreview.security.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author xiaobobo
 * @title: JwtProperties
 * @projectName ar_26_springboot-parent
 * @description: TODO
 * @date 2026/9/3  10:39
 */
@ConfigurationProperties(prefix = "jwt.token")
@Data
public class JwtProperties {

    //过期时间
    private Integer expire=30;  //默认30天过期

    //签名
    private String sign;      //JWT生成的时候的签名

}
