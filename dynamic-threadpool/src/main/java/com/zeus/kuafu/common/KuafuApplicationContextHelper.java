package com.zeus.kuafu.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

/**
 * @ClassName ApplicationContextHelper
 * @Description
 * @Author Jack.hu
 * @Date 2023/4/3
 **/

@Slf4j
public class KuafuApplicationContextHelper implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    // todo 这里的set方法执行顺序比BeanPostProcessor晚，导致npe，本类暂不可用，原因待查
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public static ApplicationContext getApplicationContext(){
        return applicationContext;
    }

    public static <T> T getBean(Class<T> clazz){
        return applicationContext.getBean(clazz);
    }

    public static <T> Map<String, T> getBeansOfType(Class<T> clazz){
        return applicationContext.getBeansOfType(clazz);
    }

}
