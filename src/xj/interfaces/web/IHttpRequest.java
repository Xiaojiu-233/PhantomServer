package xj.interfaces.web;

import xj.enums.web.RequestMethod;

import java.util.Map;

/**
 * HTTP请求提供的可以用于拓展程序使用的接口
 */
public interface IHttpRequest {

    /**
     * 获取请求头
     */
    public Map<String, String> getHeaders();

    /**
     * 获取请求路径
     */
    public String getUrl();

    /**
     * 获取请求方法
     */
    public RequestMethod getMethod();
}
