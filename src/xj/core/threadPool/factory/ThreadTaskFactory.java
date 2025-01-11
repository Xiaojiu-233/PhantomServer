package xj.core.threadPool.factory;

import xj.component.log.LogManager;
import xj.implement.thread.TCPConnectTask;
import xj.interfaces.thread.ThreadTask;

import java.net.Socket;

// 线程任务的工厂，用单例工厂模式便捷生产线程任务对象
public class ThreadTaskFactory {

    // 成员属性
    private static volatile ThreadTaskFactory instance;// 单例模式实现

    // 成员方法
    // 获取单例（防止高并发导致资源访问问题进行双判空保护）
    public static ThreadTaskFactory getInstance() {
        if(instance == null)
            synchronized (ThreadTaskFactory.class){
                if(instance == null)
                    instance = new ThreadTaskFactory();
            }
        return instance;
    }

    // 初始化
    public ThreadTaskFactory(){
        LogManager.info("线程任务工厂正在构建...");
    }

    // 创建TCP连接任务
    public ThreadTask createTCPConnectTask(Socket socket){
        return new TCPConnectTask(socket);
    }

    // 创建UDP连接任务

    // 创建文件IO任务
}
