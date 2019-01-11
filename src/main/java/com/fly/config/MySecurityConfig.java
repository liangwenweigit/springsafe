package com.fly.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.Collection;


/**
 * @author liang
 * @date 2019/1/2 - 14:32
 */
@EnableWebSecurity//这个注解类带了configuration注解 所以不用加了
public class MySecurityConfig extends WebSecurityConfigurerAdapter{
    //日志输出使用的
    private Logger logger = LoggerFactory.getLogger(getClass());
    //自定义用户信息类 要去数据库查（授权登录验证 自动登录都需要使用）
    @Autowired
    private MyUserDetailsService myUserDetailsService;

    @Autowired
    private DataSource dataSource;

    /**
     * 密码加密用的  下面那个(授权验证)方法需要引用这个返回值，并且用户注册的时候也需要使用到这个方法
     * 用户注册的时候：
     *     //定义这个bean变量
     *     @Resource(name = "passwordEncoder")
     *     private PasswordEncoder passwordEncoder;
     *     //注册方法内使用这个类加密密码存储到数据库（同一个密码每次加密结果不一样，不应该security自己比较，推荐使用）
     *     String username = passwordEncoder.encode("表单传来的密码");
     */
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    //自定义用户信息类 要去数据库查 注入自定义的myUserDetailsService用户信息类
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        //自定义授权登录验证 要去数据库查                    //2.X版本还需要加后面这个类 因为依赖是security5.X版本
        auth.userDetailsService(myUserDetailsService).passwordEncoder(passwordEncoder());
    }

    //定制url的授权规则
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //设置登录,注销，表单登录不用拦截，其他请求要拦截
        http.authorizeRequests().antMatchers("/","/user/login","/user/invalidSession").permitAll()
                .anyRequest().authenticated()//这句表示除了上面的不需要认证其他的都需要认证
                .and()
                .logout().permitAll().and();//注销不拦截
            //也可以这样按用户权限制定认证规则
            //.antMatchers("/level1/**").hasRole("VIP1")//表示/level1/** 路径需要VIP1权限
            //.antMatchers("/level2/**").hasRole("VIP2")//表示/level2/** 路径需要VIP2权限

        //开启自动配置的登陆功能，效果，如果没有登陆，没有权限就会来到登陆页面
        //并且指定 带有username,password属性数据才做尝试登录处理，并且指定登录页面，和登录失败页面
        //loginProcessingUrl("/users/loginForm")指定登录表单的action
        http.formLogin().loginProcessingUrl("/users/loginForm").usernameParameter("username").passwordParameter("password")
                //这里的/user/userlogin是一个controller方法 转到登录页面userlogin.html的方法
                .loginPage("/user/login")
                //登录成功后要干什么：我们自定义，已经在下面写了一个bean 直接当参数放进来即可
                .successHandler(authenticationSuccessHandler())
                //登录失败要做干什么：带错误信息返回登录页面,已经也在下面写了一个bean 直接当参数放进来即可
                .failureHandler(authenticationFailureHandler());


        //开启自动配置的注销功能 //访问/logout 表示用户注销，清空session
        //点击注销会删除cookie,数据库的记录也会删，反正就是再次访问就要输账号密码
        http.logout().logoutSuccessUrl("/user/login");//手动设置注销成功以后来到登录页面路径


        //开启记住我功能
        http.rememberMe().rememberMeParameter("remember-me")
                .tokenRepository(persistentTokenRepository())
                //记住我 过期时间
                .tokenValiditySeconds(60*60*24*14)
                //从数据库获取到用户用这个去做登录
                .userDetailsService(myUserDetailsService);
        //登陆成功以后，将cookie发给浏览器保存，以后访问页面带上这个cookie，只要通过检查就可以免登录


        //关闭默认的csrf跨站请求伪造认证
        http.csrf().disable();

        //session失效后要做的事 到这个controller由它带着信息转发到登录页面
        http.sessionManagement().invalidSessionUrl("/user/invalidSession");
        //session并发处理，一个用户只有一个session,第二次登录会把第一次的杀死
        http.sessionManagement().maximumSessions(1).expiredUrl("/user/invalidSession");


    }

    //设置静态资源不要拦截
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/js/**","/css/**","/images/**");
        //,"**.gif","**.jpg","**.png","**.css","**.js"这些失效
    }

    //登录成功后要做什么 默认是回到之前的请求 现在自定义登录成功要做的事
    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler(){
       return new AuthenticationSuccessHandler() {
           //登录成功后会调用这个方法
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                logger.info("登录成功~~~~~");
                //这个就是登录成功后的用户名 可以存进session
                String username = authentication.getName();
                System.out.println(username);
                //把用户设置进去session
                request.getSession().setAttribute("user",username);
                //验证后的用户的权限
                Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
                for (Object o:authorities) {
                    System.out.println(o.toString());
                }
                  /*登录成功要转跳的页面
                 * 这个转跳需要经过模板引擎
                 @RequestMapping("/main")
                 public String main(){

                 return "main";
                 }
                 */
                 //重定向到这个controller连接，在里面获取
                response.sendRedirect("/user/main");
            }
        };
    }

    //登录失败要做什么
    public AuthenticationFailureHandler authenticationFailureHandler(){

        return new AuthenticationFailureHandler() {
            @Override
            public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
                //exception对象包含了认证的过程中出现的异常 比如 账号不存在 冻结 过去 锁定等等
                logger.info("登录失败~~~~~");
                //如果是html就用建议用ajax,通过json格式传值，url从哪里来就从哪来回去
                System.out.println(exception.getMessage());//时间开发查看信息是什么以判断设置msg
                request.setAttribute("msg","密码或者用户名错误");
                //转跳到登录页面
                request.getRequestDispatcher("/user/login").forward(request,response);
            }
        };
    }

    //自动登录要用的 并且注入dataSource
    //这个功能要使用必须在每次搭建前到数据库执行下面那句sql
    @Bean
    public PersistentTokenRepository persistentTokenRepository(){
        JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
        tokenRepository.setDataSource(dataSource);
        //因为记住密码原来是把用户存到数据库的，二次登录是查数据库登录的
        //create table persistent_logins (username varchar(64) not null, series varchar(64) primary key, token varchar(64) not null, last_used timestamp not null)
        //这里注意：表单内的checkbox的name="remember-me"值要和这里记住我的值.rememberMeParameter("remember-me")一样
        return  tokenRepository;
    }

}
