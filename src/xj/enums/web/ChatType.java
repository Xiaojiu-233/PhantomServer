package xj.enums.web;

// TCP聊天室业务中聊天记录的类型
public enum ChatType {

    MESSAGE, // 文字消息
    IMAGE, // 图片消息
    OFFSET, // 请求消息同步
    CONNECT, // 开启连接
    FIN; // 结束连接
}
