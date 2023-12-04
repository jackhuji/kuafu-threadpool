package com.zeus.kuafu.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.zeus.kuafu.common.DynamicConstants;
import com.zeus.kuafu.common.DynamicProperties;
import com.zeus.kuafu.common.InetUtils;
import com.zeus.kuafu.core.AdjustExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.util.CollectionUtils;

import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * @ClassName DynamicThreadPoolStarter
 * @Description
 * @Author hjq
 * @Date 2023/3/20
 **/

@Slf4j
public class DynamicConfigListener implements ApplicationRunner, Ordered {

    @Autowired
    private DynamicProperties dynamicProperties;

    @Autowired
    private DynamicConfig dynamicConfig;

    /**
     * 应用启动首次变更不通知
     */
    private static Integer startFlag = 0;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (Boolean.FALSE.equals(dynamicProperties.isEnable())) {
            return;
        }

        String dataId = dynamicProperties.getDataId();
        String groupId = dynamicProperties.getGroupId();
        if (StringUtils.isNotBlank(dataId) && StringUtils.isNotBlank(groupId)) {
            // 添加监听
            try {
                ConfigService configService = NacosUtils.getConfigServer();
                if (Objects.isNull(configService)) {
                    log.error("[动态线程池][error]未获取到nacos连接器,configService为空,初始化动态配置失败!");
                    return;
                }

                log.info("[动态线程池]DynamicThreadPoolStarter 开始启动监听,dataId={},groupId={}", dataId, groupId);
                configService.addListener(dataId, groupId, new Listener() {
                    @Override
                    public Executor getExecutor() {
                        return null;
                    }

                    @Override
                    public void receiveConfigInfo(String configInfo) {
                        if (StringUtils.isNotBlank(configInfo)) {
                            DynamicConfig dynamicConfigDTO = JSON.parseObject(configInfo, DynamicConfig.class);
                            BeanUtils.copyProperties(dynamicConfigDTO, dynamicConfig);

                            AdjustExecutor.setDynamicConfig(dynamicConfig);
                            AdjustExecutor.resetThreadPoolConfig(canNotify());
                        }
                    }
                });
            } catch (NacosException e) {
                log.error("[动态线程池][error]DynamicThreadPoolStarter 启动diamond监听失败");
            }
        }

    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }

    /**
     * 变更是否机器人通知
     * @return
     */
    private boolean canNotify(){

        if (DynamicConstants.INT_ZERO.equals(startFlag++)) {
            return false;
        }

        if (Objects.isNull(dynamicConfig) || CollectionUtils.isEmpty(dynamicConfig.getNotifyList())) {
            return false;
        }

        String localIp = InetUtils.getLocalIp();
        if (dynamicConfig.getNotifyList().contains(localIp)) {
            return true;
        }

        return false;
    }
}
