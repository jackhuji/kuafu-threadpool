package com.zeus.kuafu.common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @ClassName DynamicProperties
 * @Description
 * @Author hjq
 * @Date 2023/3/16
 **/

@Data
@ConfigurationProperties(prefix = DynamicConstants.CONFIG_PREFIX)
public class DynamicProperties {

    /**
     * 总开关
     */
    private boolean enable = true;

    /**
     * diamond配置：dataId
     */
    private String dataId;

    /**
     * diamond配置：groupId
     */
    private String groupId;

    /**
     * 钉钉webhook
     */
    private String webHook;

}
