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

    /*
        创建了一个本地缓存，用来存放随机字符串和email的键值对
        .maximumSize(100) 是指规定缓存项的数目不超过100，当达到了这个值，缓存将尝试回收最近没有使用或总体上很少使用的缓存项
                ---- 在缓存项的数目达到限定值之前，缓存就可能进行回收操作（当数目逼近时就有可能发生）
        .expireAfterAccess(15, TimeUnit.MINUTES)，定时回收，缓存项在15分钟内没有被读/写访问，则回收
                ---- 另一种定时回收方法expireAfterWrite(long, TimeUnit)，没有被写访问，则回收
        .removalListener(RemovalListener) 移除监听器，当缓存呗移除时做一些额外的操作，这里就是当缓存项被清除的时候将用户信息从用户表中删除
                ---- 当超时时间已经过了的情况下，仍然没有发送激活链接，就将用户从user表中删除，防止下次注册时唯一键约束报错
        RemovalNotification移除通知，其中包含移除原因RemovalCause、键和值
     */

    private final Cache<String, String> registerCache = CacheBuilder.newBuilder().maximumSize(100)
            .expireAfterAccess(15, TimeUnit.MINUTES).removalListener(new RemovalListener<String, String>() {
                @Override
                public void onRemoval(RemovalNotification<String, String> removalNotification) {
                    userMapper.delete(removalNotification.getValue());
                }
            }).build();

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailService mailService;

    @Autowired
    private FileService fileService;

    @Value("${domain.name}")
    private String domainName;

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
        registerNotify(account.getEmail());

        return false;
    }

    /**
     * 1. 缓存key-email的关系
     * 2. 借助spring mail 发送邮件
     * 3. 借助异步框架进行异步操作 引入异步框架（Springboot已经默认引入了，只需加入@Async注解即可，在调用该方法时，spring会调用一个线程池，将该任务放到线程池中），然后在启动类里添加@EnableAsync注解即可
     * @param email
     */
    @Async
    public void registerNotify(String email){
        // 随机生成一个10位的字符串，并将生成的字符串放到本地缓存中存放
        String randomKey = RandomStringUtils.randomAlphabetic(10);
        registerCache.put(randomKey, email);

        // 定义一个激活链接
        String url = "http://" + domainName + "/accounts/verify?key=" + randomKey;

        mailService.sendMail(domainName, url, email);
    }


}
