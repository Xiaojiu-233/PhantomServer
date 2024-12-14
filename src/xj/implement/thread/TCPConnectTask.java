package xj.implement.thread;

import xj.component.log.LogManager;
import xj.interfaces.connect.ConnectHandler;
import xj.interfaces.thread.ThreadTask;

import java.io.*;
import java.net.Socket;

// 处理TCP连接的线程任务
public class TCPConnectTask implements ThreadTask {

    // 成员属性
    private Socket client;// 客户端连接socket

    private ConnectHandler handler;// 消息处理器

    // 成员方法
    // 初始化
    public TCPConnectTask(Socket socket){
        // 获取客户端连接socket
        client = socket;
    }

    @Override
    public void doTask() {
        // 获取当前线程名
        String threadName = Thread.currentThread().getName();
        String inputLine;
        // 确定输出输入流，开始处理socket
        try(BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()))){
            while(true){
                // 存在消息时读取消息并打包成数据包
                // 如果消息处理器为空则使用处理器工厂创建消息对应的消息处理器
                // 将数据包消息传递给处理器进行处理
                // 处理器处理完成后返回消息并打包成响应数据包发送给客户端
            }
        } catch (IOException e) {
            LogManager.error("[{}] 的TCP连接任务接收socket消息时出现异常：{}",threadName,e);
        }
    }

    @Override
    public void doDestroy() {
        try {
            // 返回失败消息

            // 断开连接
            client.close();
        } catch (IOException e) {
            LogManager.error("TCP连接任务在运行socket时出现问题: {}",e);
        }
    }

    @Override
    public String getLogDescribe() {
        return "TCP连接处理任务";
    }
}
