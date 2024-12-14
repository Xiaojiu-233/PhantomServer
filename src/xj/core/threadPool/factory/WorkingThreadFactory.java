package xj.core.threadPool.factory;

import xj.core.threadPool.WorkingThread;
import xj.tool.StrPool;

// 工作线程的工厂，使用工厂模式生产工作线程的对象
public class WorkingThreadFactory {

    // 成员属性
    private String threadName = "";// 工作线程名称

    private int threadCounter = 0;// 线程计数器，用于线程命名

    // 成员方法
    // 初始化
    public WorkingThreadFactory(String threadName){
        this.threadName = threadName;
    }

    // 生产核心线程对象
    public WorkingThread productCoreThread(){
        return new WorkingThread(threadName + StrPool.HYPHEN + threadCounter++,false);
    }

    // 生产普通线程对象
    public WorkingThread productCommonThread(){
        return new WorkingThread(threadName + StrPool.HYPHEN + threadCounter++,true);
    }
}
