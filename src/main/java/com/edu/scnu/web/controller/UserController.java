package com.edu.scnu.web.controller;

import com.edu.scnu.bean.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @ClassName UserController
 * @Description TODO
 * @Author Administrator
 * @Date 2019-04-27 17:41
 * @Version 1.0
 **/
@Controller
@RequestMapping("/user")
public class UserController {

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login() {
        return "/user/login";
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(User user) {

        return "redirect:/sys/index";
    }
}
