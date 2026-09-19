package com.ar.contractreview.security.dto;

import lombok.Data;

/**
 * @author xiaobobo
 * @title: UserLogin
 * @projectName ar_26_springboot-parent
 * @description: TODO
 * @date 2026/9/3  8:51
 */
@Data
public class UserLogin {

    //用户名
    private String username;

    //密码
    private String password;

    //是否记住我
    private Boolean rememberMe=false;


}
