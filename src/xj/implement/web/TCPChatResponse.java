package xj.implement.web;

import xj.abstracts.web.Response;
import xj.core.extern.chat.ChatObject;
import xj.tool.StrPool;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

// TCP协议聊天室业务响应对象，用于处理聊天室业务
public class TCPChatResponse extends Response {

    // 成员属性
    private String result;// 响应结果

    private String message;// 响应消息

    private byte[] fileData;// 响应文件数据

    private String fileKey;// 响应文件Key值，作为存储文件的索引

    private List<ChatObject> obs = new ArrayList<>();// 存储的聊天记录

    // 成员方法
    // 初始化
    public TCPChatResponse() {
        this.result = StrPool.SUCCESS;
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

    public void setResult(String result) {
        this.result = result;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFileKey() {
        return fileKey;
    }

    public void setFileKey(String fileKey) {
        this.fileKey = fileKey;
    }

    public List<ChatObject> getObs() {
        return obs;
    }

    public void setObs(List<ChatObject> obs) {
        this.obs = obs;
    }
}
