package xj.implement.thread;

import xj.abstracts.connect.ConnectHandler;
import xj.abstracts.web.Request;
import xj.abstracts.web.Response;
import xj.component.log.LogManager;
import xj.implement.server.ByteReceiver;
import xj.abstracts.thread.ThreadTask;
import xj.tool.StrPool;

import java.io.IOException;
import java.nio.channels.SocketChannel;

// 处理SocketChannel的写IO线程任务
public class ChannelWriteTask extends ThreadTask {

    // 成员属性
    private final SocketChannel out;// 输出流

    private ByteReceiver receiver;// 数据发送目标

    private Request request;// 请求

    private ConnectHandler handler;// 连接处理器

    // 成员方法
    // 初始化
    public ChannelWriteTask(SocketChannel out, Request request, ConnectHandler handler, ByteReceiver receiver) {
        // 存入数据
        this.out = out;
        this.receiver = receiver;
        this.request = request;
        this.handler = handler;
    }

    @Override
    public void doTask() {
        // 获取当前线程名
        String threadName = Thread.currentThread().getName();
        // 执行数据写入
        try {
            // 将数据包消息传递给处理器进行处理，处理器处理完成后返回消息并打包成响应数据包发送给客户端
            Response response = handler.doHandle(request);
            // 响应填入信息
            response.writeMessage(out);
        } catch (Exception e) {
            LogManager.error_("[{}] 的TCP通道写IO任务在输出数据时出现异常：{}",threadName,e);
            receiver.storeData(new byte[0]);
            try {
                out.close();
            } catch (IOException ex) {
                LogManager.error_("[{}] 的TCP通道写IO任务在因异常关闭时出现异常：{}",threadName,e);
            }
            return;
        }
        // 将成功消息传输给接收器
        receiver.storeData(StrPool.SUCCESS.getBytes());
    }

    @Override
    public void doDestroy() {

    }

    @Override
    public String getLogDescribe() {
        return "TCP通道IO处理任务";
    }
}
