package com.ar.contractreview.security.filter;

import com.ar.contractreview.security.context.Context;
import com.ar.contractreview.result.R;
import com.ar.contractreview.result.ResponseCode;
import com.ar.contractreview.security.service.IJwtService;
import com.ar.contractreview.security.utils.HttpResponseUtils;
import com.ar.contractreview.utils.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.io.IOException;
import java.util.List;

/**
 * @author xiaobobo
 * @title: AuthorizationFilter
 * @projectName ar_26_springboot-parent
 * @description: 这个过滤器的主要功能是校验这个身份是否合法
 * @date 2026/9/3  11:20
 */
@Slf4j
public class MyAuthenticationFilter extends BasicAuthenticationFilter {


    private IJwtService jwtService;



    //这里也需要你传递一个authenticationManager对象过来
    public MyAuthenticationFilter(AuthenticationManager authenticationManager,IJwtService jwtService) {
        super(authenticationManager);
        this.jwtService=jwtService;
    }


    /**
     * 这个方法的主要作用就是校验身份是否合法
     * @param request
     * @param response
     * @param chain
     * @throws IOException
     * @throws ServletException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        //第一步：就是通过请求拿到我们的token
        String token = jwtService.getToken(request);
        //第二步：校验token的合法性
        if(!StringUtils.isEmpty(token)){
            String username = jwtService.getUsernameByToken(token);
            Long userId = jwtService.getUserIdByToken(token);
            //就要校验身份是否合法
            if (jwtService.verifyToken(token)) {
                //说明身份合法
                log.info("身份合法:{}",token);
                //就要通过token拿到角色权限信息
                List<GrantedAuthority> roleAndPerms = Context.getLoginMapInfo().get(token);
                //将角色和权限信息直接放到上下文中 用来校验身份是否合法
                //需要从新封装成UsernamePasswordAuthenticationToken对象
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(username, userId, roleAndPerms);
                //放到上下文中
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                //放行
                chain.doFilter(request,response);
                return;
            }
            //代码执行到这里说明身份不合法
            //就直接向下执行
        }
        //下面就是token等于空 那就不放行
        log.info("身份认证失败:{}",token);
        //你还要告诉请求方
        R r = R.fail(ResponseCode.USER_INVALIDATE_EXCEPTION);
        HttpResponseUtils.sendResponse(response,r);
    }
}
