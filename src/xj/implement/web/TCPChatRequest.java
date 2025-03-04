package xj.implement.web;

import xj.abstracts.web.Request;
import xj.component.log.LogManager;
import xj.core.extern.chat.ChatObject;
import xj.core.extern.chat.OffsetData;
import xj.enums.web.ChatType;
import xj.tool.StrPool;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

// TCP协议聊天室业务请求对象，用于处理聊天室业务
public class TCPChatRequest extends Request {

    // 成员属性
    private ChatObject chatObject;// 消息对象

    private LocalDateTime startTime;// 聊天发起时间

    private OffsetData offsetData;// 请求偏移量

    private byte[] bodyBytes = new byte[0];// 请求体二进制版本

    // 成员方法
    // 初始化
    public TCPChatRequest(Request request) {
        super(request.getData(), request.getHeadMsg());
        selfAnalysis();
    }

    // 自解析
    private void selfAnalysis(){
        // 拆解数据
        String[] lines = encodeToString();
        // 判定数据是否符合规范
        if(lines.length < 5){
            LogManager.error_("TCP聊天室请求对象无法解析请求数据");
            return;
        }
        // 从中获取发起者名称、时间、消息类型
        String name = lines[1].split(StrPool.COLON + StrPool.SPACE)[1];
        String date = lines[2].split(StrPool.COLON + StrPool.SPACE)[1];
        String message = null;
        ChatType type = ChatType.valueOf(lines[3].split(StrPool.COLON + StrPool.SPACE)[1]);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(StrPool.STANDARD_DATE_FORMAT);
        startTime = (date == null || date.equals(StrPool.NULL)) ? LocalDateTime.now() : LocalDateTime.parse(date, formatter);
        // 拆除请求头获取消息体
        int headerBytes = 0;
        for(int i= 0; i<= 3; i++)
            headerBytes += lines[i].getBytes().length;
        headerBytes += lineBreak.getBytes().length * 4;
        byte[] bodyByteData = Arrays.copyOfRange(data,headerBytes,data.length);
        // 根据消息类型进一步处理消息体
        if(type.equals(ChatType.IMAGE))
            bodyBytes = bodyByteData;
        else if(type.equals(ChatType.MESSAGE)){
            message = new String(bodyByteData).replace(lineBreak,StrPool.EMPTY);
        }else if(type.equals(ChatType.OFFSET)){
            offsetData = OffsetData.analyseOffsetData(new String(bodyByteData).replace(lineBreak,StrPool.EMPTY));
        }
        // 封装成为消息对象
        chatObject = new ChatObject(name,date,type,message);
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public ChatObject getChatObject() {
        return chatObject;
    }

    public byte[] getBodyBytes() {
        return bodyBytes;
    }

    public OffsetData getOffsetData() {
        return offsetData;
    }
}
