package xj.core.threadPool.factory;

import xj.component.log.LogManager;
import xj.implement.connect.ConnectHandler;
import xj.implement.connect.HTTPConnectHandler;
import xj.implement.connect.TCPLongConnectHandler;
import xj.implement.connect.TCPShortConnectHandler;
import xj.interfaces.connect.Request;

import java.util.ArrayList;
import java.util.List;

// 连接处理器的工厂，用单例工厂模式便捷生产连接处理器对象
public class ConnectHandlerFactory {

    // 成员属性
    private static ConnectHandlerFactory instance;// 单例模式实现

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
        LogManager.info("连接处理器工厂正在构建...");
        // 初始化连接处理器列表并导入默认的连接处理器
        handlerList = new ArrayList<ConnectHandler>();
        handlerList.add(new HTTPConnectHandler());
        handlerList.add(new TCPLongConnectHandler());
        handlerList.add(new TCPShortConnectHandler());
        // 通过IOC容器导入拓展连接处理器对象

        LogManager.info("连接处理器工厂构建完毕...");
    }

    // 根据提供的请求，返回合适的连接处理器对象
    public ConnectHandler getMatchConnectHandler(Request request){
        // 遍历列表，寻找合适的处理器
        for(ConnectHandler handler : handlerList){
            // 判定是否匹配，匹配成功则复制并返回指定对象
            if(handler.isMatchedRequest(request)){
                return handler.clone();
            }
        }
        // 如果没有合适的处理器对象，将抛出错误并返回null
        LogManager.error("没有找到合适的处理器对象",request);
        return null;
    }

}
