package xj.core.server.selector;

import sun.rmi.runtime.Log;
import xj.abstracts.connect.ConnectHandler;
import xj.abstracts.web.Request;
import xj.abstracts.web.Response;
import xj.component.conf.ConfigureManager;
import xj.component.log.LogManager;
import xj.core.threadPool.ThreadPoolManager;
import xj.core.threadPool.factory.ConnectHandlerFactory;
import xj.core.threadPool.factory.ThreadTaskFactory;
import xj.implement.server.ByteReceiver;
import xj.implement.web.ProtocolRequest;
import xj.interfaces.thread.StreamIOTask;
import xj.interfaces.thread.ThreadTask;
import xj.tool.ConfigPool;
import xj.tool.Constant;
import xj.tool.StrPool;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.*;

// 被选择器管理的TCP通道处理封装对象
public class SelectorChannel {

    // 成员属性
    private SocketChannel client;// 客户端连接socket

    private ConnectHandler handler;// 消息处理器

    private Response response;// 响应体

    private ByteReceiver receiver;// 数据接收器

    private SelectorPhase phase;// 执行阶段

    private Queue<SelectorRequestUnit> requestQueue;// 请求单元队列

    private boolean isReading;// 该通道是否在执行线程任务读取

    private static int socketMaxWaitTime;// 最大请求等待时间

    private long socketWaitTime;// 请求等待时间

    private ByteBuffer signBytes;// 读信号

    private static String lineBreak;// 换行符

    private static String unitSplitBreak;// 单元分隔符

    // 成员方法
    // 初始化
    public SelectorChannel(SocketChannel client) {
        // 初始化属性
        this.client = client;
        this.receiver = new ByteReceiver();
        this.requestQueue = new LinkedList<>();
        this.phase = SelectorPhase.PREPARE;
        signBytes = ByteBuffer.allocate(1);
        socketWaitTime = System.currentTimeMillis();
    }

    // 静态代码块加载
    static {
        socketMaxWaitTime = (int) ConfigureManager.getInstance().getConfig(ConfigPool.SERVER.SOCKET_MAX_WAIT_TIME);
        lineBreak = (String) ConfigureManager.getInstance().getConfig(ConfigPool.SYSTEM_ARG.LINE_BREAK);
        unitSplitBreak = (String) ConfigureManager.getInstance().getConfig(ConfigPool.SERVER.UNIT_SPLIT_BREAK);
    }

    // 分阶段执行
    public void phaseExecute(boolean readable){
        if(phase == SelectorPhase.PREPARE){
            prepareRead(readable);
        }else if(phase == SelectorPhase.WAIT_READ){
            handleData();
        }else if(phase == SelectorPhase.WAIT_WRITE){
            handleIO();
        }else if(phase == SelectorPhase.WAIT_RESP){
            handleWrite();
        }else if(phase == SelectorPhase.ENDING){
            endSocket();
        }
    }

    // 阶段1.准备读取数据
    private void prepareRead(boolean readable){
        try {
            // 判定请求单元队列是否存在队列
            if(requestQueue.isEmpty()){
                // 判定是否有数据可读
                if(client.read(signBytes) > 0){
                    // 开始Socket的读IO
                    isReading = true;
                    signBytes.flip();
                    byte[] sign = new byte[1];
                    signBytes.get(sign);
                    receiver.storeSignData(sign);
                    signBytes.clear();
                    socketWaitTime = System.currentTimeMillis();
                    startIOTask(ThreadTaskFactory.getInstance().createChannelReadTask(client,receiver)
                            ,SelectorPhase.WAIT_READ);
                }
                else{
                    // 非长连接或者连接等待请求时间超出阈值时，结束连接，否则继续等待并重新计时
                    long nowTime = System.currentTimeMillis();
                    boolean isEnd = (handler != null && handler.needEndConnection())
                            || nowTime > socketWaitTime + socketMaxWaitTime;
                    // 如果是超时的情况，则说明情况
                    if(nowTime > socketWaitTime + socketMaxWaitTime)
                        LogManager.info_("由于连接时长超时，来自 {} 的连接通道关闭",client.getRemoteAddress());
                    if(isEnd)
                        phase = SelectorPhase.ENDING;
                }
            }else{
                phase = SelectorPhase.WAIT_READ;
            }
        } catch (IOException e) {
            LogManager.error_("TCP通道对象在准备读取数据时出现异常",e);
        }
    }

