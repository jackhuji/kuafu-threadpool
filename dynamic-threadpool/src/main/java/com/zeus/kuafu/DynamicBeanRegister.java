package com.zeus.kuafu;

import com.zeus.kuafu.common.DynamicProperties;
import com.zeus.kuafu.common.KuafuApplicationContextHelper;
import com.zeus.kuafu.common.DynamicConstants;
import com.zeus.kuafu.config.DynamicConfig;
import com.zeus.kuafu.config.DynamicConfigInit;
import com.zeus.kuafu.config.DynamicConfigListener;
import com.zeus.kuafu.core.AdjustExecutor;
import com.zeus.kuafu.core.InitPoolMap;
import com.zeus.kuafu.monitor.DynamicThreadPoolMonitor;
import com.zeus.kuafu.notify.DingNotifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName DynamicBeanRegister
 * @Description
 * @Author hjq
 * @Date 2023/3/16
 **/


@Configuration
@EnableConfigurationProperties(DynamicProperties.class)
@ConditionalOnProperty(prefix = DynamicConstants.CONFIG_PREFIX, name = "enable", havingValue = "true")
public class DynamicBeanRegister {

    private DynamicProperties dynamicProperties;

    @Autowired
    public void setDynamicProperties(DynamicProperties dynamicProperties) {
        this.dynamicProperties = dynamicProperties;
    }

    @Bean
    public KuafuApplicationContextHelper getApplicationContextHelper(){
        return new KuafuApplicationContextHelper();
    }

    /**
     * 动态配置
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public DynamicConfig getDynamicConfig(){
        return new DynamicConfig();
    }

    @Bean
    @ConditionalOnMissingBean
    public AdjustExecutor getExecutor(){
        return new AdjustExecutor();
    }

    /**
     * 注解扫描初始化map的bean
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public InitPoolMap getInitPoolMap(){
        return new InitPoolMap();
    }

    @Bean
    @ConditionalOnMissingBean
    public DynamicConfigInit getListener(){
        return new DynamicConfigInit();
    }

    @Bean
    @ConditionalOnMissingBean
    public DynamicConfigListener getStarter(){
        return new DynamicConfigListener();
    }

    @Bean
    @ConditionalOnMissingBean
    public DynamicThreadPoolMonitor getMonitor(){
        return new DynamicThreadPoolMonitor();
    }

    @Bean
    @ConditionalOnMissingBean
    public DingNotifyService getNotify() {
        return new DingNotifyService(dynamicProperties);
    }

}
