package xj.implement.connect;

import xj.abstracts.connect.ConnectHandler;
import xj.abstracts.web.Request;
import xj.abstracts.web.Response;
import xj.core.extern.chat.ChatManager;
import xj.core.extern.mvc.MVCManager;
import xj.implement.web.HTTPRequest;
import xj.implement.web.TCPChatRequest;
import xj.implement.web.TCPChatResponse;
import xj.tool.StrPool;

// TCP协议长连接处理器（以聊天室为业务）
public class TCPChatConnectHandler extends ConnectHandler {

    @Override
    public boolean isMatchedRequest(Request request) {
        String[] firstLineArgs = request.getHeadMsg().split(StrPool.SPACE );
        return firstLineArgs.length == 2 && firstLineArgs[0].contains(StrPool.TCP) &&
                firstLineArgs[1].contains(StrPool.CHAT);
    }

    @Override
    public Response handle(Request request) {
        // 将协议请求转化为TCP聊天室请求
        TCPChatRequest chatRequest = new TCPChatRequest(request);
        // 转交给聊天室模块进行处理
        return ChatManager.getInstance().handle(chatRequest);
    }

    @Override
    public boolean needEndConnection() {
        return false;
    }

    @Override
    public Response whenException() {
        return new TCPChatResponse(StrPool.FAILURE,"服务器内部错误，消息处理异常");
    }
}
