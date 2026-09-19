package com.ar.contractreview.utils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author wyh
 * @title: ThreadPoolUtils
 * @projectName ar-26-ramework-parent
 * @description: 这个是线程池的工具类
 * @date 2026/8/21  14:30
 */
public class ThreadPoolUtils {

    //这个是通知任务的线程池
    private static ThreadPoolExecutor notifyThreadPoolExecutor=null;

    static {
        notifyThreadPoolExecutor=new ThreadPoolExecutor(
                10,100,10, TimeUnit.SECONDS,new ArrayBlockingQueue<>(8192)
        );
    }

    public static ThreadPoolExecutor getNotifyThreadPoolExecutor() {
        return notifyThreadPoolExecutor;
    }
}
