package xj.implement.thread;

import xj.component.log.LogManager;
import xj.core.server.selector.ByteReceiver;
import xj.interfaces.thread.ThreadTask;
import xj.tool.Constant;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

// 处理SocketChannel的读IO线程任务
public class ChannelReadTask implements ThreadTask {

    // 成员属性
    private final SocketChannel in;// 输入流

    private ByteReceiver receiver;// 数据发送目标

    // 成员方法
    // 初始化
    public ChannelReadTask(SocketChannel in, ByteReceiver receiver) {
        // 存入数据
        this.in = in;
        this.receiver = receiver;
    }

    @Override
    public void doTask() {
        // 获取当前线程名
        String threadName = Thread.currentThread().getName();
        // 数据准备
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ByteBuffer buffer = ByteBuffer.allocate(Constant.BYTES_UNIT_CAPACITY);
        int bytesRead = 0;
        int zeroCounter = 0;// 零数据计时器
        // 开始读取数据
        try {
            // 1.读取数据并存入容器中
            while ((bytesRead = in.read(buffer)) >= 0) {
                // 判定数据读取，超过一定次数没有读取到数据则直接跳过
                if(bytesRead == 0)
                    if(zeroCounter < Constant.BYTES_UNIT_CAPACITY){
                        zeroCounter++;
                        continue;
                    }else
                        break;
                // 由写模式变为读模式
                buffer.flip();
                // 读出数据
                while (buffer.hasRemaining()){
                    byteStream.write(buffer.get());
                }
                buffer.clear();
            }
            // 2.将容器数据传输给接收器
            receiver.storeData(byteStream.toByteArray());
            // 3.读取结束
            byteStream.reset();
        } catch (IOException e) {
            LogManager.error_("[{}] 的TCP通道读IO任务在读取数据时出现异常：{}",threadName,e);
        }
    }

    @Override
    public void doDestroy() {

    }

    @Override
    public String getLogDescribe() {
        return "TCP通道读IO任务";
    }
}
