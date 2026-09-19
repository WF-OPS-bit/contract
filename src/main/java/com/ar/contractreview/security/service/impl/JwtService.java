package com.ar.contractreview.security.service.impl;

import com.ar.contractreview.security.service.IJwtService;
import com.ar.contractreview.utils.StringUtils;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * @author xiaobobo
 * @title: JwtService
 * @projectName ar_26_springboot-parent
 * @description: Jwt的实现类
 * @date 2026/9/3  10:36
 */
@Service
@Slf4j
public class JwtService implements IJwtService {

    //下面两个参数 一般是配在yml中的
    //这个是过期时间
    @Value("${jwt.token.expire}")
    private Integer expire;

    //签名
    @Value("${jwt.token.sign}")
    private String sign;


    @Override
    public String createToken(Long userId, String username) {
        // 构建密钥（必须至少32字节）
        SecretKey secretKey = Keys.hmacShaKeyFor(sign.getBytes(StandardCharsets.UTF_8));

        // 调用API生成token
        String token = Jwts.builder()
                .id(String.valueOf(userId))                          // 设置id (替代 setId)
                .subject(username)                                   // 设置名字 (替代 setSubject)
                .expiration(DateTime.now().plusDays(expire).toDate())
                .signWith(secretKey, Jwts.SIG.HS256)                // 签名算法 (参数顺序变化)
                .compressWith(CompressionCodecs.GZIP)               // 压缩算法 (保持不变)
                .compact();

        log.info("生成的token是:{}", token);
        return token;
    }

    @Override
    public boolean verifyToken(String token) {
        try {
            // 方式1：使用 SecretKey 对象
            SecretKey key = Keys.hmacShaKeyFor(sign.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(key)  // 替代 setSigningKey()
                    .build()          // 构建解析器
                    .parseSignedClaims(token)  // 替代 parseClaimsJws()
                    .getPayload();    // 获取 Claims
            return true;
        }catch (Exception err){
            return false;
        }
    }

    @Override
    public Claims parse(String token) {
        // 方式1：使用 SecretKey 对象
        SecretKey key = Keys.hmacShaKeyFor(sign.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
                .verifyWith(key)  // 替代 setSigningKey()
                .build()          // 构建解析器
                .parseSignedClaims(token)  // 替代 parseClaimsJws()
                .getPayload();    // 获取 Claims
        return claims;
    }

    @Override
    public String refreshToken(String token) {
        //先拿到原来的用户id和用户名
        Long userId = getUserIdByToken(token);
        String username = getUsernameByToken(token);
        return createToken(userId,username);
    }

    @Override
    public Long getUserIdByToken(String token) {
        String userId = parse(token).getId();
        return Long.parseLong(userId);
    }

    @Override
    public String getUsernameByToken(String token) {
        return parse(token).getSubject();
    }

    @Override
    public String getToken(HttpServletRequest request) {
        //在这里需要拿到这个token
        String bearerToken = request.getHeader("Authorization");
        //然后将上面的数据进行解析
        if(!StringUtils.isEmpty(bearerToken) && bearerToken.startsWith("Bearer ")){
            //解析上面的数据
            return bearerToken.substring(7);
        }
        return null;
    }
}
