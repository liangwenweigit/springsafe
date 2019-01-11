package com.fly.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @author liang
 * @date 2019/1/8 - 21:58
 */
@Controller
@RequestMapping("/user")
public class UserController {
    @RequestMapping("/getUser")
    public String getUser(){
        return "success";
    }

    @RequestMapping("/login")
    public String login(){

        return "userlogin";
    }

    @RequestMapping("/main")
    public String main() {

        return "user/main";
    }
    @RequestMapping("/invalidSession")
    public String invalidSession(HttpServletRequest request) {
        request.setAttribute("msg", "登录超时,请重新登录");
        System.out.println("123");
        System.out.println("123");
        return "userlogin";
    }


}
