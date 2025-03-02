package xj.core.extern.chat;

import xj.component.conf.ConfigureManager;
import xj.component.log.LogManager;
import xj.tool.ConfigPool;
import xj.tool.StrPool;

// TCP聊天室偏移量存储对象，用于存储消息偏移量等信息
public class OffsetData {

    // 成员属性
    private int offset; // 偏移量

    private String uid; // 缓存块id

    // 成员方法
    // 初始化
    public OffsetData(int offset, String uid) {
        this.offset = offset;
        this.uid = uid;
    }
    public OffsetData() {
    }

    // 解析String为偏移量对象
    public static OffsetData analyseOffsetData(String data) {
        if(data == null || data.isEmpty())
            return null;
        OffsetData offsetData = new OffsetData();
        String[] datas = data.split(StrPool.SPACE);
        if(datas.length == 2) {
            offsetData.uid = datas[0];
            offsetData.offset = Integer.parseInt(datas[1]);
        }
        return offsetData;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public String toString() {
        return uid + StrPool.SPACE + offset;
    }
}
