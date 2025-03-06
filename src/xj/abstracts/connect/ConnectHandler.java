package xj.abstracts.connect;

import xj.abstracts.web.Request;
import xj.abstracts.web.Response;
import xj.component.log.LogManager;

import java.lang.reflect.Constructor;

// 连接处理器，为连接用动态线程池处理TCP连接的解决方案接口
// 实现该接口的类都能作为服务器处理TCP连接的解决方案程序
public abstract class ConnectHandler {

    // Request的内容是否满足处理条件
    abstract public boolean isMatchedRequest(String headMessage);

    // 处理相关内容，返回对应的Response
    abstract public Response handle(Request request);

    // 出现异常时触发，进行处理
    abstract public Response whenException();

    // 断开连接的时机确认(默认情况为直接关闭)
    public boolean needEndConnection() {
        return true;
    }

    // 是否选择单元分隔符的结束连接策略(默认情况为选择)
    public boolean chooseEndStrategy() {
        return true;
    }

    // 复制自身对象
    public ConnectHandler cloneSelf(){
        try {
            Constructor<? extends ConnectHandler> con =  getClass().getDeclaredConstructor();
            con.setAccessible(true);
            return con.newInstance();
        } catch (Exception e){
            LogManager.error_("连接处理器复制时出现异常",e);
            return null;
        }
    }
}
