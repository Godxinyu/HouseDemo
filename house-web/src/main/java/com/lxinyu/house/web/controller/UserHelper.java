package com.lxinyu.house.web.controller;

import com.lxinyu.house.common.model.User;
import com.lxinyu.house.common.result.ResultMsg;
import org.junit.platform.commons.util.StringUtils;

public class UserHelper {

    public static ResultMsg validate(User account){
        if(StringUtils.isBlank(account.getEmail())){
            return ResultMsg.errorMsg("Email 有误");
        }
        if(StringUtils.isBlank(account.getName())){
            return ResultMsg.errorMsg("Name 有误");
        }
        if(StringUtils.isBlank(account.getConfirmPasswd()) || StringUtils.isBlank(account.getPasswd())
            || !account.getPasswd().equals(account.getConfirmPasswd())){
            return ResultMsg.errorMsg("密码有误");
        }
        if(account.getPasswd().length() < 6){
            return ResultMsg.errorMsg("密码长度大于6位");
        }
        return ResultMsg.successMsg("");

    }
}
