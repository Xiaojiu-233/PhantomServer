package xj.core.server.selector;

import xj.abstracts.connect.ConnectHandler;
import xj.abstracts.web.Request;
import xj.abstracts.web.Response;
import xj.component.log.LogManager;
import xj.core.threadPool.ThreadPoolManager;
import xj.core.threadPool.factory.ConnectHandlerFactory;
import xj.core.threadPool.factory.ThreadTaskFactory;
import xj.implement.web.ProtocolRequest;

import java.io.IOException;
import java.nio.channels.SocketChannel;

// 被选择器管理的TCP通道处理封装对象
public class SelectorChannel {

    // 成员属性
    private SocketChannel client;// 客户端连接socket

    private ConnectHandler handler;// 消息处理器

    private ByteReceiver receiver;// 数据接收器

    private SelectorPhase phase;// 执行阶段

    // 成员方法
    // 初始化
    public SelectorChannel(SocketChannel client) {
        // 初始化属性
        this.client = client;
        this.receiver = new ByteReceiver();
        this.phase = SelectorPhase.PREPARE;
    }

    // 分阶段执行
    public void phaseExecute(){
        if(phase == SelectorPhase.PREPARE){
            prepareRead();
        }else if(phase == SelectorPhase.WAIT_READ){
            handleData();
        }else if(phase == SelectorPhase.WAIT_WRITE){
            handleWriteAndEnding();
        }else if(phase == SelectorPhase.ENDING){
            endingSocket();
        }
    }

    // 阶段1.准备读取数据
    private void prepareRead(){
        // 接收器重置
        receiver.resetData();
        // 开启IO读线程
        ThreadPoolManager.getInstance().putThreadTask(
                ThreadTaskFactory.getInstance().createChannelReadTask(client,receiver));
        // 进入下一个阶段
        phase = SelectorPhase.WAIT_READ;
    }

    // 阶段2.处理数据
    private void handleData(){
        // 如果已经执行完读IO则继续执行
        if(!receiver.dataExist())return;
        // 如果数据为空，则直接进入结束阶段
        byte[] data = receiver.getData();
        if(data.length == 0){
            phase = SelectorPhase.ENDING;
            return;
        }
        // 数据不为空则处理数据
        Request request = new ProtocolRequest(data);
        // 如果消息处理器为空则使用处理器工厂创建消息对应的消息处理器
        if(handler == null){
            handler = ConnectHandlerFactory.getInstance().getMatchConnectHandler(request);
            // 没有找到对应消息处理器则放弃该次任务
            if(handler == null){
                LogManager.error_("TCP通道对象因未寻找到对应处理器，已被放弃");
                return;
            }
        }
        // 将数据包消息传递给处理器进行处理
        handler.handle(request);
        // 处理器处理完成后返回消息并打包成响应数据包发送给客户端
        Response response = handler.returnResponse();
        // 接收器重置
        receiver.resetData();
        // 开启IO写线程
        ThreadPoolManager.getInstance().putThreadTask(
                ThreadTaskFactory.getInstance().createChannelWriteTask(client,response,receiver));
        // 进入下一个阶段
        phase = SelectorPhase.WAIT_WRITE;
    }

    // 阶段3.返回数据与后续处理
    private void handleWriteAndEnding(){
        // 如果已经执行完读IO则继续执行
        if(!receiver.dataExist())return;
        // 判断是否可以结束连接，如果不行则进入准备阶段
        if(handler.needEndConnection())
            phase = SelectorPhase.ENDING;
        else
            phase = SelectorPhase.PREPARE;
    }

    // 阶段4.结束
    private void endingSocket(){
        // 关闭连接，回收资源
        try {
            client.close();
        } catch (IOException e) {
            LogManager.error_("TCP通道对象结束socket连接时出现异常：{}",e);
        }
    }

    // 内部枚举：执行阶段
    private enum SelectorPhase {
        PREPARE,WAIT_READ,WAIT_WRITE,ENDING
    }
}
