package com.zeus.kuafu.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.zeus.kuafu.common.DynamicProperties;
import com.zeus.kuafu.core.AdjustExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Objects;

/**
 * @ClassName ConfigListener
 * @Description
 * @Author hjq
 * @Date 2023/12/3
 **/

@Slf4j
public class DynamicConfigInit {

    @Autowired
    private DynamicConfig dynamicConfig;

    @Autowired
    private DynamicProperties dynamicProperties;


    @PostConstruct
    public void initData() throws BeansException {
        // 初始化数据
        try {
            ConfigService configService = NacosUtils.getConfigServer();
            if (Objects.isNull(configService)) {
                log.error("[动态线程池][error]未获取到nacos连接器,configService为空,初始化动态配置失败!");
                return;
            }

            String dataId = dynamicProperties.getDataId();
            String groupId = dynamicProperties.getGroupId();
            if (StringUtils.isBlank(dataId) || StringUtils.isBlank(groupId)) {
                log.error("[动态线程池][error]获取到dataId-groupId为空,初始化动态配置失败!");
                return;
            }

            String config = configService.getConfig(dataId, groupId, 3000);
            log.info("[动态线程池]getConfig获取到配置为={}", config);
            if (StringUtils.isNotBlank(config)) {
                DynamicConfig dynamicConfigDTO = JSON.parseObject(config, DynamicConfig.class);
                BeanUtils.copyProperties(dynamicConfigDTO, dynamicConfig);

                AdjustExecutor.setDynamicConfig(dynamicConfig);
                // 首次初始化无需发送通知
                AdjustExecutor.resetThreadPoolConfig(false);
            }
        } catch (NacosException e) {
            log.error("getConfig拉取配置发生异常,error={}", e);
        }

        return;
    }


}
