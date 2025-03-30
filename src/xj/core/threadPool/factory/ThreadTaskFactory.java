package xj.core.threadPool.factory;

import xj.abstracts.connect.ConnectHandler;
import xj.abstracts.web.Request;
import xj.component.log.LogManager;
import xj.implement.server.ByteReceiver;
import xj.implement.thread.ChannelReadTask;
import xj.implement.thread.ChannelWriteTask;
import xj.abstracts.thread.ThreadTask;

import java.nio.channels.SocketChannel;

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
        LogManager.info_("线程任务工厂正在构建...");
    }

    // 创建UDP连接任务

    // 创建TCP通道读IO任务
    public ThreadTask createChannelReadTask(SocketChannel in, ByteReceiver receiver){
        return new ChannelReadTask(in,receiver);
    }

    // 创建TCP通道写IO任务
    public ThreadTask createChannelWriteTask
    (SocketChannel out, ByteReceiver receiver, Request request, ConnectHandler handler){
        return new ChannelWriteTask(out,request,handler,receiver);
    }
}
