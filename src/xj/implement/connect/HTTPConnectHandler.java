package xj.implement.connect;

import xj.abstracts.connect.ConnectHandler;
import xj.abstracts.web.Request;
import xj.abstracts.web.Response;
import xj.component.monitor.MonitorManager;
import xj.core.extern.mvc.MVCManager;
import xj.enums.web.StatuCode;
import xj.implement.web.HTTPRequest;
import xj.implement.web.HTTPResponse;
import xj.tool.StrPool;

import java.util.Objects;

// HTTP协议连接处理器
public class HTTPConnectHandler extends ConnectHandler {

    // 成员行为
    @Override
    public boolean isMatchedRequest(String headMessage) {
        String[] firstLineArgs = headMessage.split(StrPool.SPACE );
        return firstLineArgs.length == 3 && firstLineArgs[2].contains(StrPool.HTTP);
    }

    @Override
    public Response handle(Request request) {
        // 将协议请求转化为HTTP请求
        HTTPRequest httpRequest = new HTTPRequest(request);
        // 可视化界面模块进行处理
        if(MonitorManager.getInstance() != null) {
            HTTPResponse response = MonitorManager.getInstance().handle(httpRequest);
            if(response != null)
                return response;
        }
        // 转交给MVC模块进行处理
        return MVCManager.getInstance().handle(httpRequest);
    }

    @Override
    public Response whenException() {
        return MVCManager.getHttpRespByStatuCode(StatuCode.INTERNAL_SERVER_ERROR);
    }

    @Override
    public boolean chooseEndStrategy() {
        return false;
    }
}
