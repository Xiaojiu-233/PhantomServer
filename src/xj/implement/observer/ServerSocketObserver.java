package xj.implement.observer;

import xj.core.threadPool.ThreadPoolManager;
import xj.core.threadPool.factory.ThreadTaskFactory;
import xj.interfaces.observer.SocketChannelObserver;

import java.net.Socket;
import java.nio.channels.SocketChannel;

// 服务器socket连接观察者
public class ServerSocketObserver implements SocketChannelObserver<SocketChannel> {

    @Override
    public void whenAccept(SocketChannel event) {
        // 将socket打包成线程任务移交给线程池
        ThreadPoolManager.getInstance().putThreadTask(
                ThreadTaskFactory.getInstance().createTCPConnectTask(event));
    }
}
