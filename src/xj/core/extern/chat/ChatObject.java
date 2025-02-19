package xj.core.extern.chat;

import xj.enums.web.ChatType;

import java.io.Serializable;

// TCP聊天室消息存储对象，用于存储一段聊天记录
public class ChatObject implements Serializable {

    // 成员属性
    private String name;// 消息发起者

    private String date;// 消息发起时间

    private ChatType type;// 消息类型

    private String message;// 消息

    // 成员方法
    // 初始化
    public ChatObject() {
    }
    public ChatObject(String name, String date, ChatType type, String message) {
        this.name = name;
        this.date = date;
        this.type = type;
        this.message = message;
    }

    // 获取与设置数据
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public ChatType getType() {
        return type;
    }

    public void setType(ChatType type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
