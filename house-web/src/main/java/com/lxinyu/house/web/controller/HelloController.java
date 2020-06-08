package com.lxinyu.house.web.controller;

import com.lxinyu.house.biz.service.UserService;
import com.lxinyu.house.common.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
public class HelloController {

    @Autowired
    private UserService userService;

    @RequestMapping("hello")
    public String hello(ModelMap modelMap) throws IllegalAccessException {
        List<User> users = userService.getUsers();
        if(users.size()!= 0){
            throw new IllegalAccessException();
        }
        User user = users.get(0);
        //key对应的是freemarker中对象的名字
        modelMap.put("user", user);
        //返回的是freemarker文件的名字
        return "hello";
    }

    @RequestMapping("index")
    public String index(){
        return "homepage/index";
    }
}
