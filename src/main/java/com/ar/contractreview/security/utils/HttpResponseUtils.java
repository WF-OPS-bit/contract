package com.ar.contractreview.security.utils;

import com.alibaba.fastjson.JSON;
import com.ar.contractreview.result.R;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author xiaobobo
 * @title: HttpResponseUtils
 * @projectName ar_26_springboot-parent
 * @description: 这个是Http响应的工具类
 * @date 2026/9/3  11:12
 */
@Slf4j
public class HttpResponseUtils {

    /**
     * 给客户端返回数据
     * @param resp
     * @param r
     */
    public static void  sendResponse(HttpServletResponse resp, R r){
        String jsonString = JSON.toJSONString(r);
        resp.setCharacterEncoding("UTF-8");
        PrintWriter writer = null;
        try {
            writer = resp.getWriter();
        } catch (IOException e) {
            log.error("获取响应流失败....");
            throw new RuntimeException(e);
        }
        writer.write(jsonString);
        writer.flush();
        writer.close();
    }


}
