package xj.core.server.selector;

// 选择器使用的请求单元，用于将请求以单元的形式存储使用
public class SelectorRequestUnit {

    // 成员属性
    private String headMessage;// 头信息

    private byte[] data;// 数据

    // 成员方法
    // 初始化
    public SelectorRequestUnit() {
    }

    public SelectorRequestUnit(String headMessage, byte[] data) {
        this.headMessage = headMessage;
        this.data = data;
    }

    public String getHeadMessage() {
        return headMessage;
    }

    public void setHeadMessage(String headMessage) {
        this.headMessage = headMessage;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
