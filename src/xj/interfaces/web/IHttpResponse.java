package xj.interfaces.web;

import xj.entity.web.Cookie;
/**
 * HTTP响应提供的可以用于拓展程序使用的接口
 * */
public interface IHttpResponse {

    /**
     * 设置响应头
     * @param key 参数名
     * @param value 参数值
     * */
    public void setRespHeaders(String key, String value);

    /**
     * 获取响应参数
     * @param key 参数名
     * */
    public String getHeaderArg(String key);

    /**
     * 设置Cookie
     * */
    public void setCookie(Cookie cookie);
}
