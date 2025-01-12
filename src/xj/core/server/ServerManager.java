package xj.core.server;

import xj.component.conf.ConfigureManager;
import xj.component.log.LogManager;
import xj.implement.observer.ServerSocketObserver;
import xj.implement.observer.SocketObserverContainer;
import xj.tool.ConfigPool;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Observer;

// 服务器模块，用于管理ServerSocket监听端口请求
public class ServerManager {

    // 成员属性
    private static volatile ServerManager instance; // 单例模式实现

    private final int port;// 监听端口

    private SocketObserverContainer observerContainer;// 观察者容器

    // 成员方法
    // 初始化
    public ServerManager(){
        LogManager.info("【服务器模块】开始初始化");
        // 获取配置
        port = (int) ConfigureManager.getInstance().getConfig(ConfigPool.SERVER.PORT);
        LogManager.info("服务器使用的端口为",port);
        // 设置观察者容器用于监听
        observerContainer = new SocketObserverContainer<>();
        observerContainer.addObserver(new ServerSocketObserver());
        LogManager.info("服务器观察者容器设置完毕");
        LogManager.info("【服务器模块】开始初始化");
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
        LogManager.info("【服务器】正在开机....");
        // 开启ServerSocket
        try(ServerSocket serverSocket = new ServerSocket(port)){
            while(true){
                Socket clientSocket = serverSocket.accept();
                if(clientSocket != null){
                    // 监听到客户端连接的socket之后，交给观察者们处理
                    LogManager.info("接收到客户端的连接请求 -> 互联网地址：{} 本地地址：{} 端口：{}"
                            ,clientSocket.getInetAddress(),clientSocket.getLocalAddress(),clientSocket.getPort());
                    observerContainer.notifyObservers(clientSocket);
                }
            }
        } catch (IOException e) {
            LogManager.error("服务器运行时出现异常", e);
        }
    }
}
