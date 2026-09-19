package com.ar.contractreview.security.handler;

import com.ar.contractreview.security.context.Context;
import com.ar.contractreview.result.R;
import com.ar.contractreview.security.service.IJwtService;
import com.ar.contractreview.security.utils.HttpResponseUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

/**
 * @author xiaobobo
 * @title: MyLogoutHandler
 * @projectName ar_26_springboot-parent
 * @description: 这个类表示的是退出之后要执行的操作
 * @date 2026/9/3  13:37
 */
@Component
@Slf4j
public class MyLogoutHandler implements LogoutHandler {

    @Autowired
    private IJwtService jwtService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response,
                       Authentication authentication) {
        //这里执行退出服务器要干的事情
        String token = jwtService.getToken(request);
        //然后删除Context中的内容
        Context.getLoginMapInfo().remove(token);
        //接下来就可以执行你的操作了
        R r = R.ok();
        HttpResponseUtils.sendResponse(response,r);
        log.info("退出成功:{}",token);
    }
}
