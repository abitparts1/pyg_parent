package com.pyg.page.service;



public interface ItemPageService {
    /**
     * 根据商品的id，生成静态页面
     * @param goodsId
     */
    public void genItemPage(Long goodsId);

    //删除商品详情页
    public boolean deleteItemHtml(Long[] goodsIds);
}
