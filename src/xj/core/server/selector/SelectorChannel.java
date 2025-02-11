package xj.core.server.selector;

import xj.abstracts.connect.ConnectHandler;
import xj.abstracts.web.Request;
import xj.abstracts.web.Response;
import xj.component.log.LogManager;
import xj.core.extern.mvc.MVCManager;
import xj.core.threadPool.ThreadPoolManager;
import xj.core.threadPool.factory.ConnectHandlerFactory;
import xj.core.threadPool.factory.ThreadTaskFactory;
import xj.enums.web.CharacterEncoding;
import xj.enums.web.ContentType;
import xj.enums.web.StatuCode;
import xj.implement.server.ByteReceiver;
import xj.implement.web.HTTPResponse;
import xj.implement.web.ProtocolRequest;
import xj.interfaces.thread.StreamIOTask;
import xj.interfaces.thread.ThreadTask;
import xj.tool.StrPool;

import java.io.IOException;
import java.nio.channels.SocketChannel;

// 被选择器管理的TCP通道处理封装对象
public class SelectorChannel {

    // 成员属性
    private SocketChannel client;// 客户端连接socket

    private ConnectHandler handler;// 消息处理器

    private Response response;// 响应体

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
            handleIO();
        }else if(phase == SelectorPhase.WAIT_RESP){
            handleWrite();
        }else if(phase == SelectorPhase.ENDING){
            endSocket();
        }
    }

    // 阶段1.准备读取数据
    private void prepareRead(){
        // 开始Socket的读IO
        startIOTask(ThreadTaskFactory.getInstance().createChannelReadTask(client,receiver)
                ,SelectorPhase.WAIT_READ);
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
        if(!receiver.dataExist())return;
        // 获取数据
        byte[] data = receiver.getData();
        if(data.length == 0){
            // 如果数据为空，则报错，并返回500响应
            response = MVCManager.getHttpRespByStatuCode(StatuCode.INTERNAL_SERVER_ERROR);
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
        if(!receiver.dataExist())return;
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

    // 内部枚举：执行阶段
    private enum SelectorPhase {
        PREPARE,WAIT_READ,WAIT_WRITE,WAIT_RESP,ENDING
    }
}
