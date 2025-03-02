package xj.implement.thread;

import xj.component.log.LogManager;
import xj.core.server.selector.SelectorChannel;
import xj.interfaces.thread.ThreadTask;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

// 使用选择器进行TCP通道管理的线程任务
public class TCPSelectorTask implements ThreadTask {

    // 成员属性
    private Selector selector;// ServerChannel处理器

    private Map<SocketChannel, SelectorChannel> channelMapping;// SocketChannel与SelectorChannel的映射容器

    // 成员方法
    // 构造方法
    public TCPSelectorTask() {
        // 初始化容器
        channelMapping = new HashMap<SocketChannel, SelectorChannel>();
        // 创建选择器
        synchronized (TCPSelectorTask.class) {
            try {
                selector = Selector.open();
            } catch (IOException e) {
                LogManager.error_("TCP选择器线程任务在创建选择器时出现异常",e);
            }
        }
    }

    // 注册读事件
    public void registerReadEvent(SocketChannel channel) {
        synchronized (TCPSelectorTask.class) {
            try {
                channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                channelMapping.put(channel, new SelectorChannel(channel));
            } catch (ClosedChannelException e) {
                LogManager.error_("TCP选择器线程任务在注册事件时出现异常",e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void doTask() {
        // 数据准备
        SocketChannel channel;
        // 循环执行，处理TCP请求
        while(true) {
            try {
                Iterator<SelectionKey> keys = null;
                synchronized (TCPSelectorTask.class) {
                    // 是否读取到事件
                    if(selector.selectNow() == 0)
                        continue;
                    // 获取处理器的事件Key
                    keys = selector.selectedKeys().iterator();
                }
                // 遍历事件Key
                while (keys.hasNext()) {
                    // 读取Key
                    SelectionKey key = keys.next();
                    // 处理Key
                    if(key.isReadable() || key.isWritable()) {
                        // 获取channel并进行分阶段工作，如果该channel还在IO阶段则跳入下一channel
                        channel = (SocketChannel) key.channel();
                        channelMapping.get(channel).phaseExecute(key.isReadable());
                    }
                    // 删除Key
                    keys.remove();
                }
            } catch (Exception e) {
                LogManager.error_("TCP选择器线程任务在执行时出现异常",e);
            }
        }
    }

    @Override
    public void doDestroy() {
        try {
            selector.close();
        } catch (IOException e) {
            LogManager.error_("TCP选择器线程任务在关闭选择器时出现异常",e);
        }
    }

    @Override
    public String getLogDescribe() {
        return "TCP选择器处理线程任务";
    }
}