    // 阶段2.处理数据
    private void handleData(){
        // 判断是否执行可读任务
        if(isReading){
            // 如果已经执行完读IO则继续执行
            if(receiver.dataNotExist())return;
            // 获取数据
            byte[] data = receiver.getData();
            isReading = false;
            // 如果没有数据则直接退回到准备阶段
            if(data.length == 0){
                phase = SelectorPhase.PREPARE;
                return;
            }
            // 存在数据时则进行解析处理
            List<Integer> splitPos = splitByteArrayByLineBreak(data);
            SelectorRequestUnit unit = new SelectorRequestUnit();
            String headMessage = null;
            int bytePointer = 0;
            byte[] splitBreakBytes = unitSplitBreak.getBytes();
            int splitBreakLen = unitSplitBreak.length();
            for(int i = 0;i < splitPos.size();i+=2){
                // 判定该行是否为单元分割符
                if(!isMatchedUnitSplitBreak(data,splitBreakBytes,splitPos.get(i),splitPos.get(i+1))){
                    // 不满足则装填数据
                    // 确认头信息
                    if(headMessage == null){
                        headMessage = new String(data, splitPos.get(i)
                                , splitPos.get(i+1) - splitPos.get(i) + 1);
                    }
                }else{
                    // 满足则将数据打包
                    unit.setData(Arrays.copyOfRange(data,bytePointer,splitPos.get(i) - 2));
                    bytePointer = splitPos.get(i) + splitBreakLen + lineBreak.length();
                    unit.setHeadMessage(headMessage);
                    headMessage = null;
                    requestQueue.add(unit);
                    unit = new SelectorRequestUnit();
                }
            }
            // 如果最后没有完成单元打包，则直接打包
            if(bytePointer == 0){
                unit.setData(data);
                unit.setHeadMessage(headMessage);
                requestQueue.add(unit);
            }
        }
        SelectorRequestUnit unit = null;
        // 判定请求单元队列是否存在队列
        if(!requestQueue.isEmpty())
            unit = requestQueue.poll();
        // 如果没有请求单元则直接退回到准备阶段
        if(unit == null){
            phase = SelectorPhase.PREPARE;
            return;
        }
        // 数据不为空则处理数据
        Request request = new ProtocolRequest(unit.getData(), unit.getHeadMessage());
        // 如果消息处理器为空则使用处理器工厂创建消息对应的消息处理器
        if(handler == null){
            handler = ConnectHandlerFactory.getInstance().getMatchConnectHandler(request);
            // 没有找到对应消息处理器则放弃该次任务
            if(handler == null) {
                LogManager.error_("TCP通道对象因未寻找到对应处理器，已被放弃");
                phase = SelectorPhase.ENDING;
                return;
            }
        }
        // 将数据包消息传递给处理器进行处理，处理器处理完成后返回消息并打包成响应数据包发送给客户端
        response = handler.handle(request);
        // 如果响应体有IO任务，为任务添加接收器
        StreamIOTask task = response.getStreamIOTask();
        if(task != null)
            task.setReceiver(receiver);
        // 开始数据流的读写IO
        startIOTask(task != null ? (ThreadTask) task :
                        ThreadTaskFactory.getInstance().createChannelWriteTask(client,response,receiver)
                ,task != null ? SelectorPhase.WAIT_WRITE : SelectorPhase.WAIT_RESP);
    }

    // 阶段3.处理服务器内部IO
    private void handleIO(){
        // 如果已经执行完读IO则继续执行
        if(receiver.dataNotExist())return;
        // 获取数据
        byte[] data = receiver.getData();
        if(data.length == 0){
            // 如果数据为空，则报错
            LogManager.error_("TCP通道对象的服务器内部数据流IO发生异常");
            response = handler.whenException();
        }else if(data.length > StrPool.SUCCESS.length() != StrPool.SUCCESS.equals(new String(data))){
            // 如果数据不为成功符号，则储存至响应体
            response.storeData(data);
        }
        // 开始Socket的写IO
        startIOTask(ThreadTaskFactory.getInstance().createChannelWriteTask(client,response,receiver)
                ,SelectorPhase.WAIT_RESP);
    }

    // 阶段4.返回数据与后续处理
    private void handleWrite(){
        // 如果已经执行完读IO则继续执行
        if(receiver.dataNotExist())return;
        // 判断是否可以结束连接，如果不行则进入准备阶段
        if(handler.needEndConnection())
            phase = SelectorPhase.ENDING;
        else
            phase = SelectorPhase.PREPARE;
    }

    // 阶段5.结束
    private void endSocket(){
        // 关闭连接，回收资源
        try {
            client.close();
        } catch (IOException e) {
            LogManager.error_("TCP通道对象结束socket连接时出现异常：{}",e);
        }
    }

    // 开启IO线程任务
    private void startIOTask(ThreadTask task,SelectorPhase phase){
        // 接收器重置
        receiver.resetData();
        // 将任务存于线程池开始运作
        ThreadPoolManager.getInstance().putThreadTask(task);
        // 进入下一个阶段
        this.phase = phase;
    }

    // 根据换行符分割字符数组(返回值中，奇数为数据段开头，偶数为数据段结尾)
    private List<Integer> splitByteArrayByLineBreak(byte[] data){
        byte[] lineBreakBytes = lineBreak.getBytes();
        int lineBreakLen = lineBreakBytes.length;
        List<Integer> splitPos = new ArrayList<>();
        int start = 0;
        int correctScan = -1;// 扫描换行符计数器
        for(int i = 0;i < data.length;i++){
            if(correctScan > -1){
                if(data[i] != lineBreakBytes[correctScan+1]){
                    correctScan = -1;
                }else{
                    correctScan++;
                    if(correctScan == lineBreakLen - 1){
                        int end = i - lineBreakLen;
                        if(start < end){
                            splitPos.add(start);
                            splitPos.add(end);
                        }
                        start = i + 1;
                        correctScan = -1;
                    }
                }
            }else if(data[i] == lineBreakBytes[0]){
                correctScan = 0;
            }
        }
        return splitPos;
    }

    // 判定字节数组是否为单元分隔符
    private boolean isMatchedUnitSplitBreak(byte[] data,byte[] unitBreakdata,int start,int end){
        if((end - start + 1) != unitBreakdata.length)
            return false;
        for(int i = 0;i < unitBreakdata.length;i++)
            if(unitBreakdata[i] != data[i + start])
                return false;
        return true;
    }

    // 内部枚举：执行阶段
    private enum SelectorPhase {
        PREPARE,WAIT_READ,WAIT_WRITE,WAIT_RESP,ENDING
    }
}
