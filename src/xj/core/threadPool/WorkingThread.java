package xj.core.threadPool;

import xj.component.log.LogManager;
import xj.interfaces.thread.ThreadTask;

// 工作线程，即动态线程池的线程对象，用于将线程对象化、执行线程任务、接受线程池创建与管理
public class WorkingThread extends Thread {

    // 成员属性
    private ThreadTask workingTask;// 当前执行的线程任务

    private boolean enable = true;// 当前线程是否为运行状态

    private Object lock = new Object();// 线程锁

    // 成员行为
    // 初始化
    public WorkingThread(String name){
        super(name);
        start();
    }

    // 工作线程执行内容
    @Override
    public void run() {
        LogManager.info("工作线程正式启动，线程名",getName());
        while(enable){
            synchronized (lock){
                if(workingTask != null){
                    workingTask.doTask();
                    workingTask = null;
                }
            }
        }
        LogManager.info("工作线程结束，线程名",getName());
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

    //
}
