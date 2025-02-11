package xj.implement.thread;

import jdk.internal.util.xml.impl.Input;
import xj.component.log.LogManager;
import xj.implement.server.ByteReceiver;
import xj.interfaces.server.IReceiver;
import xj.interfaces.thread.StreamIOTask;
import xj.interfaces.thread.ThreadTask;
import xj.tool.Constant;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

// 处理数据流的输入IO线程任务，将InputStream转为字节数组
public class StreamInputTask implements ThreadTask, StreamIOTask {

    // 成员属性
    private final InputStream in;// 输入流

    private ByteReceiver receiver;// 数据发送目标

    // 成员方法
    // 初始化
    public StreamInputTask(InputStream in) {
        // 存入数据
        this.in = in;
    }

    @Override
    public void setReceiver(IReceiver receiver) {
        this.receiver = (ByteReceiver) receiver;
    }

    @Override
    public void doTask() {
        // 获取当前线程名
        String threadName = Thread.currentThread().getName();
        // 数据准备
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[Constant.BYTES_UNIT_CAPACITY];
        // 开始读取数据
        try {
            // 1.读取数据并存入容器中
            int getByte = 0;
            while ((getByte = in.read(buffer))!= -1) {
                // 读出数据
                byteStream.write(buffer,0,getByte);
            }
            // 2.将容器数据传输给接收器
            receiver.storeData(byteStream.toByteArray());
            // 3.读取结束
            byteStream.reset();
            in.close();
        } catch (IOException e) {
            LogManager.error_("[{}] 的数据流输入IO线程任务在读取数据时出现异常：{}",threadName,e);
            receiver.storeData(new byte[0]);
        }
    }

    @Override
    public void doDestroy() {
        // 获取当前线程名
        String threadName = Thread.currentThread().getName();
        try {
            in.close();
        } catch (IOException e) {
            LogManager.error_("[{}] 的数据流输入IO线程任务在关闭数据流时出现异常：{}",threadName,e);
        }
    }

    @Override
    public String getLogDescribe() {
        return "数据流输入IO线程任务";
    }
}
