package com.zeus.kuafu.config;

import com.zeus.kuafu.common.DynamicConstants;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName DynamicConfig
 * @Description
 * @Author hjq
 * @Date 2023/3/17
 **/

@Data
public class DynamicConfig implements Serializable {

    /**
     * 配置项
     */
    private List<ThreadPoolExecutorConfig> threadPoolConfigs;

    /**
     * 监控采集间隔时间，单位:秒
     */
    private Integer collectTime = DynamicConstants.DEFAULT_INTERVAL;

    /**
     * 接收通知变更的机器列表
     */
    private List<String> notifyList = new ArrayList<>();


}
