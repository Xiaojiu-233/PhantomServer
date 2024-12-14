package xj.implement.thread;

import xj.component.log.LogManager;
import xj.interfaces.thread.ThreadTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

// 处理TCP连接的线程任务
public class TCPConnectTask implements ThreadTask {

    // 成员属性
    private Socket client;// 客户端连接socket

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
        // 根据第一个连接传入的数据确定获得的连接处理器
        try(BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))){
            while (true) {
                // 读取到信息后
                if((inputLine = in.readLine()) != null){

                }
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
