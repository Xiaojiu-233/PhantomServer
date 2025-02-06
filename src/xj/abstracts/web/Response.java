package xj.abstracts.web;

import xj.component.conf.ConfigureManager;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.SocketChannel;

// TCP通讯消息响应数据包，提供数据包对象统一抽象类
public abstract class Response {

    // 成员属性
    protected String lineBreak;// 换行符

    // 成员方法
    // 构造方法
    public Response() {
        lineBreak = (String) ConfigureManager.getInstance().getConfig("lineBreak");
    }

    // 将数据响应给socket输出流
    public abstract void writeMessage(SocketChannel os) throws IOException;

}
