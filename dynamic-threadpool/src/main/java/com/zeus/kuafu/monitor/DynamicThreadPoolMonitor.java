package com.zeus.kuafu.monitor;

import com.zeus.kuafu.common.DynamicConstants;
import com.zeus.kuafu.common.InetUtils;
import lombok.extern.slf4j.Slf4j;
import com.zeus.kuafu.core.AdjustExecutor;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName DynamicThreadPoolMonitor
 * @Description
 * @Author hjq
 * @Date 2023/3/16
 **/

@Slf4j
public class DynamicThreadPoolMonitor implements ApplicationRunner, Ordered {

    private static final ScheduledExecutorService schedulerTaskPool = Executors.newScheduledThreadPool(1);

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        log.info("监控启动成功");

        // 获取间隔时间
        Integer interval = DynamicConstants.DEFAULT_INTERVAL;
        if (Objects.nonNull(AdjustExecutor.getDynamicConfig())) {
            interval = AdjustExecutor.getDynamicConfig().getCollectTime();
        }

        schedulerTaskPool.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Map<String, ThreadPoolExecutor> pool = new HashMap<>(AdjustExecutor.POOL);
                if (MapUtils.isEmpty(pool)) {
                    return;
                }

                for (Map.Entry<String, ThreadPoolExecutor> entry : pool.entrySet()) {
                    printLog(entry.getKey(), entry.getValue());
                }

            }
        }, 2, interval, TimeUnit.SECONDS);

    }

    /**
     * 打印日志
     * @param poolName
     * @param executor
     */
    private void printLog(String poolName, ThreadPoolExecutor executor) {
        if (StringUtils.isBlank(poolName) || Objects.isNull(executor)) {
            return;
        }

        String localIp = InetUtils.getLocalIp();
        int queueSize = executor.getQueue().size();
        int remainingQueueSize = executor.getQueue().remainingCapacity();
        // int queueRatio = queueSize * 100 / (queueSize + remainingQueueSize);

        log.info("[动态线程池][thread_pool_monitor]monitor:|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}",
                localIp, // 本机ip
                poolName,  // 线程池名称
                executor.getCorePoolSize(),  // 核心数
                executor.getMaximumPoolSize(),  // 最大数
                executor.getPoolSize(),  // 当前线程数量，如果允许核心线程空闲回收则有可能获取到0
                executor.getActiveCount(),  // 活跃线程数量,即正在执行任务的线程数量,不包括处于空闲状态的线程
                executor.getTaskCount(),  // 任务总数
                executor.getQueue().getClass().getSimpleName(),  // 队列类型
                calculateRatio(queueSize, (queueSize + remainingQueueSize)),  // 队列已使用比例
                calculateRatio(executor.getActiveCount(), executor.getMaximumPoolSize()),  // 线程活跃度
                queueSize + remainingQueueSize,  // 任务队列的总容量
                queueSize,  // 当前任务队列中等待执行的任务数量，检测线程池中是否还有等待执行的任务
                remainingQueueSize,  // 任务队列的剩余容量，可以用来检测任务队列是否已经满了
                executor.getCompletedTaskCount(), // 从线程池创建以来已经完成执行的任务数量,包含被取消或者异常终止的任务
                executor.getLargestPoolSize(),  // 线程池自创建以来池中曾经同时存在的最大线程数
                executor.getKeepAliveTime(TimeUnit.SECONDS),  // 空闲存活时间
                executor.getRejectedExecutionHandler().getClass().getSimpleName()  // 拒绝策略
        );
    }

    /**
     * 计算百分比
     * @param size
     * @param total
     * @return
     */
    private String calculateRatio(int size, int total) {
        if (size == 0 || total == 0) {
            return "0.00";
        }
        double ratio = (double) size / total * 100;
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(ratio);
    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE+1;
    }
}
