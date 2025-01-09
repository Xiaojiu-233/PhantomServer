package xj.implement.connect;

import xj.interfaces.connect.Request;
import xj.interfaces.connect.Response;

// HTTP协议连接处理器
public class HTTPConnectHandler extends ConnectHandler {

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
