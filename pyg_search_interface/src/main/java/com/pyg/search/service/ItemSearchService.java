package com.pyg.search.service;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {

    /**
     * 搜索
     * @param searchMap(keywords)
     * @return
     */
    public Map search(Map searchMap);
    /**
     * 商品审核通过，-------导入solr索引库
     * @param itemList
     */
    public void importList(List itemList);
    /**
     * 商品删除---------删除solr索引库
     * @param goodsIds
     */
    public void deleteByGoodsIds(List goodsIds);
}
