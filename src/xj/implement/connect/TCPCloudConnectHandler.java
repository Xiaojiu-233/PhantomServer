package xj.implement.connect;

import xj.abstracts.connect.ConnectHandler;
import xj.abstracts.web.Request;
import xj.abstracts.web.Response;

// TCP协议短连接处理器（以网盘为业务）
public class TCPCloudConnectHandler extends ConnectHandler {

    @Override
    public boolean isMatchedRequest(String headMessage) {
        return false;
    }

    @Override
    public Response handle(Request request) {
        return null;
    }

    @Override
    public Response whenException() {
        return null;
    }
}
