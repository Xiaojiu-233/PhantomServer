package xj.core.threadPool;

import com.sun.javafx.scene.control.skin.IntegerFieldSkin;
import xj.component.conf.ConfigureManager;
import xj.component.log.LogManager;
import xj.core.threadPool.factory.*;
import xj.entity.monitor.MonitorChart;
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
    private ThreadManageStrategy threadStrategy = ThreadManageStrategy.TIMEOUT;// 线程管理策略策略，默认为超时回收

    private WorkingThreadFactory threadFactory;// 工作线程工厂
    private List<WorkingThread> coreThreadPool;// 核心线程池
    private List<WorkingThread> commonThreadPool;// 普通线程池
    private Queue<ThreadTask> threadTaskQueue;// 线程任务队列
    private final Object queueLock = new Object();// 队列锁
    private final Object commonPoolLock = new Object();// 普通线程池锁
    private MonitorChart commonThreadChart, allThreadChart, recycledThreadChart, queueTaskChart;
    // 普通线程数图表 所有线程数图表 回收线程数图表 队列任务数图表

    private List<Long> questWaitTime;// 请求在队列的等待时间，五个为一组
    private Queue<Float> avgQuestWaitTime;// 以组为单位的请求在队列的平均等待时间，取最新的三组
    private Integer recycleThreadNum;// 因智能管理策略需要义务回收的线程数
    private final int R = 200;// 常量，满足智能线程管理阈值的时间间隔(毫秒)
    private final int GROUP_UNITS = 5;// 常量，请求在队列等待时间中一组包含数据
    private final int GROUP_NUMS = 3;// 常量，请求在队列等待时间中组的上限

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
        threadStrategy = ThreadManageStrategy.getStrategyByString((String)ConfigureManager.getInstance()
                .getConfig(ConfigPool.THREAD_POOL.THREAD_MANAGE_STRATEGY));
        LogManager.info_("线程池参数 -> 核心线程数：{} 最大线程数：{} 任务队列大小：{}" +
                        " 线程最大闲置时间：{} 工作线程模板名：{} 拒绝策略：{} 线程管理策略：{}", coreThread,maxThread,queueCapacity,
                threadMaxFreeTime,threadName,strategy,threadStrategy);
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
            questWaitTime = new ArrayList<>();
            avgQuestWaitTime = new LinkedList<>();
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
            thread.setWorkingTask(task);
            coreThreadPool.add(thread);
            allThreadChart.inputData(commonThreadPool.size() + coreThreadPool.size());
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
        // 如果当前为超时回收策略且线程任务队列未满则插入到队列中
        if(!isTimeoutThreadStrategy() || threadTaskQueue.size() < queueCapacity){
            synchronized (queueLock){
                task.setQueueWaitStartTime(System.currentTimeMillis());
                threadTaskQueue.add(task);
                queueTaskChart.inputData(threadTaskQueue.size());
            }
            return;
        }
        // 判定当前线程管理策略是否为超时回收策略，有则执行普通线程添加管理
        if(isTimeoutThreadStrategy()){
            // 如果普通线程池线程未满则创建普通线程
            int commonThread = maxThread - coreThread;
            if(commonThreadPool.size() < commonThread){
                WorkingThread thread = threadFactory.productCommonThread();
                thread.setWorkingTask(task);
                synchronized (commonPoolLock){
                    commonThreadPool.add(thread);
                    allThreadChart.inputData(commonThreadPool.size() + coreThreadPool.size());
                    commonThreadChart.inputData(commonThreadPool.size());
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
            ThreadTask task = threadTaskQueue.poll();
            if (task != null && task.getQueueWaitStartTime() > 0 && !isTimeoutThreadStrategy()) {
                waitThreadStrategyAnalysis(task.getQueueWaitStartTime());
            }
            return task;
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

    // 刷新图表数值
    public void refreshRecycledThreadChart() {
        recycledThreadChart.inputData(1);
    }

    // 获取成员属性
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

    public String getStrategy() {
        return strategy.name();
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

    public String getThreadStrategy() {
        return threadStrategy.name();
    }

    public void changeProperties(int commonThreadNum, int queueTaskNum, int threadMaxFreeTime,
                                 String manageStrategy, String refuseStrategy){
        maxThread = commonThreadNum + coreThread;
        queueCapacity = queueTaskNum;
        this.threadMaxFreeTime = threadMaxFreeTime;
        strategy = RejectStrategy.getStrategyByString(refuseStrategy);
        threadStrategy = ThreadManageStrategy.getStrategyByString(manageStrategy);
    }

    // 判定当前线程管理策略是否为超时回收
    public boolean isTimeoutThreadStrategy(){
        return ThreadManageStrategy.TIMEOUT.equals(threadStrategy);
    }

    // 等待时间算法管理下的数据分析与线程管理
    private void waitThreadStrategyAnalysis(long time){
        // 数据更新
        long waitTime = System.currentTimeMillis() - time;
        if(questWaitTime.size() < GROUP_UNITS){
            // 组内数据未满则装入
            questWaitTime.add(waitTime);
        }else{
            // 组内数据已满则计算后装入组容器
            long T = 0;
            for(long t : questWaitTime)
                T += t;
            questWaitTime.clear();
            // 如果组容器超过上限则删除旧数据
            if (avgQuestWaitTime.size() >= GROUP_NUMS)
                avgQuestWaitTime.poll();
            avgQuestWaitTime.add((float) T /GROUP_UNITS);
        }
        // 线程管理
        List<Float> avgTimeList = (List<Float>) avgQuestWaitTime;
        int r1 = (int)(avgTimeList.get(0) - avgTimeList.get(1));
        int r2 = (int)(avgTimeList.get(1) - avgTimeList.get(2));
        if(Math.abs(r1) > Math.abs(r2) && Math.abs(r2) > R){
            if(r1 > 0 && r2 > 0){
                // 如果时间差距值都为正，则添加普通线程
                synchronized (commonPoolLock){
                    commonThreadPool.add(threadFactory.productCommonThread());
                    allThreadChart.inputData(commonThreadPool.size() + coreThreadPool.size());
                    commonThreadChart.inputData(commonThreadPool.size());
                }
            }else{
                // 如果时间差距值都为负，则添加回收普通线程的需求
                synchronized (commonPoolLock){
                    recycleThreadNum++;
                }
            }
        }
    }

    // 普通线程判定当前是否有义务回收需求
    public boolean needRecycleItSelf(){
        synchronized (commonPoolLock){
            if(recycleThreadNum > 0){
                recycleThreadNum--;
                return true;
            }else
                return false;
        }
    }

    // 内部枚举类
    // 线程管理策略
    private enum ThreadManageStrategy{
        TIMEOUT,// 超时回收管理
        WAIT;// 等待时间算法管理

        public static ThreadManageStrategy getStrategyByString(String strategy){
            try{
                return ThreadManageStrategy.valueOf(strategy);
            }catch (IllegalArgumentException e){
                LogManager.error_("获取线程管理策略时出错，已使用默认方案-超时回收",e);
                return ThreadManageStrategy.TIMEOUT;
            }
        }
    }

    // 动态线程池对任务的拒绝策略
    private enum RejectStrategy {
        THROW_EXCEPTION,// 抛出异常
        THROW_TASK,// 不执行该任务
        THROW_QUEUE_TASK;// 将线程池里最早进入队列的任务

        public static RejectStrategy getStrategyByString(String strategy){
            try{
                return RejectStrategy.valueOf(strategy);
            }catch (IllegalArgumentException e){
                LogManager.error_("获取拒绝策略时出错，已使用默认方案-不执行任务",e);
                return RejectStrategy.THROW_TASK;
            }
        }

    }
}
