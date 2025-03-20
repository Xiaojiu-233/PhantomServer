package xj.core.threadPool;

import xj.component.conf.ConfigureManager;
import xj.component.log.LogManager;
import xj.core.threadPool.factory.*;
import xj.entity.monitor.MonitorChart;
import xj.enums.thread.RejectStrategy;
import xj.abstracts.thread.ThreadTask;
import xj.tool.ConfigPool;
import xj.tool.Constant;
import xj.tool.StrPool;

import java.util.*;

// 动态线程池管理器，用于管理服务器内部的工作线程
public class ThreadPoolManager {

    // 成员属性
    private static volatile ThreadPoolManager instance;// 单例模式实现

    private int maxThread = 0;// 最大线程数
    private int coreThread = 0;// 最大线程数
    private int queueCapacity = 0;// 任务队列大小
    private int threadMaxFreeTime;// 普通线程最大闲置时间，超时会被回收
    private String threadName = "";// 工作线程名称
    private RejectStrategy strategy = RejectStrategy.THROW_TASK;// 拒绝策略，默认为抛弃任务

    private WorkingThreadFactory threadFactory;// 工作线程工厂
    private List<WorkingThread> coreThreadPool;// 核心线程池
    private List<WorkingThread> commonThreadPool;// 普通线程池
    private Queue<ThreadTask> threadTaskQueue;// 线程任务队列
    private final Object queueLock = new Object();// 队列锁
    private final Object commonPoolLock = new Object();// 普通线程池锁
    private MonitorChart commonThreadChart, allThreadChart, recycledThreadChart, queueTaskChart;
    // 普通线程数图表 所有线程数图表 回收线程数图表 队列任务数图表

    // 成员方法
    // 初始化
    public ThreadPoolManager() {
        LogManager.info_("【线程池模块】开始初始化");
        initConfig();
        initContainer();
        initFactory();
        LogManager.info_("【线程池模块】初始化完毕");
    }

    // 读取配置文件
    private void initConfig(){
        LogManager.info_("【线程池模块】正在读取配置文件...");
        // 数据读取
        maxThread = (int) ConfigureManager.getInstance().getConfig(ConfigPool.THREAD_POOL.MAX_THREAD);
        coreThread = (int) ConfigureManager.getInstance().getConfig(ConfigPool.THREAD_POOL.CORE_THREAD);
        queueCapacity = (int) ConfigureManager.getInstance().getConfig(ConfigPool.THREAD_POOL.QUEUE_CAPACITY);
        threadMaxFreeTime = (int) ConfigureManager.getInstance().getConfig(ConfigPool.THREAD_POOL.MAX_FREE_TIME);
        threadName = (String) ConfigureManager.getInstance().getConfig(ConfigPool.THREAD_POOL.THREAD_NAME);
        strategy = RejectStrategy.getStrategyByString((String)ConfigureManager.getInstance()
                .getConfig(ConfigPool.THREAD_POOL.REJECT_STRATEGY));
        LogManager.info_("线程池参数 -> 核心线程数：{} 最大线程数：{} 任务队列大小：{}" +
                        " 线程最大闲置时间：{} 工作线程模板名：{} 拒绝策略：{}", coreThread,maxThread,queueCapacity,
                threadMaxFreeTime,threadName,strategy);
        // 数据检查
        if(coreThread == 0) LogManager.warn_("核心线程数为0，这将导致线程池只存在普通线程");
        if(threadMaxFreeTime < Constant.RECOMMEND_FREE_TIME)
            LogManager.warn_("线程最大闲置时间过短，推荐时长为{}秒",Constant.RECOMMEND_FREE_TIME);
        if(maxThread < 0) LogManager.warn_("最大线程数不应为0！");
        if(maxThread - coreThread <= 0) LogManager.warn_("最大线程数不应小于核心线程数！");
    }

    // 初始化工作线程、任务队列等容器
    private void initContainer(){
        LogManager.info_("【线程池模块】正在初始化相关容器...");
        try{
            threadFactory = new WorkingThreadFactory(threadName);
            coreThreadPool = new ArrayList<>(coreThread);
            commonThreadPool = new LinkedList<>();
            commonThreadChart = new MonitorChart(false);
            allThreadChart = new MonitorChart(false);
            recycledThreadChart = new MonitorChart(true);
            queueTaskChart = new MonitorChart(false);
            threadTaskQueue = new PriorityQueue<>(queueCapacity);
        }catch (Exception e){
            LogManager.error_("线程池创建容器时出现异常",e);
        }
    }

    // 初始化线程使用的相关工厂
    public void initFactory(){
        LogManager.info_("【线程池模块】正在初始化线程池相关工厂...");
        ThreadTaskFactory.getInstance();
        ConnectHandlerFactory.getInstance();
    }

