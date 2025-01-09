package xj.implement.connect;

import xj.component.log.LogManager;
import xj.interfaces.connect.Request;
import xj.interfaces.connect.Response;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

// 连接处理器，为连接用动态线程池处理TCP连接的解决方案接口
// 实现该接口的类都能作为服务器处理TCP连接的解决方案程序
public abstract class ConnectHandler {

    // Request的内容是否满足处理条件
    abstract public boolean isMatchedRequest(Request request);

    // 处理相关内容
    abstract public void handle(Request request);

    // 返回对应的Response
    abstract public Response returnResponse();

    // 复制自身对象
    public ConnectHandler clone(){
        try {
            Constructor con =  getClass().getDeclaredConstructor();
            con.setAccessible(true);
            return (ConnectHandler) con.newInstance();
        } catch (Exception e){
            LogManager.error("连接处理器复制时出现异常",e);
            return null;
        }
    }
}
