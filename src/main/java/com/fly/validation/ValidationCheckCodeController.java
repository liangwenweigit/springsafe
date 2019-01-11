package com.fly.validation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author liang
 * @date 2019/1/8 - 14:23
 * 校验校验码controller
 */
@Controller
@RequestMapping("/images")//images/**路径设置了不拦截的
public class ValidationCheckCodeController {

    @RequestMapping("/checkCode")
    public void checkCode(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String clientCheckcode = request.getParameter("checkcode");
        HttpSession session = request.getSession();
        String serverCheckcode = (String) session.getAttribute("checkcode");
        PrintWriter out = response.getWriter();
        if (clientCheckcode != null) {//校验码不区分大小写
            if (clientCheckcode.equalsIgnoreCase(serverCheckcode)) {
                out.print("success");
                out.flush();
                out.close();
            } else {
                out.print("error");
                out.flush();
                out.close();
            }
        }
    }
}
