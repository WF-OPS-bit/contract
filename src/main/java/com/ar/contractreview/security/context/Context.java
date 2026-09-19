package com.ar.contractreview.security.context;

import org.springframework.security.core.GrantedAuthority;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xiaobobo
 * @title: Context
 * @projectName ar_26_springboot-parent
 * @description: TODO
 * @date 2026/9/3  11:09
 */
public class Context {

    //这里因为可能涉及到用户信息的修改所以这里要采用一个线程安全的Map集合
    private static Map<String, List<GrantedAuthority>> loginMapInfo=new ConcurrentHashMap<>();

    public static Map<String, List<GrantedAuthority>> getLoginMapInfo() {
        return loginMapInfo;
    }
}
