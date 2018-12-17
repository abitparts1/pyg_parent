package com.pyg.search.service.impl;

import com.alibaba.druid.sql.ast.expr.SQLCaseExpr;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pyg.pojo.TbItem;
import com.pyg.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;
import java.util.Map;

@Component
public class ItemSearchListener implements MessageListener{
    @Autowired
    private ItemSearchService itemSearchService;
    @Override
    public void onMessage(Message message) {
        System.out.println("接收监听的消息");
        try{
            //把messqge转换称文本对象，再把返回的内容转换称数组，遍历数组打印id和title，把规格转换成对象
            //再把转换后的值赋值给规格对象，导入数据
            TextMessage textMessage = (TextMessage) message;
            String text = textMessage.getText();
            List<TbItem> itemList = JSON.parseArray(text, TbItem.class);
            for (TbItem item : itemList) {
                System.out.println(item.getId()+"  ....   "+item.getTitle());
                Map jsonMap = JSON.parseObject(item.getSpec());
                item.setSpecMap(jsonMap);
            }
            itemSearchService.importList(itemList);
            System.out.println("成功导入索引库");

        }catch (Exception e){

        }
    }
}
