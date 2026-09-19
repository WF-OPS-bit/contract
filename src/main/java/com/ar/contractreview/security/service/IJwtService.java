package com.ar.contractreview.security.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author xiaobobo
 * @title: IJwtService
 * @projectName ar_26_springboot-parent
 * @description: Jwt相关的操作
 * @date 2026/9/3  10:32
 */
public interface IJwtService {


    /**
     * 通过用户名和用户id创建token
     * @param userId
     * @param username
     * @return
     */
    String createToken(Long userId,String username);

    /**
     * 校验token的合法性
     * @param token
     * @return
     */
    boolean verifyToken(String token);

    /**
     * 解析token
     * @param token
     * @return
     */
    Claims parse(String token);


    /**
     * 刷新token
     * @param token
     * @return
     */
    String refreshToken(String token);


    /**
     * 通过token拿到用户的id信息
     * @param token
     * @return
     */
    Long getUserIdByToken(String token);


    /**
     * 童女过token拿到用户名字
     * @param token
     * @return
     */
    String getUsernameByToken(String token);


    /**
     * 通过请求拿到这个token
     * @param request
     * @return
     */
    String getToken(HttpServletRequest request);


}
