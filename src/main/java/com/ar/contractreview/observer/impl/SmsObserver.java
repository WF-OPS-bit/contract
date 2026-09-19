package com.ar.contractreview.observer.impl;

import com.ar.contractreview.observer.base.Observer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 短信通知观察者，接收到通知后通过日志模拟发送短信
 *
 * @author wyh
 */
@Slf4j
@Component
public class SmsObserver implements Observer {

    /**
     * 接收通知消息
     *
     * @param message 通知内容
     */
    @Override
    public void notifyMessage(String message) {
        log.info("短信通知: {}", message);
    }
}