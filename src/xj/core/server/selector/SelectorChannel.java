package xj.core.server.selector;

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
import xj.abstracts.thread.ThreadTask;
import xj.tool.ConfigPool;
import xj.tool.Constant;
import xj.tool.FileIOUtil;
import xj.tool.StrPool;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
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

    private String id;// channel的编号

    private String fromIp;// 来源IP

    private long connectStartTime,connectStablishTime;// 连接开始时间 连接建立时间

    private List<Long> connectingTimes;// 连接持续时间列表

    private boolean isClosed;// 是否完成channel连接任务

    private static int channelCounter;// channel计数器

    // 成员方法
    // 初始化
    public SelectorChannel(SocketChannel client) {
        // 初始化属性
        long nowTime = System.currentTimeMillis();
        this.client = client;
        this.receiver = new ByteReceiver();
        this.requestQueue = new LinkedList<>();
        this.phase = SelectorPhase.PREPARE;
        signBytes = ByteBuffer.allocate(1);
        socketWaitTime = nowTime;
        connectStablishTime = nowTime;
        channelCounter = (channelCounter + 1) % Constant.SELECTOR_CHANNEL_ID_MAXMIZE;
        id = String.valueOf(nowTime / Constant.SELECTOR_CHANNEL_ID_MAXMIZE + channelCounter);
        fromIp = client.socket().getRemoteSocketAddress().toString();
        connectingTimes = new ArrayList<>();
        connectStartTime = -1;
    }

    // 静态代码块加载
    static {
        socketMaxWaitTime = (int) ConfigureManager.getInstance().getConfig(ConfigPool.SERVER.SOCKET_MAX_WAIT_TIME);
        lineBreak = (String) ConfigureManager.getInstance().getConfig(ConfigPool.SYSTEM_ARG.LINE_BREAK);
        unitSplitBreak = (String) ConfigureManager.getInstance().getConfig(ConfigPool.SERVER.UNIT_SPLIT_BREAK);
    }

    // 分阶段执行
    public void phaseExecute(){
        if(phase == SelectorPhase.PREPARE){
            prepareRead();
        }else if(phase == SelectorPhase.WAIT_READ){
            handleData();
        }else if(phase == SelectorPhase.WAIT_WRITE){
            handleIO();
        }else if(phase == SelectorPhase.ENDING){
            endSocket();
        }
    }

    // 阶段1.准备读取数据
    private void prepareRead(){
        try {
            // 判定请求单元队列是否存在队列
            if(requestQueue.isEmpty()){
                // 判定是否有数据可读
                if(client.read(signBytes) > 0){
                    // 开始Socket的读IO
                    connectStartTime = System.currentTimeMillis();
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
            List<Integer> splitPos = FileIOUtil.splitByteArrayByLineBreak(data,false);
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
                    unit.setData(Arrays.copyOfRange(data,bytePointer,splitPos.get(i) - lineBreak.length()));
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
        Request request = new ProtocolRequest(unit.getData(), unit.getHeadMessage(),
                client.socket().getInetAddress().getHostAddress());
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
        // 开始数据流的读写IO
        startIOTask(ThreadTaskFactory.getInstance().createChannelWriteTask(client,receiver,request,handler),
                 SelectorPhase.WAIT_WRITE);
    }

    // 阶段3.处理服务器内部IO
    private void handleIO(){
        // 如果已经执行完读IO则继续执行
        if(receiver.dataNotExist())return;
        // 获取数据
        byte[] data = receiver.getData();
        if(data.length == 0){
            // 如果数据为空，则报错
            LogManager.error_("TCP通道对象的服务器内部IO发生异常");
        }
        // 执行到此确定为一个流程的连接处理结束，进行连接持续时间结算
        long nowTime = System.currentTimeMillis();
        connectingTimes.add(nowTime - connectStartTime);
        connectStartTime = 0;
        // 判断是否可以结束连接，如果不行则进入准备阶段
        if(handler.needEndConnection())
            phase = SelectorPhase.ENDING;
        else
            phase = SelectorPhase.PREPARE;
    }

    // 阶段4.结束
    private void endSocket(){
        // 关闭连接，回收资源
        try {
            client.close();
            isClosed = true;
        } catch (IOException e) {
            LogManager.error_("TCP通道对象结束socket连接时出现异常：{}",e);
        }
    }

    // 开启IO线程任务
    private void startIOTask(ThreadTask task,SelectorPhase phase){
        // 接收器重置
        receiver.resetData();
        // 存储channel的Id
        task.setChannelId(id);
        // 将任务存于线程池开始运作
        ThreadPoolManager.getInstance().putThreadTask(task);
        // 进入下一个阶段
        this.phase = phase;
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

    // 返回channel基础信息
    public Map<String,Object> returnChannelInfo(){
        Map<String, Object> ret = new HashMap<>();
        long nowConnectTime = connectStartTime > 0 ? System.currentTimeMillis() - connectStartTime : 0;
        long avgConnectTime = connectingTimes.stream().mapToInt(Long::intValue).sum() + nowConnectTime
                / (connectingTimes.size() + 1);
        ret.put(StrPool.CHANNEL_ID,id);
        ret.put("来源IP",fromIp);
        ret.put("当前连接持续时间(毫秒)",nowConnectTime);
        ret.put("平均连接持续时间(毫秒)",avgConnectTime);
        ret.put("使用的处理器",handler == null ? StrPool.NONE : handler.getClass().getSimpleName());
        ret.put("连接建立时间", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(new Date(connectStablishTime)));
        ret.put(StrPool.CHANNEL_STATU, SelectorPhase.PREPARE.equals(phase) ? StrPool.WEB_PREPARE :
                !isClosed ? StrPool.WEB_RUNNING :
                connectStartTime == 0 ? StrPool.WEB_SUCC_END : StrPool.WEB_FAIL_END);
        return ret;
    }

    // 内部枚举：执行阶段
    private enum SelectorPhase {
        PREPARE,WAIT_READ,WAIT_WRITE,ENDING
    }
}
