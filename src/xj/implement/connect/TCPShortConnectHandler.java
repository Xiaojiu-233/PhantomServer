package xj.implement.connect;

import xj.abstracts.connect.ConnectHandler;
import xj.abstracts.web.Request;
import xj.abstracts.web.Response;

// TCP协议短连接处理器
public class TCPShortConnectHandler extends ConnectHandler {

    @Override
    public boolean isMatchedRequest(Request request) {
        return false;
    }

    @Override
    public void handle(Request request) {

    }

    @Override
    public Response returnResponse() {
        return null;
    }
}
