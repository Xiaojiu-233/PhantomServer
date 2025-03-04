package xj.implement.connect;

import xj.abstracts.connect.ConnectHandler;
import xj.abstracts.web.Request;
import xj.abstracts.web.Response;
import xj.component.log.LogManager;
import xj.core.extern.chat.ChatManager;
import xj.core.extern.mvc.MVCManager;
import xj.enums.web.ChatType;
import xj.implement.web.HTTPRequest;
import xj.implement.web.TCPChatRequest;
import xj.implement.web.TCPChatResponse;
import xj.tool.StrPool;

// TCP协议长连接处理器（以聊天室为业务）
public class TCPChatConnectHandler extends ConnectHandler {

    // 成员属性
    private boolean endConnect = false;

    // 成员方法
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
        // 如果请求内容为请求连接或者断开连接，则返回成功消息并作出处理
        ChatType type = chatRequest.getChatObject().getType();

        if(ChatType.IMAGE.equals(type)){
            LogManager.debug_("那很舒服了");
        }

        if(ChatType.CONNECT.equals(type)) {
            LogManager.debug_("收到开始连接了！");
            return new TCPChatResponse(StrPool.SUCCESS,"连接成功");
        }else if(ChatType.FIN.equals(type)){
            endConnect = true;
            LogManager.debug_("收到结束连接了！");
            return new TCPChatResponse(StrPool.SUCCESS,"断开连接成功");
        }
        // 转交给聊天室模块进行处理
        return ChatManager.getInstance().handle(chatRequest);
    }

    @Override
    public boolean needEndConnection() {
        return endConnect;
    }

    @Override
    public Response whenException() {
        return new TCPChatResponse(StrPool.FAILURE,"服务器内部错误，消息处理异常");
    }
}
