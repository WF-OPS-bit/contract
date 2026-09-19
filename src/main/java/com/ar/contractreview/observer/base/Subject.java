package com.ar.contractreview.observer.base;

/**
 * 通知主题接口，定义发送通知的行为
 * 当事件发生时，通过该接口通知所有注册的观察者
 *
 * @author wyh
 */
public interface Subject {

    /**
     * 通知所有观察者
     *
     * @param message 通知内容
     */
    void notifyMessage(String message);
}