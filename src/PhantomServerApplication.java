import xj.component.conf.ConfigureManager;
import xj.component.log.LogManager;
import xj.core.extern.IOCManager;
import xj.core.extern.JarManager;
import xj.core.server.ServerManager;
import xj.core.threadPool.ThreadPoolManager;
import xj.core.threadPool.factory.ConnectHandlerFactory;

// 用于启动整个系统的应用类，项目的基础
public class PhantomServerApplication {

    // 初始化系统与开启服务器
    public static void main(String[] args) {
        try{
            // 1.初始化核心组件
            LogManager.getInstance();
            LogManager.info_("-----服务器进入准备阶段！-----");
            ConfigureManager.getInstance();
            ServerManager.getInstance();
            ThreadPoolManager.getInstance();
            // 2.拓展jar包的读取
            LogManager.info_("-----服务器进入拓展程序读取阶段！-----");
            JarManager.getInstance();
            // 3.IOC容器反射读取拓展程序
            LogManager.info_("-----服务器进入IOC容器注入阶段！-----");
            IOCManager.getInstance();
            // 4.服务器开机
            LogManager.info_("-----服务器进入启动阶段！-----");
            LogManager.getInstance().initLogManager();
            ConnectHandlerFactory.getInstance().importHandlerByIOC();
            LogManager.info_("-----服务器完成启动！开始运行-----");
            ServerManager.getInstance().openServer();
        }catch (Exception e){
            if(LogManager.getInstance() != null){
                LogManager.error_("出现未知异常",e);
            }
            e.printStackTrace();
        }
    }
}
