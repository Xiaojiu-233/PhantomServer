package xj.interfaces.web;

import xj.abstracts.connect.ConnectHandler;
import xj.abstracts.web.Request;

/**
 * 通用Web请求过滤器，用于过滤请求或者对请求做出处理
 * */
public interface WebFilter {

    /**
     * 执行过滤，返回结果，如果为false则直接停止执行
     * @param request
     * @return 是否继续流程
     * */
    boolean doFilter(Request request);

    /**
     * 返回优先级，值越小越靠前执行
     * @return 优先级
     * */
    int getPriority();

    /**
     * 返回所属的连接处理器对象
     * @return 优先级
     * */
    Class<? extends ConnectHandler> getBelongConnectHandler();
}
