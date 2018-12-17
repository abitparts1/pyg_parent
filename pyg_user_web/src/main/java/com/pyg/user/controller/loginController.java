package com.pyg.user.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class loginController {

    @RequestMapping("getLoginUser")
    public Map getLoginUser(){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Map result = new HashMap();
        result.put("username",name);
        return result;
    }
}
