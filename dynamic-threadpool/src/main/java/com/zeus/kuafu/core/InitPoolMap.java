package com.zeus.kuafu.core;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.type.MethodMetadata;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @ClassName InitPoolMap
 * @Description
 * @Author Jack.hu
 * @Date 2023/5/9
 **/

@Slf4j
public class InitPoolMap implements BeanPostProcessor, ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (!(bean instanceof ThreadPoolExecutor)){
            return bean;
        }

        // ApplicationContext applicationContext = KuafuApplicationContextHelper.getApplicationContext();
        try {
            // Adjustable adjustable = applicationContext.findAnnotationOnBean(beanName, Adjustable.class);

            BeanDefinitionRegistry registry = (BeanDefinitionRegistry) applicationContext;
            if (registry == null){
                return bean;
            }
            BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
            if (beanDefinition == null || !(beanDefinition instanceof AnnotatedBeanDefinition)) {
                return bean;
            }
            AnnotatedBeanDefinition annotatedBeanDefinition = (AnnotatedBeanDefinition) beanDefinition;
            MethodMetadata methodMetadata = (MethodMetadata) annotatedBeanDefinition.getSource();

            if (Objects.isNull(methodMetadata) || !methodMetadata.isAnnotated(Adjustable.class.getName())) {
                return bean;
            }
            Boolean enable = (Boolean) Optional.ofNullable(methodMetadata.getAnnotationAttributes(Adjustable.class.getName()))
                    .orElse(Collections.emptyMap())
                    .getOrDefault("enable", false);

            if (Boolean.TRUE.equals(enable)){
                AdjustExecutor.putIfNotExist(beanName, (ThreadPoolExecutor) bean);
            }

            log.info("[动态线程池][InitPoolMap][postProcessAfterInitialization]POOL初始化为={}", JSON.toJSONString(AdjustExecutor.POOL));
        } catch (NoSuchBeanDefinitionException e) {
            log.error("[动态线程池][error]InitPoolMap postProcessAfterInitialization error:", e);
        }

        return bean;
    }
}
