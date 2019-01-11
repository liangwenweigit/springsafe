package com.fly.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author liang
 * @date 2019/1/7 - 9:01
 * 自定义用户信息类 要去数据库查
 */
@Component
public class MyUserDetailsService implements UserDetailsService{
    //@Autowired //注入可以根据页面输入的用户名查询用户信息的dao
    //private Dao dao;
    @Resource(name = "passwordEncoder")
    private PasswordEncoder passwordEncoder;

    //这个作用往session中存储用户名，使用srring相关的是可以这样获取的
    @Autowired
    HttpServletRequest request;

    //输出日志用的 开发完可以去掉
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //User1 user1 = dao.selectUserBy(username)实际开发根据上面用户名查询去数据库查询用户信息(重点)
        //查出来后，还要判断是否为空，可以设置下面的属性为false，有一个为false就会验证不通过
        System.out.println("123");
        logger.info("登录用户名："+username);
        //这里的用户数据是模拟出来的 没有去查数据库
        //.userdetails.User; 构造User构造函数有7个参数
        //参数1：用户输入的账号，
        //参数2：数据库根据用户名查出来的密码，
        //参数3：enabled用户名,密码,是否激活 true表示已经激活 根据逻辑也 可以永远设置成true
        //参数4：accountNonExpired如果帐户没有过期设置为true          可以永远设置成true
        //参数5：credentialsnonexpired如果证书/密码没有过期设置为true  可以永远设置成true
        //参数6：accountNonLocked如果帐户不锁定设置为true             可以永远设置成true
        //参数7：集合 代表用户的权限也是数据库查的 多个权限则设置参数"VIP1,VIP2" 逗号隔开
//public User(String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities)
        //第二个参数数据库查出来是已经是加密的，不用加密了，但是用户注册的时候要用这个加密，同一密码加密每次字符串都不一样
        User user = new User(username, passwordEncoder.encode("123456"),true,true,true,
                true, AuthorityUtils.commaSeparatedStringToAuthorityList("VIP,VIP2"));
        request.getSession().setAttribute("user",username);
        return user;
    }
}
