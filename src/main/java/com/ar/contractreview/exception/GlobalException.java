package com.ar.contractreview.exception;

import com.ar.contractreview.observer.base.Subject;
import com.ar.contractreview.result.R;
import com.ar.contractreview.result.ResponseCode;
import com.ar.contractreview.utils.ExceptionUtils;
import com.ar.contractreview.utils.ThreadPoolUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;

/**
 * @author wyh
 * @title: GlobalException
 * @projectName ar_26_springboot-parent
 * @description: TODO
 * @date 2026/8/25  15:08
 */
@RestControllerAdvice
@Slf4j
public class GlobalException {

    @Autowired
    private Subject subject;

    /**
     * 处理业务异常
     * @param businessException
     * @return
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseBody
    public R handlerBusinessException(BusinessException businessException){
        log.info("出现了业务异常:{}",businessException.getMessage());
        return R.fail(businessException.getCode(),businessException.getMessage());
    }


    /**
     * 处理SQL异常
     * @param sqlException
     * @return
     */
    @ExceptionHandler(SQLException.class)
    @ResponseBody
    public R handlerSQLException(SQLException sqlException){
        log.info("出现了SQL异常....");
        return R.fail(ResponseCode.SQL_EXCEPTION);
    }


    /**
     * 处理其他异常
     * @return
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public R handlerSystemException(Exception err){
        err.printStackTrace();
        String exceptionStackInfo = ExceptionUtils.getExceptionStackInfo(err);
        //这个日志最终是需要写入到文件中的
        log.error("出现了系统异常:{}",exceptionStackInfo);
        //这里通知运维人员检查代码是否有误
        //观察者设计模式通知运维...  这里可能出现高并发
        //防止出现高并发 这里使用线程池做缓冲
        ThreadPoolUtils.getNotifyThreadPoolExecutor().execute(()->{
            subject.notifyMessage(exceptionStackInfo);
        });
        return R.fail(ResponseCode.SYSTEM_EXCEPTION);
    }


}
