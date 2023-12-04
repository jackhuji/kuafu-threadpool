package com.zeus.kuafu.common;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @ClassName RejectStrategy
 * @Description
 * @Author Jack.hu
 * @Date 2023/5/22
 **/

@Slf4j
public class RejectStrategy {

    /**
     * JDK默认的拒绝策略有四种：
     * AbortPolicy：丢弃任务并抛出RejectedExecutionException异常。
     * DiscardPolicy：丢弃任务，但是不抛出异常。可能导致无法发现系统的异常状态。
     * DiscardOldestPolicy：丢弃队列最前面的任务，然后重新提交被拒绝的任务。
     * CallerRunsPolicy：由调用线程处理该任务。
     *
     */

    public static final String AbortPolicy = "AbortPolicy";

    public static final String DiscardPolicy = "DiscardPolicy";

    public static final String DiscardOldestPolicy = "DiscardOldestPolicy";

    public static final String CallerRunsPolicy = "CallerRunsPolicy";

    /**
     * 获取拒绝策略
     * @param reject
     * @return
     */
    public static RejectedExecutionHandler getRejectStrategyByType(String reject){
        // 使用AbortPolicy兜底
        RejectedExecutionHandler result = new ThreadPoolExecutor.AbortPolicy();
        if (StringUtils.equalsIgnoreCase(AbortPolicy, reject)){
            result = new ThreadPoolExecutor.AbortPolicy();
        }

        if (StringUtils.equalsIgnoreCase(DiscardPolicy, reject)){
            result = new ThreadPoolExecutor.DiscardPolicy();
        }

        if (StringUtils.equalsIgnoreCase(DiscardOldestPolicy, reject)){
            result = new ThreadPoolExecutor.DiscardOldestPolicy();
        }

        if (StringUtils.equalsIgnoreCase(CallerRunsPolicy, reject)){
            result = new ThreadPoolExecutor.CallerRunsPolicy();
        }

        return result;
    }

}
