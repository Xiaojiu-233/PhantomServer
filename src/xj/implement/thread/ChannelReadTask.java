package xj.implement.thread;

import xj.component.conf.ConfigureManager;
import xj.component.log.LogManager;
import xj.core.threadPool.factory.ConnectHandlerFactory;
import xj.implement.server.ByteReceiver;
import xj.interfaces.thread.ThreadTask;
import xj.tool.ConfigPool;
import xj.tool.Constant;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

// 处理SocketChannel的读IO线程任务
public class ChannelReadTask implements ThreadTask {

    // 成员属性
    private final SocketChannel in;// 输入流

    private final ByteReceiver receiver;// 数据发送目标

    private EndStrategy strategy;// 策略

    private final String lineBreak;// 换行符

    private final String endBreak;// 单元分隔符

    // 成员方法
    // 初始化
    public ChannelReadTask(SocketChannel in, ByteReceiver receiver) {
        // 存入数据
        this.in = in;
        this.receiver = receiver;
        // 数据初始化
        strategy = EndStrategy.NONE;
        lineBreak = (String) ConfigureManager.getInstance().getConfig(ConfigPool.SYSTEM_ARG.LINE_BREAK);
        endBreak = (String) ConfigureManager.getInstance().getConfig(ConfigPool.SERVER.UNIT_SPLIT_BREAK);
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
                // 由写模式变为读模式
                buffer.flip();
                // 读出数据
                byte[] bytes = null;
                while (buffer.hasRemaining()){
                    bytes = new byte[bytesRead];
                    buffer.get(bytes);
                    byteStream.write(bytes);
                }
                buffer.clear();
                // 如果没有确认结束策略，则从头信息中确认
                if(EndStrategy.NONE.equals(strategy))
                    chooseEndStatistic(bytes);
                // 结束策略选择与使用
                if(EndStrategy.ZERO_COUNT.equals(strategy)){
                    // 判定数据读取，超过一定次数没有读取到数据则直接跳过
                    if(bytesRead == 0)
                        if(zeroCounter < Constant.ZERO_COUNT_LIMIT){
                            zeroCounter++;
                            continue;
                        }else{
                            break;
                        }
                    zeroCounter = 0;
                }else if(EndStrategy.END_BREAK.equals(strategy)){
                    // 读到数据结尾有单元分隔符时，跳出循环
                    String[] lines = byteStream.toString().split(lineBreak);
                    String endLine = lines[lines.length - 1];
                    if(endLine.length() == endBreak.length() && endLine.equals(endBreak)){
                        break;
                    }
                }
            }
            // 2.将容器数据传输给接收器
            receiver.storeData(byteStream.toByteArray());
            // 3.读取结束
            byteStream.reset();
        } catch (Exception e) {
            LogManager.error_("[{}] 的TCP通道读IO任务在读取数据时出现异常：{}",threadName,e);
        }
    }

    // 选择结束策略
    private void chooseEndStatistic(byte[] data){
        // 判定传入数据合法性
        if(data == null || data.length == 0){
            return;
        }
        // 确认最终要解析的数据
        byte[] finData = data;
        byte[] signData = receiver.getSign();
        if(signData != null && signData.length > 0){
            finData = new byte[data.length + signData.length];
            System.arraycopy(signData, 0, finData, 0, signData.length);
            System.arraycopy(data, 0, finData, signData.length, data.length);
        }
        // 拆解数据，选择策略
        String headMessage = new String(finData).split(lineBreak)[0];
        strategy = ConnectHandlerFactory.getInstance().isChooseEndBreakStrategy(headMessage) ?
            EndStrategy.END_BREAK : EndStrategy.ZERO_COUNT;
    }

    @Override
    public void doDestroy() {

    }

    @Override
    public String getLogDescribe() {
        return "TCP通道读IO任务";
    }

    // 终止策略
    private enum EndStrategy {
        NONE, // 无策略
        ZERO_COUNT, // 零计算统计策略
        END_BREAK; // 单元分隔符策略
    }
}
