package com.lxinyu.house.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;


/**
 * 该类用于处理自定义异常，记录异常日志并返回500页面
 * @ControllerAdvice 作用有三个 1.全局异常处理 --------与@ExceptionHandler一起使用 value值显示哪些异常进入到这个方法中
 *                              2.全局数据绑定 --------与@ModelAttribute一起使用（这个没用过。。。）
 *                              3.全局数据预处理 ------这个还不清楚怎么用，做什么用的
 */

@ControllerAdvice
public class ErrorHandler {

    private static final Logger logger = LoggerFactory.getLogger(ErrorHandler.class);

    @ExceptionHandler(value={Exception.class, RuntimeException.class})
    public String errorInfo(HttpServletRequest request, Exception e){
        logger.error(e.getMessage(), e);
        logger.error(request.getRequestURL() + " encounter 500");
        return "error/500";
    }
}
