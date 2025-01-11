package xj.implement.web;

import xj.abstracts.web.Request;

// 协议用请求对象，用于等待确定协议
public class ProtocolRequest extends Request {

    // 成员属性

    // 成员方法
    // 构造函数
    public ProtocolRequest(byte[] data) {
        super(data);
    }
}
