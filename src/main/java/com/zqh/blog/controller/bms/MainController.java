package com.zqh.blog.controller.bms;

import com.zqh.blog.controller.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
* author: zqh
* email：zqhfsf@gmail.com
* date: 2018/2/8 22:45
* description: 后台登陆controller
**/
@RequestMapping("bms")
@Controller
public class MainController extends BaseController {


    @GetMapping("login.do")
    public String toLogin() {
        return "bms/adminLogin";
    }

    @PostMapping("login.do")
    public String loginSubmit(String userName,String password) {
        if("admin".equals(userName) && "123456".equals(password)) {
            return "bms/index";
        }

        return "bms/adminLogin";
    }

    @RequestMapping("main.do")
    public String toMain() {
        return "bms/common/main";
    }
}
