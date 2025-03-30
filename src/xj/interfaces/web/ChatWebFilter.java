package xj.interfaces.web;

import xj.abstracts.connect.ConnectHandler;
import xj.implement.connect.HTTPConnectHandler;
import xj.implement.connect.TCPChatConnectHandler;

/**
 * TCP聊天室协议专用过滤器
 */
public interface ChatWebFilter extends WebFilter {

    default Class<? extends ConnectHandler> getBelongConnectHandler(){
        return TCPChatConnectHandler.class;
    }
}
