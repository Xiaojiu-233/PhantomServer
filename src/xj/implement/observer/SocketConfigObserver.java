package xj.implement.observer;

import xj.component.conf.ConfigureManager;
import xj.component.log.LogManager;
import xj.core.threadPool.ThreadPoolManager;
import xj.core.threadPool.factory.ThreadTaskFactory;
import xj.interfaces.observer.SocketObserver;
import xj.tool.ConfigPool;

import java.net.Socket;
import java.net.SocketException;

// 服务器socket配置观察者
public class SocketConfigObserver implements SocketObserver<Socket> {

    @Override
    public void whenAccept(Socket event) {
        // 给socket进行相关配置
        try {
            event.setSoTimeout((Integer) ConfigureManager.getInstance()
                    .getConfig(ConfigPool.SERVER.SOCKET_MAX_READ_TIME));
        } catch (SocketException e) {
            LogManager.error_("在对传入的socket对象进行配置时出现异常", e);
        }
    }
}
