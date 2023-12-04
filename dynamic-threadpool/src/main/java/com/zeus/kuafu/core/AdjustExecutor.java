package com.zeus.kuafu.core;

import com.alibaba.fastjson.JSON;
import com.zeus.kuafu.common.DynamicConstants;
import com.zeus.kuafu.common.RejectStrategy;
import com.zeus.kuafu.config.ThreadPoolExecutorConfig;
import com.zeus.kuafu.notify.DingNotifyService;
import com.zeus.kuafu.notify.EventLog;
import lombok.extern.slf4j.Slf4j;
import com.zeus.kuafu.config.DynamicConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;


import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @ClassName AdjustExecutor
 * @Description
 * @Author hjq
 * @Date 2023/3/17
 **/

@Slf4j
public class AdjustExecutor {

    public static final Map<String, ThreadPoolExecutor> POOL = new ConcurrentHashMap<>();

    private static DynamicConfig dynamicConfig;

    // @Autowired  静态方法无法通过Autowired注入
    public static void setDynamicConfig(DynamicConfig dynamicConfig) {
        AdjustExecutor.dynamicConfig = dynamicConfig;
    }

    public static DynamicConfig getDynamicConfig(){
        return dynamicConfig;
    }

    public static void putIfNotExist(String beanName, ThreadPoolExecutor executor){
        POOL.putIfAbsent(beanName, executor);
        log.info("[动态线程池][AdjustExecutor][putIfNotExist]POOL中添加beanName={}, executor={}", JSON.toJSONString(beanName), JSON.toJSONString(executor));
    }

    /**
     * 重置线程池配置项
     */
    public static void resetThreadPoolConfig(boolean canNotify) {
        log.info("[动态线程池][AdjustExecutor][resetThreadPoolConfig]POOL修改前为={}", JSON.toJSONString(POOL));
        // 修改已有配置
        try {
            List<ThreadPoolExecutorConfig> threadPoolConfigs = dynamicConfig.getThreadPoolConfigs();
            log.info("[动态线程池][AdjustExecutor][resetThreadPoolConfig]读取到的配置为threadPoolConfigs={}", JSON.toJSONString(threadPoolConfigs));
            if (CollectionUtils.isEmpty(threadPoolConfigs)){
                return;
            }
            Map<String, ThreadPoolExecutorConfig> configMap = threadPoolConfigs.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(ThreadPoolExecutorConfig::getName, Function.identity()));

            // 已有bean更新配置
            for (Map.Entry<String, ThreadPoolExecutorConfig> entry : configMap.entrySet()) {
                String beanName = StringUtils.trim(entry.getKey());
                if (!POOL.containsKey(beanName)){
                    continue;
                }

                ThreadPoolExecutorConfig executorConfig = entry.getValue();
                if (validateConfig(executorConfig)){
                    updateConfig(beanName, executorConfig, canNotify);
                }

            }

            log.info("[动态线程池]POOL修改后为={}", JSON.toJSONString(POOL));
        } catch (Exception e) {
            log.error("[动态线程池][error]resetThreadPoolConfig发生异常,", e);
        }

