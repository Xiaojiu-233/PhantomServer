package xj.interfaces.web;

import xj.abstracts.connect.ConnectHandler;
import xj.implement.connect.HTTPConnectHandler;

/**
 * HTTP协议专用过滤器
 */
public interface HttpWebFilter extends WebFilter {

    default Class<? extends ConnectHandler> getBelongConnectHandler(){
        return HTTPConnectHandler.class;
    }
}
