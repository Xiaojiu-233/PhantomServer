import xj.component.conf.ConfigureManager;
import xj.component.log.LogManager;

//用于启动整个系统的应用类，项目的基础
public class PhantomServerApplication {

    //初始化系统与开启服务器
    public static void main(String[] args) {
        try{
            //1.初始化核心组件
            LogManager.getInstance();
            ConfigureManager.getInstance();
            //2.拓展jar包的读取与调用初始化方法

            //3.IOC容器反射读取拓展程序

            //4.服务器开机
            LogManager.getInstance().initLogManager();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
