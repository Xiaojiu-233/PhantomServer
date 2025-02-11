package xj.implement.server;

import xj.interfaces.server.IReceiver;

// 字节数据接收器，用于接收与存储字节数据
public class ByteReceiver implements IReceiver {

    // 成员属性
    private volatile byte[] data;

    // 成员方法
    // 存储数据
    public void storeData(byte[] data) {
        synchronized (this) {
            this.data = data;
        }
    }

    // 数据是否存在
    public boolean dataExist(){
        return data != null;
    }

    // 获取数据
    public byte[] getData() {
        return data;
    }

    // 数据重置
    public void resetData(){
        synchronized (this) {
            this.data = null;
        }
    }

}
