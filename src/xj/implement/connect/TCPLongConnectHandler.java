package xj.implement.connect;

import xj.interfaces.connect.Request;
import xj.interfaces.connect.Response;

// TCP协议长连接处理器
public class TCPLongConnectHandler extends ConnectHandler {

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
