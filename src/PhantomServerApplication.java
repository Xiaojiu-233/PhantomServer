import xj.component.conf.ConfigureManager;
import xj.component.log.LogManager;
import xj.core.server.ServerManager;

//用于启动整个系统的应用类，项目的基础
public class PhantomServerApplication {

    //初始化系统与开启服务器
    public static void main(String[] args) {
        try{
            //1.初始化核心组件
            LogManager.getInstance();
            LogManager.info("-----服务器进入准备阶段！-----");
            ConfigureManager.getInstance();
            ServerManager.getInstance();
            //2.拓展jar包的读取与调用初始化方法
            LogManager.info("-----服务器进入拓展程序调用阶段！-----");
            //3.IOC容器反射读取拓展程序
            LogManager.info("-----服务器进入IOC容器注入阶段！-----");
            //4.服务器开机
            LogManager.info("-----服务器进入启动阶段！-----");
            LogManager.getInstance().initLogManager();
            LogManager.info("-----服务器完成启动！开始运行-----");
            ServerManager.getInstance().openServer();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
