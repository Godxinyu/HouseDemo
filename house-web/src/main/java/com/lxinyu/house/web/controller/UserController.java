package com.lxinyu.house.web.controller;

import com.lxinyu.house.biz.service.UserService;
import com.lxinyu.house.common.model.User;
import com.lxinyu.house.common.result.ResultMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @RequestMapping("user")
    public List<User> getUsers(){
        return userService.getUsers();
    }

    /**
     * 注册提交：1. 注册验证 2. 发送邮件 3. 验证失败重定向到注册页面
     * 注册页获取：根据account对象为依据判断是否注册页获取请求
     * @param account
     * @param modelMap
     * @return
     */
    @RequestMapping("accounts/register")
    public String accountsRegister(User account, ModelMap modelMap){
        if(account == null || account.getName() == null){
            return "accounts/register";
        }

        // 用户验证
        ResultMsg resultMsg = UserHelper.validate(account);

        if(resultMsg.isSuccess() && userService.addAccount(account)){
            modelMap.put("email", account.getEmail());
            return "accounts/registerSubmit";
        } else {
            return "redirect:/accounts/register?"+resultMsg.asUrlParams();
        }


    }

    @RequestMapping("accounts/verify")
    public String verifyAccount(String key, ModelMap modelMap){
        boolean result = userService.verifyAccount(key);
        if(result){
            return "redirect:/index?"+ResultMsg.successMsg("激活成功").asUrlParams();
        }else{
            return "redirect:/accounts/register?"+ResultMsg.errorMsg("激活失败，请重新注册").asUrlParams();
        }

    }

}
