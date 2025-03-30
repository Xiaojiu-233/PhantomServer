package xj.abstracts.web;

import xj.component.conf.ConfigureManager;
import xj.tool.ConfigPool;
import xj.tool.Constant;

import java.util.Arrays;

/**
 * TCP通讯消息请求数据包，提供数据包对象统一抽象类
 * */
public abstract class Request {

    // 成员属性
    protected byte[] data;// 数据

    protected String lineBreak;// 换行符

    protected String headMsg;// 头数据

    protected String remoteIp;// 请求IP

    // 成员方法
    /**
     * 构造方法
     */
    public Request(byte[] data,String headMsg) {
        // 读取数据
        this.data = data;
        lineBreak = (String) ConfigureManager.getInstance().getConfig(ConfigPool.SYSTEM_ARG.LINE_BREAK);
        // 获取头数据
        this.headMsg = headMsg;
    }
    public Request(byte[] data,String headMsg,String remoteIp) {
        this(data,headMsg);
        this.remoteIp = remoteIp;
    }

    /**
     * 将数据编码为字符串数组
     */
    public String[] encodeToString() {
        return new String(data).split(lineBreak);
    }

    /**
     * 返回数据
     */
    public byte[] getData() {
        return data;
    }

    /**
     * 返回头数据
     */
    public String getHeadMsg() {
        return headMsg;
    }

    /**
     * 获取请求IP
     */
    public String getRemoteIp() {
        return remoteIp;
    }
}
