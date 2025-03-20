package xj.core.threadPool;

import xj.component.log.LogManager;
import xj.abstracts.thread.ThreadTask;
import xj.core.server.selector.SelectorChannel;
import xj.tool.Constant;
import xj.tool.StrPool;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

// 工作线程，即动态线程池的线程对象，用于将线程对象化、执行线程任务、接受线程池创建与管理
public class WorkingThread extends Thread {

    // 成员属性
    private ThreadTask workingTask;// 当前执行的线程任务

    private boolean enable = true;// 当前线程是否为运行状态

    private boolean commonThread = false;// 当前线程是否为普通线程

    private final Object lock = new Object(),dataLock = new Object();// 线程锁 数据分析锁

    private LocalTime timer;// 等待超时计时器

    private String fromChannelId;// 任务来源channel的Id

    private long threadStartRunTime,threadStartWaitTime;// 线程开始运行时间 线程开始等待时间

    private List<Long> waitingTimes;// 线程等待时间列表

    // 成员方法
    // 初始化
    public WorkingThread(String name,boolean commonThread){
        super(name);
        this.commonThread = commonThread;
        timer = LocalTime.now();
        threadStartWaitTime = System.currentTimeMillis();
        waitingTimes = new ArrayList<>();
        start();
    }

    // 工作线程执行内容
    @Override
    public void run() {
        LogManager.info_("[{}] 正在启动...",getName());
        while(true){
            synchronized (lock){
                if(workingTask != null){
                    // 接收任务前的数据处理
                    synchronized (dataLock){
                        waitingTimes.add(System.currentTimeMillis()-threadStartWaitTime);
                        threadStartWaitTime = 0;
                        threadStartRunTime = System.currentTimeMillis();
                    }
                    // 当前线程有任务时，执行线程任务
                    workingTask.doTask();
                    // 线程执行任务完成后，数据处理
                    synchronized (dataLock){
                        timer = LocalTime.now();
                        threadStartWaitTime = System.currentTimeMillis();
                        threadStartRunTime = 0;
                        workingTask = null;
                        fromChannelId = null;
                    }
                }else if(!ThreadPoolManager.getInstance().queueEmpty()){
                    // 当前线程没有任务但是任务队列有任务存在
                    // 读取队列任务
                    receiveThreadTask(ThreadPoolManager.getInstance().getQueueTask());
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
        LogManager.info_("[{}] 结束运行...",getName());
    }

    // 设置线程任务
    public void setWorkingTask(ThreadTask task){
        synchronized (lock){
            receiveThreadTask(task);
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

    // 接收线程任务
    private void receiveThreadTask(ThreadTask task){
        synchronized (dataLock){
            // 接收任务
            workingTask = task;
            timer = LocalTime.now();
            // 获取任务的来源channelId
            fromChannelId = task.getChannelId();
        }
    }

    // 返回线程基础信息
    public Map<String,Object> returnThreadInfo(){
        Map<String, Object> ret = new HashMap<>();
        synchronized (dataLock){
            long runningTime = threadStartRunTime > 0 ? System.currentTimeMillis() - threadStartRunTime : 0;
            long waitingTime = threadStartWaitTime > 0 ? System.currentTimeMillis() - threadStartWaitTime : 0;
            long avgWaitingTime = waitingTimes.stream().mapToInt(Long::intValue).sum() + waitingTime
                    / (waitingTimes.size() + 1);
            ret.put("来源channel",fromChannelId == null ? StrPool.NONE : fromChannelId);
            ret.put("线程任务名",workingTask == null? StrPool.NONE : workingTask.getLogDescribe());
            ret.put("线程名",getName());
            ret.put("线程类别",commonThread ? StrPool.THREAD_COMMON : StrPool.THREAD_CORE);
            ret.put("状态",runningTime == 0 ? StrPool.THREAD_WAITING : StrPool.THREAD_RUNNING);
            ret.put("线程当前运行时间(毫秒)",runningTime);
            ret.put("线程当前等待时间(毫秒)",waitingTime);
            ret.put("线程平均等待时间(毫秒)",avgWaitingTime);
        }
        return ret;
    }

}
