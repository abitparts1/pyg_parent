package com.pyg.pojogroup;

import com.pyg.pojo.TbOrderItem;

import java.io.Serializable;
import java.util.List;

public class Cart implements Serializable{
    //商家id，商家名字，商品明细
    private String sellerId;
    private String sellerName;
    private List<TbOrderItem> orderItemList;

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public List<TbOrderItem> getOrderItemList() {
        return orderItemList;
    }

    public void setOrderItemList(List<TbOrderItem> orderItemList) {
        this.orderItemList = orderItemList;
    }
}
