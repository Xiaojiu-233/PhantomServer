package xj.abstracts.web;

import xj.component.conf.ConfigureManager;
import xj.tool.Constant;

import java.util.Arrays;

// TCP通讯消息请求数据包，提供数据包对象统一抽象类
public abstract class Request {

    // 成员属性
    protected byte[] data;// 数据

    protected String lineBreak;// 换行符

    protected String headMsg;// 头数据

    // 成员方法
    // 构造方法
    public Request(byte[] data) {
        // 读取数据
        this.data = data;
        lineBreak = (String) ConfigureManager.getInstance().getConfig("lineBreak");
        // 获取头数据（读到换行符之前的数据）
        int pos = 0;
        for(int i= 0;i<data.length;i++)
            if(data[i] == '\n'){
                pos = i;break;
            }
        byte[] headDataBytes = Arrays.copyOfRange(data,0,pos);
        headMsg = new String(headDataBytes);
    }

    // 将数据编码为字符串数组
    public String[] encodeToString() {
        return new String(data).split(lineBreak);
    }

    // 返回数据
    public byte[] getData() {
        return data;
    }

    // 返回头数据
    public String getHeadMsg() {
        return headMsg;
    }
}
