package com.pyg.seckill.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class UserDetailsServiceImpl implements UserDetailsService{

    /*
        *@Desc 认证方法 作用相当于  <user>
        *@param username 登陆页提交过来的用户名 会根据用户名查询数据库 seller  password
        *@return org.springframework.security.core.userdetails.UserDetails
        **/
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println(username);
        //查询数据库
        List<GrantedAuthority> authorities = new ArrayList<>();//权限列表
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_SELLER");//权限对象
        GrantedAuthority authority2 = new SimpleGrantedAuthority("ROLE_USER");//权限对象
        authorities.add(authority2);
        authorities.add(authority);
        //user  username  password  authorities权限列表
        return new User(username,"",authorities);
    }
}
