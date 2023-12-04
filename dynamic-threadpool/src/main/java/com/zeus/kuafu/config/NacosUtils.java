package com.zeus.kuafu.config;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.extern.slf4j.Slf4j;

/**
 * @Classname NacosUtils
 * @Description
 * @Date 2023/12/3 3:12 下午
 * @Created by jack
 */

@Slf4j
public class NacosUtils {

    /**
     * nacos服务地址
     */
    public static final String CONFIG_SERVER = "127.0.0.1:8848";

    public static ConfigService getConfigServer() {

        // 创建ConfigService
        ConfigService configService = null;
        try {
            configService = NacosFactory.createConfigService(CONFIG_SERVER);
        } catch (NacosException e) {
            log.error("[动态线程池]创建nacos-configService发生异常,error={}", e);
        }

        return configService;
    }


}
