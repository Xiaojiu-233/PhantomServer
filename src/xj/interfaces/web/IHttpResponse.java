package xj.interfaces.web;

import xj.enums.web.RequestMethod;
import xj.implement.web.HTTPResponse;
import xj.interfaces.thread.StreamIOTask;

import java.util.Map;

/**
 * HTTP响应提供的可以用于拓展程序使用的接口
 */
public interface IHttpResponse {

    /**
     * 设置响应头
     * @param key
     * @param value
     */
    public void setRespHeaders(String key, String value);

    /**
     * 获取响应参数
     * @param key
     */
    public String getHeaderArg(String key);
}
