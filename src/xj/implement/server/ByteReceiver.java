package xj.implement.server;

import xj.interfaces.server.IReceiver;

import java.util.Arrays;

// 字节数据接收器，用于接收与存储字节数据
public class ByteReceiver implements IReceiver {

    // 成员属性
    private volatile byte[] data;// 数据

    private volatile byte[] sign;// 副数据

    // 成员方法
    // 存储数据
    public void storeData(byte[] data) {
        synchronized (this) {
            if(sign == null)
                this.data = data;
            else{
                byte[] newData = new byte[sign.length + data.length];
                System.arraycopy(sign, 0, newData, 0, sign.length);
                System.arraycopy(data, 0, newData, sign.length, data.length);
                this.data = newData;
                sign = null;
            }
        }
    }

    // 存储副数据
    public void storeSignData(byte[] data) {
        synchronized (this) {
            this.sign = data;
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
