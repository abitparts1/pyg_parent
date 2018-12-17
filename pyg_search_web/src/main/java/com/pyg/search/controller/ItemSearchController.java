package com.pyg.search.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.pyg.search.service.ItemSearchService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("search")
public class ItemSearchController {

    @Reference(timeout = 5000)
    private ItemSearchService searchService;

    @RequestMapping("searchItem")
    public Map searchItem(@RequestBody Map searchMap){
        return searchService.search(searchMap);
    }
}
