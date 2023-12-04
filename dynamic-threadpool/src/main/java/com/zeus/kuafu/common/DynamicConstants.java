package com.zeus.kuafu.common;

/**
 * @ClassName DynamicConstonts
 * @Description
 * @Author hjq
 * @Date 2023/3/16
 **/


public class DynamicConstants {

    public static final String CONFIG_PREFIX = "dynamic.thread.pool";

    public static final Integer INT_ZERO = 0;

    public static final Integer DEFAULT_INTERVAL = 1;

    public static final String CHANGE_NOTIFY = "变更通知";

    public static final String DING_CHANGE_NOTICE_TEMPLATE =
            "动态线程池参数变更通知 \n\n " +
                    "ip：%s \n\n " +
                    "线程池名称：%s \n\n " +
                    "核心线程数：%s => %s \n\n " +
                    "最大线程数：%s => %s \n\n " +
                    "允许核心线程超时：%s => %s \n\n " +
                    "线程存活时间：%ss => %ss \n\n " +
                    "队列类型：%s \n\n " +
                    "队列容量：%s => %s \n\n " +
                    "拒绝策略：%s => %s \n\n " +
                    "通知时间：%s \n\n";



}
