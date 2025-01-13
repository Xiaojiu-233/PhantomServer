package xj.implement.thread;

import xj.component.log.LogManager;
import xj.core.threadPool.factory.ConnectHandlerFactory;
import xj.abstracts.connect.ConnectHandler;
import xj.abstracts.web.Request;
import xj.abstracts.web.Response;
import xj.interfaces.thread.ThreadTask;
import xj.tool.ProtocolUtils;

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
        try(InputStream in = client.getInputStream();
            OutputStream out = client.getOutputStream()){
            while(true){
                // 存在消息时读取消息并打包成数据包
                Request request = ProtocolUtils.getProtocolRequest(in);
                if(request.isEmptyData()) continue;
                // 如果消息处理器为空则使用处理器工厂创建消息对应的消息处理器
                if(handler == null)
                    handler = ConnectHandlerFactory.getInstance().getMatchConnectHandler(request);
                // 将数据包消息传递给处理器进行处理
                handler.handle(request);
                // 处理器处理完成后返回消息并打包成响应数据包发送给客户端
                Response response = handler.returnResponse();
                response.writeMessage(out);
                // 判断是否可以结束连接
                if(handler.needEndConnection()) break;
            }
        } catch (IOException e) {
            LogManager.error_("[{}] 的TCP连接任务接收socket消息时出现异常：{}",threadName,e);
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
            // 返回失败消息

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
