package com.zeus.kuafu.notify;

import com.zeus.kuafu.common.DynamicConstants;
import com.zeus.kuafu.common.InetUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName EventLog
 * @Description 用于记录线程池的瞬时状态
 * @Author Jack.hu
 * @Date 2023/6/15
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventLog {

    /**
     * 记录 线程池名称
     */
    private String poolName;

    /**
     * 记录 核心数
     */
    private Integer coreSize;

    /**
     * 记录 最大数
     */
    private Integer maxSize;

    /**
     * 记录 存活时间,单位：s
     */
    private long keepAliveTime;

    /**
     * 记录 是否允许回收核心线程
     */
    private boolean allowRecycleCore;

    /**
     * 记录 队列类型
     */
    private String queueType;

    /**
     * 记录 队列总长度
     */
    private Integer queueSize;

    /**
     * 记录 拒绝策略
     */
    private String rejectStrategy;

    /**
     * 记录时间(记录时间戳，单位ms)
     */
    private long logTime;

    /**
     * 记录log
     * @param beanName
     * @param executorService
     * @return
     */
    public static EventLog writeEventLog(String beanName, ThreadPoolExecutor executorService){
        if (Objects.isNull(executorService)) {
            return null;
        }

        EventLog log = new EventLog();
        log.setPoolName(beanName);
        log.setCoreSize(executorService.getCorePoolSize());
        log.setMaxSize(executorService.getMaximumPoolSize());
        log.setKeepAliveTime(executorService.getKeepAliveTime(TimeUnit.SECONDS));
        log.setAllowRecycleCore(executorService.allowsCoreThreadTimeOut());
        log.setQueueType(executorService.getQueue().getClass().getSimpleName());
        log.setQueueSize(executorService.getQueue().size() + executorService.getQueue().remainingCapacity());
        log.setRejectStrategy(executorService.getRejectedExecutionHandler().getClass().getSimpleName());
        log.setLogTime(System.currentTimeMillis());

        return log;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EventLog eventLog = (EventLog) o;

        if (keepAliveTime != eventLog.keepAliveTime) return false;
        if (allowRecycleCore != eventLog.allowRecycleCore) return false;
        if (poolName != null ? !poolName.equals(eventLog.poolName) : eventLog.poolName != null) return false;
        if (coreSize != null ? !coreSize.equals(eventLog.coreSize) : eventLog.coreSize != null) return false;
        if (maxSize != null ? !maxSize.equals(eventLog.maxSize) : eventLog.maxSize != null) return false;
        if (queueType != null ? !queueType.equals(eventLog.queueType) : eventLog.queueType != null) return false;
        if (queueSize != null ? !queueSize.equals(eventLog.queueSize) : eventLog.queueSize != null) return false;
        if (rejectStrategy != null ? !rejectStrategy.equals(eventLog.rejectStrategy) : eventLog.rejectStrategy != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = poolName != null ? poolName.hashCode() : 0;
        result = 31 * result + (coreSize != null ? coreSize.hashCode() : 0);
        result = 31 * result + (maxSize != null ? maxSize.hashCode() : 0);
        result = 31 * result + (int) (keepAliveTime ^ (keepAliveTime >>> 32));
        result = 31 * result + (allowRecycleCore ? 1 : 0);
        result = 31 * result + (queueType != null ? queueType.hashCode() : 0);
        result = 31 * result + (queueSize != null ? queueSize.hashCode() : 0);
        result = 31 * result + (rejectStrategy != null ? rejectStrategy.hashCode() : 0);
        return result;
    }

    /**
     * 获取diff
     * @param before
     * @param after
     * @return
     */
    public static String getDiffLog(EventLog before, EventLog after){
        if (Objects.isNull(before) || Objects.isNull(after)) {
            return null;
        }

        if (before.equals(after)) {
            return null;
        }

        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(after.getLogTime()));
        String msg = String.format(DynamicConstants.DING_CHANGE_NOTICE_TEMPLATE,
                InetUtils.getLocalIp(),
                before.getPoolName(),
                before.getCoreSize(), after.getCoreSize(),
                before.getMaxSize(), after.getMaxSize(),
                before.isAllowRecycleCore(), after.isAllowRecycleCore(),
                before.getKeepAliveTime(), after.getKeepAliveTime(),
                before.getQueueType(),
                before.getQueueSize(), after.getQueueSize(),
                before.getRejectStrategy(), after.getRejectStrategy(),
                time
        );


        return msg;
    }

    // public static void main(String[] args) {
    //
    //     EventLog log1 = EventLog.builder()
    //             .poolName("abc")
    //             .coreSize(1)
    //             .maxSize(10)
    //             .keepAliveTime(20)
    //             .allowRecycleCore(true)
    //             .queueType("queue1")
    //             .queueSize(30)
    //             .rejectStrategy("default")
    //             .logTime(System.currentTimeMillis())
    //             .build();
    //
    //     EventLog log2 = EventLog.builder()
    //             .poolName("abc")
    //             .coreSize(1)
    //             .maxSize(10)
    //             .keepAliveTime(20)
    //             .allowRecycleCore(true)
    //             .queueType("queue1")
    //             .queueSize(100)
    //             .rejectStrategy("default")
    //             .logTime(System.currentTimeMillis()-1000)
    //             .build();
    //
    //     String diffLog = getDiffLog(log1, log2);
    //     System.out.println(diffLog);
    // }


}
