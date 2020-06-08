package com.lxinyu.house.biz.config;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.google.common.collect.Lists;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DruidConfig {

    //@ConfigurationProperties这个注解的作用
    //                                  1.将配置文件中的spring.druid这项配置与DruidDataSource中的属性进行绑定。DruidDataSource也会成为一个spring bean
    //                                  2.将bean方法中的返回对象与外部的配置文件进行绑定
    @ConfigurationProperties(prefix = "spring.druid")
    @Bean(initMethod = "init", destroyMethod = "close")
    public DruidDataSource dataSource(){
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setProxyFilters(Lists.newArrayList(statFilter()));
        return dataSource;
    }

    //该filter是将慢sql打印出来
    @Bean
    public Filter statFilter(){
        StatFilter statFilter = new StatFilter();
        //设置慢sql的处理时长
        statFilter.setSlowSqlMillis(5000);
        //设置是否打印慢sql -- true
        statFilter.setLogSlowSql(true);
        //设置是否将sql合并
        statFilter.setMergeSql(true);
        return statFilter;
    }

    @Bean
    public ServletRegistrationBean servletRegistrationBean(){
        return new ServletRegistrationBean(new StatViewServlet(),"/druid/*");
    }
}
