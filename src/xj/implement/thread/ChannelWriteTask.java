package xj.implement.thread;

import xj.abstracts.web.Response;
import xj.component.log.LogManager;
import xj.implement.server.ByteReceiver;
import xj.interfaces.thread.ThreadTask;
import xj.tool.StrPool;

import java.io.IOException;
import java.nio.channels.SocketChannel;

// 处理SocketChannel的写IO线程任务
public class ChannelWriteTask implements ThreadTask {

    // 成员属性
    private final SocketChannel out;// 输出流

    private final Response response;// Web响应对象

    private ByteReceiver receiver;// 数据发送目标

    // 成员方法
    // 初始化
    public ChannelWriteTask(SocketChannel out, Response response, ByteReceiver receiver) {
        // 存入数据
        this.out = out;
        this.response = response;
        this.receiver = receiver;
    }

    @Override
    public void doTask() {
        // 获取当前线程名
        String threadName = Thread.currentThread().getName();
        // 执行数据写入
        try {
            response.writeMessage(out);
        } catch (IOException e) {
            LogManager.error_("[{}] 的TCP通道写IO任务在输出数据时出现异常：{}",threadName,e);
            try {
                out.close();
            } catch (IOException ex) {
                LogManager.error_("[{}] 的TCP通道写IO任务在因异常关闭时出现异常：{}",threadName,e);
            }
        }
        // 将成功消息传输给接收器
        receiver.storeData(StrPool.SUCCESS.getBytes());
    }

    @Override
    public void doDestroy() {

    }

    @Override
    public String getLogDescribe() {
        return "TCP通道写IO任务";
    }
}
