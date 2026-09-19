package com.ar.contractreview.observer.base;

/**
 * 观察者接口，定义接收通知消息的行为
 * 当事件触发时，实现该接口的类会被通知
 *
 * @author wyh
 */
public interface Observer {

    /**
     * 接收通知消息
     *
     * @param message 通知内容
     */
    void notifyMessage(String message);
}