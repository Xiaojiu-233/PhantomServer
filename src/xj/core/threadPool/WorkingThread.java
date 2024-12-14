package xj.core.threadPool;

import xj.component.log.LogManager;
import xj.interfaces.thread.ThreadTask;
import xj.tool.Constant;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

// 工作线程，即动态线程池的线程对象，用于将线程对象化、执行线程任务、接受线程池创建与管理
public class WorkingThread extends Thread {

    // 成员属性
    private ThreadTask workingTask;// 当前执行的线程任务

    private boolean enable = true;// 当前线程是否为运行状态

    private boolean commonThread = false;// 当前线程是否为普通线程

    private final Object lock = new Object();// 线程锁

    private LocalTime timer;// 超时计时器

    // 成员方法
    // 初始化
    public WorkingThread(String name,boolean commonThread){
        super(name);
        this.commonThread = commonThread;
        timer = LocalTime.now();
        start();
    }

    // 工作线程执行内容
    @Override
    public void run() {
        LogManager.info("[{}] 正在启动...",getName());
        while(true){
            synchronized (lock){
                if(workingTask != null){
                    // 当前线程有任务时，执行线程任务
                    LogManager.info("[{}] 收到任务：{}",getName(),workingTask.getLogDescribe());
                    workingTask.doTask();
                    LogManager.info("[{}] 完成任务：{}",getName(),workingTask.getLogDescribe());
                    workingTask = null;
                }else if(!ThreadPoolManager.getInstance().queueEmpty()){
                    // 当前线程没有任务但是任务队列有任务存在
                    // 读取队列任务
                    workingTask = ThreadPoolManager.getInstance().getQueueTask();
                    timer = LocalTime.now();
                }else if(commonThread){
                    // 当前线程没有任务同时也没有队列任务，且该线程为普通线程
                    // 开始计时，如果时间超过最大闲置时间，回收该线程
                    long time = ChronoUnit.SECONDS.between(timer,LocalTime.now());
                    if(time < 0) time += Constant.DAY_SECONDS;
                    if(time >= ThreadPoolManager.getInstance().getThreadMaxFreeTime()){
                        stopThread();
                    }
                }
                // 当前线程是否可运作
                if(!enable)break;
            }
        }
        LogManager.info("[{}] 结束运行...",getName());
    }

    // 设置线程任务
    public void setWorkingTask(ThreadTask task){
        synchronized (lock){
            workingTask = task;
            timer = LocalTime.now();
        }
    }

    // 回收该线程
    private void stopThread(){
        // 断开与线程池的连接
        ThreadPoolManager.getInstance().delCommonPoolThread(this);
        // 结束线程循环
        enable = false;
    }

    // 该线程是否空闲
    public boolean isFreeThread(){
        return workingTask == null;
    }
}
