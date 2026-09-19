package com.ar.contractreview.security.filter;

import com.alibaba.fastjson.JSON;
import com.ar.contractreview.security.bo.SecurityUser;
import com.ar.contractreview.security.context.Context;
import com.ar.contractreview.security.dto.UserLogin;
import com.ar.contractreview.result.R;
import com.ar.contractreview.result.ResponseCode;
import com.ar.contractreview.security.service.IJwtService;
import com.ar.contractreview.security.utils.HttpResponseUtils;
import com.ar.contractreview.utils.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * @author xiaobobo
 * @title: LoginFilter
 * @projectName ar_26_springboot-parent
 * @description: 因为security中原来的过滤器 只能接受前端传递的 form表单数据不能接受json格式的数据
 * 所以我们需要重写这个登录的过滤器
 * 而且还有个问题 原来的security登录成功之后 是直接跳转页面 我们今天希望的是返回JSON格式的数据
 * @date 2026/9/3  8:41
 */
@Slf4j
public class LoginFilter extends UsernamePasswordAuthenticationFilter {
    //登录的管理器
    private AuthenticationManager authenticationManager;

    private IJwtService jwtService;


    public LoginFilter(AuthenticationManager authenticationManager,IJwtService jwtService){
        this.authenticationManager=authenticationManager;
        this.jwtService=jwtService;
        this.setRequiresAuthenticationRequestMatcher(
                new AntPathRequestMatcher("/user/login", HttpMethod.POST.name()));
    }


    /**
     * 这个方法是登录的时候自动调用的
     * @param request from which to extract parameters and perform the authentication
     * @param response the response, which may be needed if the implementation has to do a
     * redirect as part of a multi-stage authentication process (such as OIDC).
     * @return
     * @throws AuthenticationException
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        //前端可能传递json格式的数据
        //拿到前端传递的数据
        try {
            UserLogin userLogin=getLoginUser(request);
            //接下来就是将用户信息封装成一个Authentication对象返回去就可以了
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userLogin.getUsername(), userLogin.getPassword(), null);
            //这里需要传递一个authenticationManager对象过来(这里是从配置文件中传递过来的)
            return this.authenticationManager.authenticate(usernamePasswordAuthenticationToken);

        } catch (BadCredentialsException e) {
            log.info("用户名或密码错误:{}",e.getMessage());
            HttpResponseUtils.sendResponse(response,R.fail(ResponseCode.LOGIN_EXCEPTION));
            return null;
        } catch (Exception e) {
            log.info("获取前端传递的JSON信息失败:"+e.getMessage(), e);
            HttpResponseUtils.sendResponse(response,R.fail(ResponseCode.LOGIN_JSON_GET__EXCEPTION));
            return null;
        }
    }

    /**
     * 获取登录的用户信息
     * @param request
     * @throws IOException
     */
    private UserLogin getLoginUser(HttpServletRequest request) throws IOException {
        request.setCharacterEncoding("UTF-8");
        //就可以取出这个数据了 因为是post请求而且是json格式所以只能采用流的形式来拿到这个数据
        ServletInputStream in= request.getInputStream();
        if(null==in){
            throw new RuntimeException("参数不对");
        }
        //设置一个缓冲数组
        byte[] buf=new byte[1024];
        //一次性读取完成
        int readLength = in.read(buf);
        if(readLength==0){
            throw new RuntimeException("参数不对");
        }
        //格式化成正常的字符串
        String message = new String(buf, 0, readLength);
        //接下来可以转换成Java对象
        if(StringUtils.isEmpty(message)){
            throw new RuntimeException("登录的信息不能为空");
        }
        //在这里我们可以实现代码
        UserLogin userLogin = JSON.parseObject(message, UserLogin.class);
        return userLogin;
    }


    /**
     * 登录成功之后要回调的函数
     * @param request
     * @param response
     * @param chain
     * @param authResult 这个是UserDetailService中返回的那个对象的封装
     * method.
     * @throws IOException
     * @throws ServletException
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        //如果执行到这里 说明登录是成功的
        //拿到用户信息 生成身份唯一的标识 用户信息怎么拿到呢?
        //下面拿到的信息 其实就是UserDetailService中返回的数据
        SecurityUser securityUser = (SecurityUser) authResult.getPrincipal();
        //先生成用户身份的唯一标识？前面我们生成标识是UUID 正常做开发的时候这里要采用JWT(JSON Web Token)
        String token = jwtService.createToken(securityUser.getUser().getId(), securityUser.getUser().getUsername());
        //接下来就要将权限和角色信息放到Redis中.... Redis是一个基于内存的数据库  数据放到内存的速度快
        //但是现在还没有学习Redis 那么我们将信息 放到Map中一样的
        Context.getLoginMapInfo().put(token, (List<GrantedAuthority>) securityUser.getAuthorities());
        //把信息返回给客户端
        HttpResponseUtils.sendResponse(response,R.ok()
                .data("token",token)
                .data("username",securityUser.getUsername()));
        log.info("{}:登录成功....",securityUser.getUsername());
    }


    /**
     * 就是用户登录失败的回调函数
     * @param request
     * @param response
     * @param failed
     * @throws IOException
     * @throws ServletException
     */
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        //如果执行到这里 说明登录是失败的
        HttpResponseUtils.sendResponse(response,R.fail(ResponseCode.LOGIN_EXCEPTION));
        log.info("登录失败....{}",failed.getMessage());
    }
}
