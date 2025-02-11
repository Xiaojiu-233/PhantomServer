package xj.implement.connect;

import xj.abstracts.connect.ConnectHandler;
import xj.abstracts.web.Request;
import xj.abstracts.web.Response;

// TCP协议长连接处理器
public class TCPLongConnectHandler extends ConnectHandler {

    @Override
    public boolean isMatchedRequest(Request request) {
        return false;
    }

    @Override
    public Response handle(Request request) {
        return null;
    }

    @Override
    public boolean needEndConnection() {
        return false;
    }
}
