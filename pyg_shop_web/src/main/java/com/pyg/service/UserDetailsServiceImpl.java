package com.pyg.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.pyg.pojo.TbSeller;
import com.pyg.sellergoods.service.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * 认证类
 * @author Administrator
 *  因为安全框架的密码是写死的，所以我们把他改成去数据库查找的方式
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService{
    @Reference
    private SellerService sellerService;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("经过了grantedAuthority");
        List<GrantedAuthority> grantedAuths = new ArrayList<>();
        //构建角色
        grantedAuths.add(new SimpleGrantedAuthority("ROLE_SELLER"));
        //这里需要去数据库查一个对象的username给到前端故要调用findOne
        TbSeller seller = sellerService.findOne(username);
        if (seller!=null){//如果获取的对象不为空，则再判断状态码是不是通过审核的
            System.out.println(seller.getSellerId());
            if (seller.getStatus().equals("1")){
                System.out.println(1);
                return new User(username,seller.getPassword(),grantedAuths);
            }else {
                return null;
            }
        }else {
            return null;
        }
    }
}
