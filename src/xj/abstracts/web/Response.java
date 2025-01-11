package xj.abstracts.web;

import java.io.IOException;
import java.io.OutputStream;

// TCP通讯消息响应数据包，提供数据包对象统一抽象类
public abstract class Response {

    // 成员属性

    // 成员方法
    // 将数据响应给socket输出流
    public abstract void writeMessage(OutputStream os) throws IOException;

}
