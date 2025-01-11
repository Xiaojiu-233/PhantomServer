package xj.implement.connect;

import xj.abstracts.connect.ConnectHandler;
import xj.abstracts.web.Request;
import xj.abstracts.web.Response;
import xj.implement.web.HTTPRequest;
import xj.tool.StrPool;

// HTTP协议连接处理器
public class HTTPConnectHandler extends ConnectHandler {

    @Override
    public boolean isMatchedRequest(Request request) {
        String[] firstLineArgs = request.encodeToString()[0].split(StrPool.SPACE );
        return firstLineArgs.length == 3 && firstLineArgs[2].contains(StrPool.HTTP);
    }

    @Override
    public void handle(Request request) {
        // 将协议请求转化为HTTP请求
        HTTPRequest httpRequest = new HTTPRequest(request);
    }

    @Override
    public Response returnResponse() {
        return null;
    }
}
