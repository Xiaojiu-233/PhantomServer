package xj.implement.observer;

import xj.core.threadPool.ThreadPoolManager;
import xj.implement.thread.TCPSelectorTask;
import xj.interfaces.observer.SocketChannelObserver;

import java.nio.channels.SocketChannel;

// 服务器socket交付选择器观察者
public class SocketSelectorObserver implements SocketChannelObserver<SocketChannel> {

    // 成员属性
    private final TCPSelectorTask selectorTask;// 选择器的线程任务

    // 成员方法
    // 构造方法
    public SocketSelectorObserver() {
        // 设置选择器线程任务等参数
        selectorTask = new TCPSelectorTask();
        // 存入线程池中
        ThreadPoolManager.getInstance().putThreadTask(selectorTask);
    }

    @Override
    public void whenAccept(SocketChannel event) {
        // 将socket打包成线程任务移交给处理器
        selectorTask.registerReadEvent(event);
    }
}
