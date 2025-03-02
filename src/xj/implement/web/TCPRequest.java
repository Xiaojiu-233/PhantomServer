package xj.implement.web;

import xj.abstracts.web.Request;

// TCP协议请求对象，用于处理非HTTP协议的TCP协议
public class TCPRequest extends Request {

    // 成员属性

    // 成员方法
    // 构造方法
    public TCPRequest(byte[] data,String headMsg) {
        super(data,headMsg);
    }
}
