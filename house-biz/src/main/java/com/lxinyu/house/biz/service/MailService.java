package com.lxinyu.house.biz.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.lxinyu.house.biz.mapper.UserMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class MailService {

    private Logger logger = LoggerFactory.getLogger(MailService.class);
;

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
    private JavaMailSender mailSender;

    @Value("${domain.name}")
    private String domainName;

    @Autowired
    private UserMapper userMapper;

    @Value("${spring.mail.username}")
    private String from;

    public void sendMail(String title, String url, String email){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setText(url);
        mailSender.send(message);
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

        sendMail(domainName, url, email);
    }

    public boolean verifyAccount(String key){
        String userEmail = registerCache.getIfPresent(key);
        if (StringUtils.isBlank(userEmail)){
            return false;
        }

        int i = userMapper.updateUser(userEmail);
        logger.info("更新数据"+i+"条成功");

        registerCache.invalidate(key);

        return true;
    }
}
