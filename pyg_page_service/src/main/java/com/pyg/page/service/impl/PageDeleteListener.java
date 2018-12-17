package com.pyg.page.service.impl;

import com.pyg.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.*;

@Component
public class PageDeleteListener implements MessageListener{
    @Autowired
    private ItemPageService itemPageService;
    @Override
    public void onMessage(Message message) {
        //接收消息
        ObjectMessage objectMessage = (ObjectMessage) message;
        try {
            Long[] goodIds = (Long[]) objectMessage.getObject();
            boolean b = itemPageService.deleteItemHtml(goodIds);
            System.out.println("删除页面结果"+b);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
