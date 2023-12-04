package com.zeus.kuafu.config;

import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName ThreadPoolExecutorConfig
 * @Description
 * @Author hjq
 * @Date 2023/3/17
 **/

@Data
public class ThreadPoolExecutorConfig implements Serializable {

    /**
     * 线程池名称
     */
    private String name;

    /**
     * 核心线程数
     */
    private Integer core;

    /**
     * 最大线程数
     */
    private Integer max;

    /**
     * 空闲线程存活时间.单位:秒
     */
    private Integer keepAliveSeconds;

    /**
     * 是否回收核心线程数
     */
    private boolean recycleCore;

    /**
     * 拒绝策略
     * 参照：@see com.zeus.kuafu.common.RejectStrategy 中常量
     */
    private String rejectStrategy;

    /**
     * 队列长度
     */
    private Integer queueSize;


}
