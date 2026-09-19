package com.ar.contractreview.security.entrypoint;

import com.ar.contractreview.result.R;
import com.ar.contractreview.result.ResponseCode;
import com.ar.contractreview.security.utils.HttpResponseUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author xiaobobo
 * @title: MyAuthenticationEntryPoint
 * @projectName ar_26_springboot-parent
 * @description: 这个就是没有权限访问的时候 执行的方法
 * @date 2026/9/3  13:41
 */
@Component
@Slf4j
public class MyAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        R r = R.fail(ResponseCode.NO_PERM_EXCEPTION);
        HttpResponseUtils.sendResponse(response,r);
        log.info("没有权限访问....");
    }
}
