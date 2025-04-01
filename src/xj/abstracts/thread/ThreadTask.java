package xj.abstracts.thread;

/**
 * 线程的工作任务接口，将线程需要处理的任务对象化以达到执行、运输、多样化的功能
 * */
public abstract class ThreadTask {

    // 成员属性
    private String channelId;// channel的Id

    private long queueWaitStartTime;// 开始在队列等待的时间戳

    // 成员方法
    /**
     * 执行任务（从开始到回收）
     * */
    public abstract void doTask();

    /**
     * 任务被抛弃时执行程序
     * */
    public abstract void doDestroy();

    /**
     * 任务说明（用于在日志中表达）
     * */
    public abstract String getLogDescribe();

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public long getQueueWaitStartTime() {
        return queueWaitStartTime;
    }

    public void setQueueWaitStartTime(long queueWaitStartTime) {
        this.queueWaitStartTime = queueWaitStartTime;
    }
}
