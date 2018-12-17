package com.pyg.entity;

import com.pyg.pojo.TbGoods;
import com.pyg.pojo.TbGoodsDesc;
import com.pyg.pojo.TbItem;

import java.io.Serializable;
import java.util.List;

public class Goods implements Serializable{
    private TbGoods goods;
    private TbGoodsDesc goodsDesc;
    private List<TbItem> itemCatList;

    public TbGoods getGoods() {
        return goods;
    }

    public void setGoods(TbGoods goods) {
        this.goods = goods;
    }

    public TbGoodsDesc getGoodsDesc() {
        return goodsDesc;
    }

    public void setGoodsDesc(TbGoodsDesc goodsDesc) {
        this.goodsDesc = goodsDesc;
    }

    public List<TbItem> getItemCatList() {
        return itemCatList;
    }

    public void setItemCatList(List<TbItem> itemCatList) {
        this.itemCatList = itemCatList;
    }
}
