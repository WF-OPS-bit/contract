package com.ar.contractreview.exception;

import lombok.Data;

/**
 * @author wyh
 * @title: BusinessException
 * @projectName ar_26_springboot-parent
 * @description: TODO
 * @date 2026/8/25  15:06
 */
@Data
public class BusinessException extends RuntimeException{

    //错误码
    private Integer code;

    //异常信息
    private String message;

    public BusinessException(Integer code,String message){
        //把异常信息传递给他爹 让异常链路保持完整
        super(message);
        this.code=code;
        this.message=message;
    }
}
