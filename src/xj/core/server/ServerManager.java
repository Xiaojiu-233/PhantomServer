package xj.core.server;

import xj.component.conf.ConfigureManager;
import xj.component.log.LogManager;
import xj.implement.observer.SocketSelectorObserver;
import xj.implement.observer.SocketChannelObserverContainer;
import xj.implement.observer.SocketConfigObserver;
import xj.interfaces.observer.SocketChannelObserver;
import xj.tool.ConfigPool;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

// 服务器模块，用于管理ServerSocket监听端口请求
public class ServerManager {

    // 成员属性
    private static volatile ServerManager instance; // 单例模式实现

    private final int port;// 监听端口

    private SocketChannelObserverContainer observerContainer;// 观察者容器

    // 成员方法
    // 初始化
    public ServerManager(){
        LogManager.info_("【服务器模块】开始初始化");
        // 获取配置
        port = (int) ConfigureManager.getInstance().getConfig(ConfigPool.SERVER.PORT);
        LogManager.info_("服务器使用的端口为",port);
        // 设置观察者容器用于监听
        observerContainer = new SocketChannelObserverContainer<>();
        observerContainer.addObserver(new SocketConfigObserver());
        observerContainer.addObserver(new SocketSelectorObserver());
        LogManager.info_("服务器观察者容器设置完毕");
        LogManager.info_("【服务器模块】初始化完成");
    }

    // 获取单例模式
    public static ServerManager getInstance(){
        if(instance == null)
            synchronized (ServerManager.class){
                if(instance == null)
                    instance = new ServerManager();
            }
        return instance;
    }

    // 开机
    public void openServer(){
        LogManager.info_("【服务器】正在开机....");
        // 开启ServerSocket
        try(ServerSocketChannel serverChannel = ServerSocketChannel.open()){
            serverChannel.configureBlocking(false);
            serverChannel.bind(new InetSocketAddress(port));
            while(true){
                SocketChannel clientChannel = serverChannel.accept();
                if(clientChannel != null){
                    // 监听到客户端连接的socket之后，交给观察者们处理
                    LogManager.info_("接收到客户端的连接请求 -> 互联网地址：{} 本地地址：{} 端口：{}"
                            ,clientChannel.getRemoteAddress(),clientChannel.getLocalAddress()
                            ,clientChannel.socket().getPort());
                    observerContainer.notifyObservers(clientChannel);
                }
            }
        } catch (IOException e) {
            LogManager.error_("服务器运行时出现异常", e);
        }
    }
}
