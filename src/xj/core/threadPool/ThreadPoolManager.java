package xj.core.threadPool;

import xj.component.conf.ConfigureManager;
import xj.component.log.LogManager;
import xj.enums.thread.RefuseStrategy;
import xj.interfaces.thread.ThreadTask;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

// 动态线程池管理器，用于管理服务器内部的工作线程
public class ThreadPoolManager {

    // 成员属性
    private static ThreadPoolManager instance;// 单例模式实现

    private int maxThread = 0;// 最大线程数
    private int coreThread = 0;// 最大线程数
    private int queueCapacity = 0;// 任务队列大小
    private String threadName = "";// 工作线程名称
    private RefuseStrategy strategy = RefuseStrategy.THROW_TASK;// 拒绝策略，默认为抛弃任务

    private List<WorkingThread> coreThreadPool;// 核心线程池
    private List<WorkingThread> commonThreadPool;// 普通线程池
    private Queue<ThreadTask> threadTaskQueue;// 线程任务队列

    private int threadCounter = 0;// 线程计数器，用于线程命名

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
        maxThread = (int) ConfigureManager.getInstance().getConfig("thread-pool.max-thread");
        coreThread = (int) ConfigureManager.getInstance().getConfig("thread-pool.core-thread");
        queueCapacity = (int) ConfigureManager.getInstance().getConfig("thread-pool.queue-capacity");
        threadName = (String) ConfigureManager.getInstance().getConfig("thread-pool.thread-name");
        strategy = RefuseStrategy.getStrategyByString((String)ConfigureManager.getInstance()
                .getConfig("thread-pool.refuse-strategy"));
        // 数据检查
        if(coreThread == 0) LogManager.warn("核心线程数为0，这将导致线程池只存在普通线程");
        if(maxThread < 0) LogManager.warn("最大线程数不应为0！");
        if(maxThread - coreThread <= 0) LogManager.warn("最大线程数不应小于核心线程数！");
    }

    // 初始化工作线程、任务队列等容器
    private void initContainer(){
        LogManager.info("【线程池模块】正在初始化相关容器...");
        try{
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

    }
}
