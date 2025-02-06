package xj.implement.observer;

import xj.component.conf.ConfigureManager;
import xj.component.log.LogManager;
import xj.core.threadPool.ThreadPoolManager;
import xj.core.threadPool.factory.ThreadTaskFactory;
import xj.interfaces.observer.SocketChannelObserver;
import xj.tool.ConfigPool;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.nio.channels.SocketChannel;

// 服务器socket配置观察者
public class SocketConfigObserver implements SocketChannelObserver<SocketChannel> {

    @Override
    public void whenAccept(SocketChannel event) {
        // 给socket进行相关配置
        try {
            event.configureBlocking(false);// 不再阻塞
        } catch (IOException e) {
            LogManager.error_("在对传入的socket对象进行配置时出现异常", e);
        }
    }
}