    // 获取单例（防止高并发导致资源访问问题进行双判空保护）
    public static ThreadPoolManager getInstance() {
        if(instance == null)
            synchronized (ThreadPoolManager.class){
                if(instance == null)
                    instance = new ThreadPoolManager();
            }
        return instance;
    }

    // 接收与处理线程任务
    public void putThreadTask(ThreadTask task){
        // 如果核心线程池线程未满则创建核心线程
        if(coreThreadPool.size() < coreThread){
            WorkingThread thread = threadFactory.productCoreThread();
            allThreadChart.inputData(coreThreadPool.size() + commonThreadPool.size());
            thread.setWorkingTask(task);
            coreThreadPool.add(thread);
            return;
        }
        // 如果核心线程池存在空闲线程则交予其处理
        if(coreThreadPool.size() == coreThread){
            boolean find = false;
            for(WorkingThread thread : coreThreadPool)
                if(thread.isFreeThread()){
                    find = true;
                    thread.setWorkingTask(task);
                    break;
                }
            if(find) return;
        }
        // 如果线程任务队列未满则插入到队列中
        if(threadTaskQueue.size() < queueCapacity){
            synchronized (queueLock){
                threadTaskQueue.add(task);
                queueTaskChart.inputData(threadTaskQueue.size()-1);
            }
            return;
        }
        // 如果普通线程池线程未满则创建普通线程
        int commonThread = maxThread - coreThread;
        if(commonThreadPool.size() < commonThread){
            WorkingThread thread = threadFactory.productCommonThread();
            allThreadChart.inputData(coreThreadPool.size() + commonThreadPool.size());
            commonThreadChart.inputData(commonThreadPool.size());
            thread.setWorkingTask(task);
            synchronized (commonPoolLock){
                commonThreadPool.add(thread);
            }
            return;
        }
        // 如果普通线程池存在空闲线程则交予其处理
        if(commonThreadPool.size() == commonThread){
            boolean find = false;
            synchronized (commonPoolLock){
                for(WorkingThread thread : commonThreadPool)
                    if(thread.isFreeThread()){
                        find = true;
                        thread.setWorkingTask(task);
                        break;
                    }
            }
            if(find) return;
        }
        // 若以上情况都不满足则执行拒绝策略
        if(strategy == RejectStrategy.THROW_TASK){
            // 抛弃该任务
            task.doDestroy();
            return;
        }
        if(strategy == RejectStrategy.THROW_EXCEPTION){
            // 抛出异常
            LogManager.error_("线程任务：{} 已被抛弃！",task.getLogDescribe());
            task.doDestroy();
            return;
        }
        if(strategy == RejectStrategy.THROW_QUEUE_TASK){
            // 抛弃队列中最早的任务并插入该任务
            ThreadTask oldTask = threadTaskQueue.poll();
            oldTask.doDestroy();
            threadTaskQueue.add(task);
            return;
        }
    }

    // 获取队列任务
    public ThreadTask getQueueTask(){
        synchronized (queueLock){
            queueTaskChart.inputData(threadTaskQueue.size()-1);
            return threadTaskQueue.poll();
        }
    }

    // 当前任务队列是否为空
    public boolean queueEmpty(){
        return threadTaskQueue.isEmpty();
    }

    // 普通线程池中回收工作线程对象
    public void delCommonPoolThread(WorkingThread thread){
        synchronized (commonPoolLock){
            commonThreadPool.remove(thread);
            recycledThreadChart.inputData(1);
            allThreadChart.inputData(coreThreadPool.size() + commonThreadPool.size());
            commonThreadChart.inputData(commonThreadPool.size());
        }
    }

    // 返回线程基础信息
    public List<Map<String,Object>> returnThreadInfos(){
        List<Map<String,Object>> ret = new ArrayList<>();
        for(WorkingThread thread : coreThreadPool)
            ret.add(thread.returnThreadInfo());
        for(WorkingThread thread : commonThreadPool)
            ret.add(thread.returnThreadInfo());
        return ret;
    }

    public int getThreadMaxFreeTime(){
        return threadMaxFreeTime;
    }

    public int getMaxThread() {
        return maxThread;
    }

    public int getCoreThread() {
        return coreThread;
    }

    public int getQueueCapacity() {
        return queueCapacity;
    }

    public String getThreadName() {
        return threadName;
    }

    public RejectStrategy getStrategy() {
        return strategy;
    }

    public MonitorChart getCommonThreadChart() {
        return commonThreadChart;
    }

    public MonitorChart getAllThreadChart() {
        return allThreadChart;
    }

    public MonitorChart getRecycledThreadChart() {
        return recycledThreadChart;
    }

    public MonitorChart getQueueTaskChart() {
        return queueTaskChart;
    }
}
