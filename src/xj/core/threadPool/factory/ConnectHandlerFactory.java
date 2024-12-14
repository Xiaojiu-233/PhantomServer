package xj.core.threadPool.factory;

import xj.component.log.LogManager;

// 连接处理器的工厂，用单例工厂模式便捷生产连接处理器对象
public class ConnectHandlerFactory {

    // 成员属性
    private static ConnectHandlerFactory instance;// 单例模式实现

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
        LogManager.info("连接处理器工厂构建完毕...");
    }
}
