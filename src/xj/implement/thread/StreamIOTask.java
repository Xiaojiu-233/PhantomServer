package xj.implement.thread;

import xj.abstracts.connect.ConnectHandler;
import xj.component.log.LogManager;
import xj.interfaces.thread.ThreadTask;
import xj.tool.Constant;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

// 处理数据流IO的线程任务
public class StreamIOTask implements ThreadTask {

    // 成员属性
    private final SocketChannel in;// 输入流

    private final boolean once;// 是否为一次性传输

    private boolean readFinish;// 是否读取完成数据

    private ByteArrayOutputStream byteStream;// 字节输出流，用于存储数据

    private final Object lock = new Object();// 锁

    // 成员方法
    // 初始化
    public StreamIOTask(SocketChannel in, boolean once) {
        // 存入数据
        this.in = in;
        this.once = once;
    }

    // 读取数据，如果输出字节流没有数据变动则可以获取
    public byte[] getData(){
        synchronized (lock) {
            // 不存在或者空数据返回null
            if(byteStream == null || byteStream.size() == 0)
                return null;
            // 存在时查看数据是否有变动
            if(readFinish){
                //LogManager.debug_("StreamIOTask读完: bytesGet={} , byteStream.size={}",bytesGet, byteStream.size());
                byte[] ret = byteStream.toByteArray();
                byteStream.reset();
                readFinish = false;
                return ret;
            }
            //LogManager.debug_("StreamIOTask: dataRemain={}, bytesGet={}, byteStream.size={}",dataRemain,bytesGet, byteStream.size());
            return null;
        }
    }

    @Override
    public void doTask() {
        // 获取当前线程名
        String threadName = Thread.currentThread().getName();
        // 数据准备
        byteStream = new ByteArrayOutputStream();
        ByteBuffer buffer = ByteBuffer.allocate(Constant.BYTES_UNIT_CAPACITY);
        int bytesRead = 0;
        int zeroCounter = 0;// 零数据计时器
        // 开始读取数据
        try {
            // 如果非一次性则停止第二次数据读取，否则一直读取
            do {
                // 1.读取数据并存入容器中
                while ((bytesRead = in.read(buffer)) >= 0)
                    synchronized (lock) {
                        // 判定数据读取
                        if(bytesRead == 0)
                            if(zeroCounter < Constant.BYTES_UNIT_CAPACITY){
                                zeroCounter++;
                                continue;
                            }else
                                break;
                        buffer.flip();  // 由写模式变为读模式
                        // 读出数据
                        while (buffer.hasRemaining()){
                            byteStream.write(buffer.get());
                        }
                        buffer.clear();
                    }
                // 2.等待容器数据被读取
                synchronized (lock) {
                    readFinish = true;
                }
                while(true)
                    synchronized (lock) {
                        if (byteStream.size() == 0)
                            break;
                    }
                // 3.读取结束
                byteStream.reset();
            }while (!once);
        } catch (IOException  e) {
            LogManager.error_("[{}] 的数据流IO任务在读取数据时出现异常：{}",threadName,e);
        }
    }

    @Override
    public void doDestroy() {

    }

    @Override
    public String getLogDescribe() {
        return "数据流IO处理任务";
    }
}
