package xj.implement.connect;

import xj.abstracts.connect.ConnectHandler;
import xj.abstracts.web.Request;
import xj.abstracts.web.Response;
import xj.component.conf.ConfigureManager;
import xj.component.log.LogManager;
import xj.core.extern.MVCManager;
import xj.enums.web.CharacterEncoding;
import xj.enums.web.StatuCode;
import xj.implement.web.HTTPRequest;
import xj.implement.web.HTTPResponse;
import xj.tool.ConfigPool;
import xj.tool.StrPool;

import java.io.UnsupportedEncodingException;

// HTTP协议连接处理器
public class HTTPConnectHandler extends ConnectHandler {

    // 成员属性
    private HTTPResponse httpResponse;// HTTP协议响应对象

    // 成员行为
    @Override
    public boolean isMatchedRequest(Request request) {
        String[] firstLineArgs = request.encodeToString()[0].split(StrPool.SPACE );
        return firstLineArgs.length == 3 && firstLineArgs[2].contains(StrPool.HTTP);
    }

    @Override
    public void handle(Request request) {
        // 清除之前的HTTP响应
        httpResponse = null;
        // 将协议请求转化为HTTP请求
        HTTPRequest httpRequest = new HTTPRequest(request);
        // 转交给MVC模块进行处理
        httpResponse = MVCManager.getInstance().handle(httpRequest);
    }

    @Override
    public Response returnResponse() {
        // 返回统一的响应头
        if(httpResponse != null) {
            httpResponse.setHeaders(StrPool.SERVER,
                    (String) ConfigureManager.getInstance().getConfig(ConfigPool.SERVER.NAME));
        }
        // 将从MVC模块得到的响应返回
        return httpResponse;
    }
}
