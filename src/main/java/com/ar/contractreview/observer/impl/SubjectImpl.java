package com.ar.contractreview.observer.impl;

import com.ar.contractreview.observer.base.Observer;
import com.ar.contractreview.observer.base.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

/**
 * 通知主题实现类，管理观察者并广播通知消息
 * 默认注册邮件和短信两个观察者
 *
 * @author wyh
 */
@Component
public class SubjectImpl implements Subject {

    /**
     * 观察者列表
     */
    @Autowired   //TODO 这里不能直接注入
    private Map<String, Observer> observerMap;

    /**
     * 通知所有观察者
     *
     * @param message 通知内容
     */
    @Override
    public void notifyMessage(String message) {
        Collection<Observer> values = observerMap.values();
        for (Observer observer : values) {
            observer.notifyMessage(message);
        }
    }
}