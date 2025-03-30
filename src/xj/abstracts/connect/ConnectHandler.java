package xj.abstracts.connect;

import xj.abstracts.web.Request;
import xj.abstracts.web.Response;
import xj.component.log.LogManager;
import xj.core.extern.IOCManager;
import xj.interfaces.web.WebFilter;

import java.lang.reflect.Constructor;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * 连接处理器，为连接用动态线程池处理TCP连接的解决方案接口
 * 实现该接口的类都能作为服务器处理TCP连接的解决方案程序
 * */
public abstract class ConnectHandler {

    // 成员属性
    /**
     * web请求过滤器列表
     * */
    private PriorityQueue<WebFilter> webFilters;

    // 成员方法
    /**
     * 构造方法
     * */
    public ConnectHandler() {
        // 初始化容器
        webFilters = new PriorityQueue<>(Comparator.comparingInt(WebFilter::getPriority));
    }

    /**
     * 过滤器列表装填对应过滤器
     * @param childClass 子类对象
     * */
    public final void initFilter(Class<? extends ConnectHandler> childClass) {
        try {
            // 进行指定过滤器装填
            List<Object> list = IOCManager.getInstance().returnImplInstancesByClass(WebFilter.class);
            for (Object o : list)
                if(o instanceof WebFilter) {
                    WebFilter filter = (WebFilter) o;
                    if(filter.getBelongConnectHandler().equals(childClass))
                        webFilters.add(filter);
                }
        } catch (Exception e) {
            LogManager.error_("连接处理器装填过滤器时出现异常", e);
        }

    }

    /**
     * Request的内容是否满足处理条件
     * @param headMessage 头信息
     * */
    abstract public boolean isMatchedRequest(String headMessage);

    /**
     * 处理相关内容，返回对应的Response
     * @param request 请求
     * */
    abstract protected Response handle(Request request);

    /**
     * 出现异常时触发，进行处理
     * */
    abstract public Response whenException();

    /**
     * 断开连接的时机确认(默认情况为直接关闭)
     * */
    public boolean needEndConnection() {
        return true;
    }

    /**
     * 是否选择单元分隔符的结束连接策略(默认情况为选择)
     * */
    public boolean chooseEndStrategy() {
        return true;
    }

    /**
     * 复制自身对象
     * */
    public ConnectHandler cloneSelf() {
        try {
            Constructor<? extends ConnectHandler> con = getClass().getDeclaredConstructor();
            con.setAccessible(true);
            return con.newInstance();
        } catch (Exception e) {
            LogManager.error_("连接处理器复制时出现异常", e);
            return null;
        }
    }

    /**
     * 带上过滤器的执行处理
     * @param request 请求
     * */
    public Response doHandle(Request request) {
        // 进行过滤器过滤
        if (!webFilters.isEmpty())
            for (WebFilter webFilter : webFilters)
                if (!webFilter.doFilter(request))
                    return whenException();
        // 如果所有过滤器通过过滤，执行
        return handle(request);
    }
}