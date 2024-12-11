package xj.core.threadPool;

import xj.component.conf.ConfigureManager;
import xj.component.log.LogManager;
import xj.enums.thread.RejectStrategy;
import xj.interfaces.thread.ThreadTask;
import xj.tool.ConfigPool;

import java.util.*;

// 动态线程池管理器，用于管理服务器内部的工作线程
public class ThreadPoolManager {

    // 成员属性
    private static ThreadPoolManager instance;// 单例模式实现

    private int maxThread = 0;// 最大线程数
    private int coreThread = 0;// 最大线程数
    private int queueCapacity = 0;// 任务队列大小
    private int longConnectMaxThread;// 最大长连接线程数
    private String threadName = "";// 工作线程名称
    private RejectStrategy strategy = RejectStrategy.THROW_TASK;// 拒绝策略，默认为抛弃任务

    private WorkingThreadFactory threadFactory;// 工作线程工厂
    private List<WorkingThread> coreThreadPool;// 核心线程池
    private List<WorkingThread> commonThreadPool;// 普通线程池
    private Queue<ThreadTask> threadTaskQueue;// 线程任务队列
    private final Object lock = new Object();// 资源锁


    // 成员行为
    // 初始化
    public ThreadPoolManager() {
        LogManager.info("【线程池模块】开始初始化");
        initConfig();
        initContainer();
        LogManager.info("【线程池模块】初始化完毕");
    }

    // 读取配置文件
    private void initConfig(){
        LogManager.info("【线程池模块】正在读取配置文件...");
        // 数据读取
        maxThread = (int) ConfigureManager.getInstance().getConfig(ConfigPool.THREAD_POOL.MAX_THREAD);
        coreThread = (int) ConfigureManager.getInstance().getConfig(ConfigPool.THREAD_POOL.CORE_THREAD);
        queueCapacity = (int) ConfigureManager.getInstance().getConfig(ConfigPool.THREAD_POOL.QUEUE_CAPACITY);
        longConnectMaxThread = (int) ConfigureManager.getInstance().getConfig(ConfigPool.THREAD_POOL.LONG_CONNECT_MAX_THREAD);
        threadName = (String) ConfigureManager.getInstance().getConfig(ConfigPool.THREAD_POOL.THREAD_NAME);
        strategy = RejectStrategy.getStrategyByString((String)ConfigureManager.getInstance()
                .getConfig(ConfigPool.THREAD_POOL.REJECT_STRATEGY));
        LogManager.info("线程池参数 -> 核心线程数：{} 最大线程数：{} 任务队列大小：{} 最大长连接线程数：{}" +
                        " 工作线程名：{} 拒绝策略：{}",
                coreThread,maxThread,queueCapacity,threadName,strategy);
        // 数据检查
        if(coreThread == 0) LogManager.warn("核心线程数为0，这将导致线程池只存在普通线程");
        if(longConnectMaxThread > maxThread / 2) LogManager.warn("最大长连接线程数超过最大线程数的一半，推荐数量为小于等于最大线程数一半");
        if(maxThread < 0) LogManager.warn("最大线程数不应为0！");
        if(maxThread - coreThread <= 0) LogManager.warn("最大线程数不应小于核心线程数！");
    }

    // 初始化工作线程、任务队列等容器
    private void initContainer(){
        LogManager.info("【线程池模块】正在初始化相关容器...");
        try{
            threadFactory = new WorkingThreadFactory(threadName);
            coreThreadPool = new ArrayList<>(coreThread);
            commonThreadPool = new ArrayList<>(maxThread - coreThread);
            threadTaskQueue = new PriorityQueue<>(queueCapacity);
        }catch (Exception e){
            LogManager.error("线程池创建容器时出现异常",e);
        }
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
        synchronized (lock){
            // 如果核心线程池线程未满则创建核心线程
            if(coreThreadPool.size() < coreThread){
                WorkingThread thread = threadFactory.productThread();
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
                    }
                if(find) return;
            }
            // 如果线程任务队列未满则插入到队列中
            if(threadTaskQueue.size() < queueCapacity){
                threadTaskQueue.add(task);
                return;
            }
            // 如果普通线程池线程未满则创建普通线程
            int commonThread = maxThread - coreThread;
            if(commonThreadPool.size() < commonThread){
                WorkingThread thread = threadFactory.productThread();
                thread.setWorkingTask(task);
                commonThreadPool.add(thread);
                return;
            }
            // 如果普通线程池存在空闲线程则交予其处理
            if(commonThreadPool.size() == commonThread){
                boolean find = false;
                for(WorkingThread thread : commonThreadPool)
                    if(thread.isFreeThread()){
                        find = true;
                        thread.setWorkingTask(task);
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
                LogManager.error("线程任务：{} 已被抛弃！",task.getLogDescribe());
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
    }
}
