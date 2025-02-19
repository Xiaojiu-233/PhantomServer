package xj.core.threadPool.factory;

import xj.component.log.LogManager;
import xj.abstracts.connect.ConnectHandler;
import xj.core.extern.IOCManager;
import xj.implement.connect.HTTPConnectHandler;
import xj.implement.connect.TCPChatConnectHandler;
import xj.implement.connect.TCPCloudConnectHandler;
import xj.abstracts.web.Request;

import java.util.ArrayList;
import java.util.List;

// 连接处理器的工厂，用单例工厂模式便捷生产连接处理器对象
public class ConnectHandlerFactory {

    // 成员属性
    private static volatile ConnectHandlerFactory instance;// 单例模式实现

    private List<ConnectHandler> handlerList;// 连接处理器列表

    // 成员方法
    // 获取单例（防止高并发导致资源访问问题进行双判空保护）
    public static ConnectHandlerFactory getInstance() {
        if(instance == null)
            synchronized (ConnectHandlerFactory.class){
                if(instance == null)
                    instance = new ConnectHandlerFactory();
            }
        return instance;
    }

    // 初始化
    public ConnectHandlerFactory(){
        LogManager.info_("连接处理器工厂正在构建...");
        // 初始化连接处理器列表
        handlerList = new ArrayList<ConnectHandler>();
        // 导入默认的连接处理器
        handlerList.add(new HTTPConnectHandler());
        handlerList.add(new TCPChatConnectHandler());
        handlerList.add(new TCPCloudConnectHandler());
    }

    // 通过IOC容器导入拓展连接处理器对象
    public void importHandlerByIOC(){
        LogManager.info_("连接处理器工厂开始导入拓展处理器对象");
        // 将拓展连接处理器导入到列表头部
        List<Object> handlers = IOCManager.getInstance().returnInstancesByClass(ConnectHandler.class);
        for(Object handler : handlers){
            handlerList.add(0, (ConnectHandler) handler);
        }
        LogManager.info_("连接处理器工厂导入拓展处理器对象完成");
    }

    // 根据提供的请求，返回合适的连接处理器对象
    public ConnectHandler getMatchConnectHandler(Request request){
        synchronized(ConnectHandlerFactory.class){
            // 遍历列表，寻找合适的处理器
            for(ConnectHandler handler : handlerList){
                // 判定是否匹配，匹配成功则复制并返回指定对象
                if(handler.isMatchedRequest(request)){
                    return handler.cloneSelf();
                }
            }
            // 如果没有合适的处理器对象，将抛出错误并返回null
            LogManager.error_("没有找到合适的处理器对象",request.getClass().getName());
            return null;
        }
    }

}