        // 新增bean动态创建
        // dynamicConfig.getDynamicThreadPool();

    }


    /**
     * 校验
     * @param config
     * @return
     */
    private static boolean validateConfig(ThreadPoolExecutorConfig config){
        if (Objects.isNull(config)){
            return false;
        }

        if (StringUtils.isEmpty(config.getName())){
            log.error("[动态线程池]配置异常,配置线程池名称为空");
            return false;
        }

        if (Objects.isNull(config.getCore()) || config.getCore() <= 0){
            log.error("[动态线程池]配置异常,核心线程数异常,{}配置值为={}", config.getName(), config.getCore());
            return false;
        }

        if (Objects.isNull(config.getMax()) || config.getMax() <= 0){
            log.error("[动态线程池]配置异常,最大线程数异常,{}配置值为={}", config.getName(), config.getMax());
            return false;
        }

        if (config.getMax() < config.getCore()){
            log.error("[动态线程池]配置异常,最大线程数小于核心线程数,{}配置核心线程数为={},最大线程数为={}",
                    config.getName(), config.getCore(), config.getMax());
            return false;
        }

        return true;
    }

    /**
     * 更新配置
     * @param beanName
     * @param config
     */
    private static void updateConfig(String beanName, ThreadPoolExecutorConfig config, boolean canNotify){

        ThreadPoolExecutor executorService = (ThreadPoolExecutor) POOL.get(beanName);
        if (Objects.isNull(executorService) || Objects.isNull(config)) {
            return;
        }
        EventLog beforeEventLog = EventLog.writeEventLog(beanName, executorService);
        log.info("[动态线程池]修改之前线程池快照,beforeEventLog={}", JSON.toJSONString(beforeEventLog));

        updateCoreAndMax(executorService, config);
        updateKeepAliveTime(executorService, config);
        allowRecycleCore(executorService, config);
        updateRejectStrategy(executorService, config);
        resetPoolSize(executorService, config);

        EventLog afterEventLog = EventLog.writeEventLog(beanName, executorService);
        log.info("[动态线程池]修改之后线程池快照,afterEventLog={}", JSON.toJSONString(afterEventLog));
        String diffLog = EventLog.getDiffLog(beforeEventLog, afterEventLog);
        log.info("[动态线程池]updateConfig diffLog={}", JSON.toJSONString(diffLog));

        // 启用推送
        if (canNotify) {
            DingNotifyService.sendMsg(DynamicConstants.CHANGE_NOTIFY, diffLog);
        }

    }

    /**
     * 设置核心数和最大数
     * @param executorService
     * @param config
     */
    private static void updateCoreAndMax(ThreadPoolExecutor executorService, ThreadPoolExecutorConfig config){

        try {
            // 调小最大数的情况下优先设置核心数
            if (Objects.nonNull(config.getCore()) && Objects.nonNull(config.getMax())
                    && config.getMax() < executorService.getMaximumPoolSize()){
                if (!config.getCore().equals(executorService.getCorePoolSize())){
                    executorService.setCorePoolSize(config.getCore());
                    log.info("[动态线程池][updateCoreAndMax]修改{}核心线程数成功", config.getName());
                }
                if (!config.getMax().equals(executorService.getMaximumPoolSize())){
                    executorService.setMaximumPoolSize(config.getMax());
                    log.info("[动态线程池][updateCoreAndMax]修改{}最大线程数成功", config.getName());
                }
                return;
            }

            // 优先设置最大线程数,避免瞬时将核心数调整为大于最大数的情况
            if (Objects.nonNull(config.getMax()) && !config.getMax().equals(executorService.getMaximumPoolSize())){
                executorService.setMaximumPoolSize(config.getMax());
                log.info("[动态线程池][updateCoreAndMax]修改{}最大线程数成功", config.getName());
            }
            if (Objects.nonNull(config.getCore()) && !config.getCore().equals(executorService.getCorePoolSize())){
                executorService.setCorePoolSize(config.getCore());
                log.info("[动态线程池][updateCoreAndMax]修改{}核心线程数成功", config.getName());
            }
        } catch (IllegalArgumentException e){
            log.error("[动态线程池][error]最大线程数配置异常,poolName={},errorMsg={}", config.getName(), e);
        } catch (Exception e) {
            log.error("[动态线程池][error]修改线程池核心或最大数量发生异常,poolName={},errorMsg={}", config.getName(), e);
        }

    }

    /**
     * 设置存活时间
     * @param executorService
     * @param config
     */
    private static void updateKeepAliveTime(ThreadPoolExecutor executorService, ThreadPoolExecutorConfig config){
        if (Objects.nonNull(config.getKeepAliveSeconds()) && config.getKeepAliveSeconds() > 0
                && !config.getKeepAliveSeconds().equals(executorService.getKeepAliveTime(TimeUnit.SECONDS))){
            executorService.setKeepAliveTime(config.getKeepAliveSeconds(), TimeUnit.SECONDS);
            log.info("[动态线程池][updateKeepAliveTime]修改{}空闲线程存活时间成功, 修改为={}s", config.getName(),executorService.getKeepAliveTime(TimeUnit.SECONDS));
        }
    }

    /**
     * 是否允许核心数回收
     * @param executorService
     * @param config
     */
    private static void allowRecycleCore(ThreadPoolExecutor executorService, ThreadPoolExecutorConfig config){
        if (Objects.nonNull(config.isRecycleCore()) && !Objects.equals(executorService.allowsCoreThreadTimeOut(), config.isRecycleCore())){
            executorService.allowCoreThreadTimeOut(config.isRecycleCore());
            log.info("[动态线程池][allowRecycleCore]修改{}允许回收核心线程为={}", config.getName(),executorService.allowsCoreThreadTimeOut());
        }
    }

    /**
     * 更新拒绝策略
     * @param executorService
     * @param config
     */
    private static void updateRejectStrategy(ThreadPoolExecutor executorService, ThreadPoolExecutorConfig config){
        if (StringUtils.isBlank(config.getRejectStrategy())) {
            return;
        }

        String rejectName = executorService.getRejectedExecutionHandler().getClass().getSimpleName();
        if (!Objects.equals(rejectName, config.getRejectStrategy())){
            RejectedExecutionHandler rejectedExecutionHandler = RejectStrategy.getRejectStrategyByType(config.getRejectStrategy());
            executorService.setRejectedExecutionHandler(rejectedExecutionHandler);
            log.info("[动态线程池][updateRejectStrategy]修改{}拒绝策略,由{}修改为{}", config.getName(), rejectName, executorService.getRejectedExecutionHandler().getClass().getSimpleName());
        }

    }

    /**
     * 更新线程池队列长度
     * @param executorService
     * @param config
     */
    private static void resetPoolSize(ThreadPoolExecutor executorService, ThreadPoolExecutorConfig config) {
        if (Objects.isNull(config.getQueueSize())) {
            return;
        }

        try {
            BlockingQueue<Runnable> queue = executorService.getQueue();
            boolean canAdjustQueue = queue instanceof AdjustableLinkedBlockingQueue;
            log.info("[动态线程池]修改线程池队列长度,queue={},是否为AdjustableLinkedBlockingQueue={}", JSON.toJSONString(queue), canAdjustQueue);
            if (!(queue instanceof AdjustableLinkedBlockingQueue)) {
                return;
            }

            int capacity = queue.size() + queue.remainingCapacity();
            if (!Objects.equals(capacity, config.getQueueSize())) {
                ((AdjustableLinkedBlockingQueue<Runnable>) queue).setCapacity(config.getQueueSize());
                log.info("[动态线程池][resetPoolSize]修改线程池队列长度由{}修改为{}", capacity, config.getQueueSize());
            }
        } catch (Exception e) {
            log.error("[动态线程池][error]更新线程池队列长度发生异常,poolName={},errorMsg={}", config.getName(), e);
        }


    }

}
