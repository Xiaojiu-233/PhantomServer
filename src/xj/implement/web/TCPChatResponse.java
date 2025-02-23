package xj.implement.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.fasterxml.jackson.databind.util.JSONWrappedObject;
import jdk.nashorn.internal.parser.JSONParser;
import xj.abstracts.web.Response;
import xj.core.extern.chat.ChatObject;
import xj.core.extern.chat.OffsetData;
import xj.enums.web.ChatType;
import xj.tool.StrPool;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

// TCP协议聊天室业务响应对象，用于处理聊天室业务
public class TCPChatResponse extends Response {

    // 成员属性
    private String result;// 响应结果

    private String message;// 响应消息

    private byte[] fileData;// 响应文件数据

    private OffsetData offsetData;// 响应偏移量

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
        // 数据准备
        StringBuilder sb = new StringBuilder();
        String endStr = null;
        // 填充请求头
        sb.append(StrPool.TCP).append(StrPool.SPACE).append(StrPool.CHAT).append(StrPool.ENTER);
        sb.append(StrPool.RESULT).append(StrPool.COLON).append(StrPool.SPACE).append(result).append(StrPool.ENTER);
        sb.append(StrPool.MESSAGE).append(StrPool.COLON).append(StrPool.SPACE).append(message).append(StrPool.ENTER);
        sb.append(StrPool.NEW_OFFSET).append(StrPool.COLON).append(StrPool.SPACE)
                .append(offsetData).append(StrPool.ENTER).append(StrPool.ENTER);
        // 填充请求体
        for(ChatObject obj : obs) {
            String jsonStr = new ObjectMapper().writeValueAsString(obj);
            if(ChatType.MESSAGE.equals(obj.getType())) {
                // 如果是消息则直接装入数据并换行
                sb.append(jsonStr).append(StrPool.ENTER);
            }else if(ChatType.IMAGE.equals(obj.getType())){
                // 如果是图片则进行拆分再填充，并结束本次读取
                String uuid = obj.getMessage();
                String[] jsonSplit = jsonStr.split(uuid);
                if(jsonSplit.length != 2)
                    throw new IOException("图片对象json解析不存在uuid数据，解析失败");
                sb.append(jsonSplit[0]);
                endStr = jsonSplit[1] + StrPool.ENTER;
            }
        }
        // 写入响应
        ByteBuffer buffer = null;
        byte[] headBytes = sb.toString().getBytes();
        if(endStr != null) {
            byte[] endBytes = endStr.getBytes();
            buffer = ByteBuffer.allocate(headBytes.length + fileData.length + endBytes.length);
            buffer.put(headBytes);
            buffer.put(fileData);
            buffer.put(endBytes);
        }else{
            buffer = ByteBuffer.allocate(headBytes.length);
            buffer.put(headBytes);
        }
        buffer.flip();
        os.write(buffer);
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

    public OffsetData getOffsetData() {
        return offsetData;
    }

    public void setOffsetData(OffsetData offsetData) {
        this.offsetData = offsetData;
    }
}
