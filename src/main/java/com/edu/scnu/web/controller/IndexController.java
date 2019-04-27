package com.edu.scnu.web.controller;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @ClassName IndexController
 * @Description
 * @Author Administrator
 * @Date 2019-04-27 16:43
 * @Version 1.0
 **/
@Controller
@RequestMapping("/sys")
public class IndexController {

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public String index() {
        System.out.println("...........");
        return "/sys/index";
    }

    @RequiresRoles({"admin"})
    @RequestMapping(value = "/admin", method = RequestMethod.GET)
    public String admin() {
        return "/sys/admin";
    }

    @RequiresRoles({"admin", "public"})
    @RequestMapping(value = "/public", method = RequestMethod.GET)
    public String publicc() {
        return "/sys/public";
    }
}
