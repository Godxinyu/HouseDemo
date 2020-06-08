package com.lxinyu.house.biz.service;

import com.lxinyu.house.biz.mapper.UserMapper;
import com.lxinyu.house.common.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public List<User> getUsers(){
        return userMapper.selectUsers();
    }

}
