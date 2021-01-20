package com.lxinyu.house.biz.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.Lists;
import com.lxinyu.house.biz.mapper.UserMapper;
import com.lxinyu.house.common.model.User;
import com.lxinyu.house.common.utils.BeanHelper;
import com.lxinyu.house.common.utils.HashUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {



    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailService mailService;

    @Autowired
    private FileService fileService;

    public List<User> getUsers(){
        return userMapper.selectUsers();
    }

    /**
     * 1. 插入数据库，非激活； 密码加盐加密md5；保存头像到本地
     * 2. 生成key，绑定email
     * 3. 发送邮件给用户
     * @param account
     * @return
     */

    // 该注解在其他类调用此方法时生效，若本类其他方法调用该方法，该注解无效
    @Transactional(rollbackFor = Exception.class)
    public boolean addAccount(User account){
        // 对用户密码进行加盐操作
        account.setPasswd(HashUtils.encryPassword(account.getPasswd()));
        // 将用户头像保存到本地
        List<String> imgList = fileService.getImgPath(Lists.newArrayList(account.getAvatarFile()));
        if(!imgList.isEmpty()){
            account.setAvatar(imgList.get(0));
        }
        BeanHelper.setDefaultProp(account,User.class);
        BeanHelper.onInsert(account);
        account.setEnable(0);
        userMapper.insert(account);

        // 发送邮件
        mailService.registerNotify(account.getEmail());

        return true;
    }

//    @Transactional(rollbackFor = Exception.class)
    public boolean verifyAccount(String key){

        return mailService.verifyAccount(key);
    }


}
