package xj.interfaces.thread;

/**
 * 线程的工作任务接口，将线程需要处理的任务对象化以达到执行、运输、多样化的功能
 */
public interface ThreadTask {

    /**
     * 执行任务（从开始到回收）
     */
    public void doTask();

    /**
     * 任务被抛弃时执行程序
     */
    public void doDestroy();

    /**
     * 任务说明（用于在日志中表达）
     */
    public String getLogDescribe();
}
