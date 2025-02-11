package xj.implement.thread;

import xj.component.log.LogManager;
import xj.implement.server.ByteReceiver;
import xj.interfaces.server.IReceiver;
import xj.interfaces.thread.StreamIOTask;
import xj.interfaces.thread.ThreadTask;
import xj.tool.Constant;
import xj.tool.StrPool;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;

// 处理数据流的输出IO线程任务，将InputStream转为文件
public class StreamOutputTask implements ThreadTask, StreamIOTask {

    // 成员属性
    private final InputStream source;// 输入流

    private final String targetPath;// 目标文件路径

    private ByteReceiver receiver;// 数据发送目标

    // 成员方法
    // 初始化
    public StreamOutputTask(InputStream out,String target) {
        // 存入数据
        this.source = out;
        this.targetPath = target;
    }

    @Override
    public void setReceiver(IReceiver receiver) {
        this.receiver = (ByteReceiver) receiver;
    }

    @Override
    public void doTask() {
        // 获取当前线程名
        String threadName = Thread.currentThread().getName();
        // 打开文件
        File file = new File(targetPath);
        if (!file.exists())
            if(!file.mkdirs()){
                LogManager.error_("[{}] 的数据流输出IO线程任务无法创建文件: {}",threadName,targetPath);
                receiver.storeData(new byte[0]);
                return;
            }
        // 数据准备
        byte[] buffer = new byte[Constant.BYTES_UNIT_CAPACITY];
        // 开始读取数据
        try (OutputStream target = Files.newOutputStream(file.toPath())) {
            // 1.读取数据并存入文件中
            int getByte = 0;
            while ((getByte = source.read(buffer))!= -1) {
                // 数据
                target.write(buffer,0,getByte);
            }
            // 3.将成功信息传输给接收器
            receiver.storeData(StrPool.SUCCESS.getBytes());
            // 4.读取结束
            source.close();
        } catch (IOException e) {
            LogManager.error_("[{}] 的数据流输出IO线程任务在读取数据时出现异常：{}",threadName,e);
            receiver.storeData(new byte[0]);
        }
    }

    @Override
    public void doDestroy() {
        // 获取当前线程名
        String threadName = Thread.currentThread().getName();
        try {
            source.close();
        } catch (IOException e) {
            LogManager.error_("[{}] 的数据流输出IO线程任务在关闭数据流时出现异常：{}",threadName,e);
        }
    }

    @Override
    public String getLogDescribe() {
        return "数据流输出IO线程任务";
    }
}
