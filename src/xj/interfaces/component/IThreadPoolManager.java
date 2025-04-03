package xj.interfaces.component;

import xj.abstracts.thread.ThreadTask;

/**
    线程池管理器的接口形式，为拓展程序用户提供自制线程任务管理的接口
 */
public interface IThreadPoolManager {

    /**
     * 接收与处理线程任务
     * @param task 线程任务
     */
    void putThreadTask(ThreadTask task);

}
