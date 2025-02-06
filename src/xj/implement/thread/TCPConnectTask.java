package xj.implement.thread;

import xj.component.conf.ConfigureManager;
import xj.component.log.LogManager;
import xj.core.threadPool.ThreadPoolManager;
import xj.core.threadPool.factory.ConnectHandlerFactory;
import xj.abstracts.connect.ConnectHandler;
import xj.abstracts.web.Request;
import xj.abstracts.web.Response;
import xj.core.threadPool.factory.ThreadTaskFactory;
import xj.implement.web.ProtocolRequest;
import xj.interfaces.thread.ThreadTask;
import xj.tool.ConfigPool;
import xj.tool.FileIOUtil;

import java.io.*;
import java.net.Socket;
import java.nio.channels.SocketChannel;

// 处理TCP连接的线程任务
public class TCPConnectTask implements ThreadTask {

    // 成员属性
    private SocketChannel client;// 客户端连接socket

    private ConnectHandler handler;// 消息处理器

    private Long maxWaitTime;// 未响应超时时间（毫秒）

    private StreamIOTask ioTask;// 数据传输线程任务

    // 成员方法
    // 初始化
    public TCPConnectTask(SocketChannel socket){
        // 获取客户端连接socket
        client = socket;
        // 开启数据传输线程任务
        ioTask = (StreamIOTask) ThreadTaskFactory.getInstance()
                .createStreamIOTask(client,true);
        ThreadPoolManager.getInstance().putThreadTask(ioTask);
        // 获取参数
        int time = (Integer)ConfigureManager.getInstance().getConfig(ConfigPool.THREAD_POOL.MAX_WAIT_TIME) ;
        maxWaitTime = (long) time;
    }

    @Override
    public void doTask() {
        // 获取当前线程名
        String threadName = Thread.currentThread().getName();
        String inputLine;
        // 确定输出输入流，开始处理socket
        try{
            // 等待计时器设置
            long maxWaitTimer = System.currentTimeMillis();
            // 开始循环
            while(true){
                // 如果连接等待时间超过最大等待时间，则放弃该任务
                if(handler == null && System.currentTimeMillis() - maxWaitTimer > maxWaitTime){
                    LogManager.info_("[{}] 的TCP连接任务因未接受数据且超时等待已被回收",threadName);
                    break;
                }
                // 存在消息时读取消息，不存在时跳过
                byte[] data = ioTask.getData();
                if(data == null || data.length == 0) continue;// 将消息打包成数据包
                Request request = new ProtocolRequest(data);
                // 如果消息处理器为空则使用处理器工厂创建消息对应的消息处理器
                if(handler == null){
                    handler = ConnectHandlerFactory.getInstance().getMatchConnectHandler(request);
                    // 没有找到对应消息处理器则放弃该次任务
                    if(handler == null){
                        LogManager.error_("[{}] 的TCP连接任务因未寻找到对应处理器，已被放弃",threadName);
                        break;
                    }
                }
                // 将数据包消息传递给处理器进行处理
                handler.handle(request);
                // 处理器处理完成后返回消息并打包成响应数据包发送给客户端
                Response response = handler.returnResponse();
                response.writeMessage(client);
                // 判断是否可以结束连接
                if(handler.needEndConnection()) break;
                // 刷新等待时间
                maxWaitTimer = System.nanoTime();
            }
        } catch (IOException e) {
            LogManager.error_("[{}] 的TCP连接任务接收socket消息时出现异常：{}",threadName,e);
        } catch (Exception e) {
            LogManager.error_("[{}] 的TCP连接任务在执行时出现异常：{}",threadName,e);
        }
        // 连接结束
        try {
            client.close();
        } catch (IOException e) {
            LogManager.error_("[{}] 的TCP连接任务结束socket连接时出现异常：{}",threadName,e);
        }
    }

    @Override
    public void doDestroy() {
        try {
            // 断开连接
            client.close();
        } catch (IOException e) {
            LogManager.error_("TCP连接任务在运行socket时出现问题: {}",e);
        }
    }

    @Override
    public String getLogDescribe() {
        return "TCP连接处理任务";
    }

}
