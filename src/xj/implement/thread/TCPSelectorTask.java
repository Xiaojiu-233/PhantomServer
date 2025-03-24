package xj.implement.thread;

import xj.component.conf.ConfigureManager;
import xj.component.log.LogManager;
import xj.core.server.ServerManager;
import xj.core.server.selector.SelectorChannel;
import xj.abstracts.thread.ThreadTask;
import xj.tool.ConfigPool;
import xj.tool.StrPool;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;

// 使用选择器进行TCP通道管理的线程任务
public class TCPSelectorTask extends ThreadTask {

    // 成员属性
    private Selector selector;// ServerChannel处理器

    private final Map<SocketChannel, SelectorChannel> channelMapping;// SocketChannel与SelectorChannel的映射容器

    private final Map<String,Object> channelInfos;// 存储的channel数据

    private boolean openInfoListen = false;

    // 成员方法
    // 构造方法
    public TCPSelectorTask() {
        // 初始化容器
        channelMapping = new HashMap<>();
        channelInfos = new HashMap<>();
        // 初始化数据
        openInfoListen = (Boolean)ConfigureManager.getInstance().getConfig(ConfigPool.MONITOR.ENABLE);
        // 创建选择器
        synchronized (this) {
            try {
                selector = Selector.open();
            } catch (IOException e) {
                LogManager.error_("TCP选择器线程任务在创建选择器时出现异常",e);
            }
        }
    }

    // 注册读事件
    public void registerReadEvent(SocketChannel channel) {
        synchronized (this) {
            try {
                channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                channelMapping.put(channel, new SelectorChannel(channel));
            } catch (ClosedChannelException e) {
                LogManager.error_("TCP选择器线程任务在注册事件时出现异常",e);
            }
        }
    }

    @Override
    public void doTask() {
        // 数据准备
        SocketChannel channel;
        ServerManager.getInstance().setSelectorTask(this);
        Iterator<SelectionKey> keys = null;
        // 循环执行，处理TCP请求
        while(true) {
            try {
                synchronized (this) {
                    // 是否读取到事件
                    if(selector.selectNow() == 0)
                        continue;
                    // 获取处理器的事件Key
                    keys = selector.selectedKeys().iterator();
                    // 遍历事件Key
                    while (keys.hasNext()) {
                        // 读取Key
                        SelectionKey key = keys.next();
                        // 处理Key
                        if(key.isReadable() || key.isWritable()) {
                            // 获取channel并进行分阶段工作，如果该channel还在IO阶段则跳入下一channel
                            channel = (SocketChannel) key.channel();
                            SelectorChannel selChannel = channelMapping.get(channel);
                            if(selChannel != null)
                                selChannel.phaseExecute();
                        }
                        // 删除Key
                        keys.remove();
                    }
                    // 刷新容器
                    refreshChannelMapping();
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

    // 获取channel信息列表
    public List<Object> getChannelsInfo() {
        return new ArrayList<>(channelInfos.values());
    }

    // 刷新channel容器，并设置缓存数据
    private void refreshChannelMapping() {
        List<SocketChannel> removeChannels = new ArrayList<>();
        for(Map.Entry<SocketChannel, SelectorChannel> entry : channelMapping.entrySet()) {
            // 判定channel是否可删除
            Map<String,Object> info = entry.getValue().returnChannelInfo();
            String statu = (String) info.get(StrPool.CHANNEL_STATU);
            if (StrPool.WEB_SUCC_END.equals(statu) || StrPool.WEB_FAIL_END.equals(statu))
                removeChannels.add(entry.getKey());
            // 当开启可视化界面模块时，监听channel数据
            if(openInfoListen){
                String id = (String) info.get(StrPool.CHANNEL_ID);
                channelInfos.put(id,info);
            }
        }
        for(SocketChannel channel : removeChannels) {
            channelMapping.remove(channel);
        }
    }

}
