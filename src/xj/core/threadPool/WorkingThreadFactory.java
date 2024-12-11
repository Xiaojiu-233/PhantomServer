package xj.core.threadPool;

import xj.tool.StrPool;

// 工作线程的工厂，使用工厂模式生产工作线程的对象
public class WorkingThreadFactory {

    // 成员属性
    private String threadName = "";// 工作线程名称
    private int threadCounter = 0;// 线程计数器，用于线程命名

    // 成员行为
    // 初始化
    public WorkingThreadFactory(String threadName){
        this.threadName = threadName;
    }

    // 生产线程对象
    public WorkingThread productThread(){
        return new WorkingThread(threadName + StrPool.HYPHEN + threadCounter++);
    }
}
