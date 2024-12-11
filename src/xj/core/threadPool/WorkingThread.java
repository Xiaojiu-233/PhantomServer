package xj.core.threadPool;

import xj.component.log.LogManager;
import xj.interfaces.thread.ThreadTask;

// 工作线程，即动态线程池的线程对象，用于将线程对象化、执行线程任务、接受线程池创建与管理
public class WorkingThread extends Thread {

    // 成员属性
    private ThreadTask workingTask;// 当前执行的线程任务

    private boolean enable = true;// 当前线程是否为运行状态

    private final Object lock = new Object();// 线程锁

    // 成员行为
    // 初始化
    public WorkingThread(String name){
        super(name);
        start();
    }

    // 工作线程执行内容
    @Override
    public void run() {
        LogManager.info("[{}] 正在启动",getName());
        while(true){
            synchronized (lock){
                // 当前线程是否可运作
                if(!enable)break;
                // 当前线程执行线程任务
                if(workingTask != null){
                    LogManager.info("[{}] 收到任务：{}",getName(),workingTask.getLogDescribe());
                    workingTask.doTask();
                    LogManager.info("[{}] 完成任务：{}",getName(),workingTask.getLogDescribe());
                    workingTask = null;
                }
            }
        }
        LogManager.info("[{}] 结束运行",getName());
    }

    // 设置线程任务
    public void setWorkingTask(ThreadTask task){
        synchronized (lock){
            workingTask = task;
        }
    }

    // 停止该线程
    public void stopThread(){
        synchronized (lock) {
            enable = false;
        }
    }

    // 该线程是否空闲
    public boolean isFreeThread(){
        return workingTask == null;
    }
}
