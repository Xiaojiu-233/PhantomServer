package xj.implement.web;

import xj.abstracts.web.Response;
import xj.core.extern.chat.ChatObject;

import java.io.IOException;
import java.nio.channels.SocketChannel;

// TCP协议聊天室业务响应对象，用于处理聊天室业务
public class TCPChatResponse extends Response {

    // 成员属性
    private String result;// 响应结果

    private String message;// 响应消息

    private byte[] fileData;// 响应文件数据

    // 成员方法
    // 初始化
    public TCPChatResponse() {
    }
    public TCPChatResponse(String result, String message) {
        this.result = result;
        this.message = message;
    }

    @Override
    public void writeMessage(SocketChannel os) throws IOException {

    }

    @Override
    public void storeData(byte[] data) {
        fileData = data;
    }
}
