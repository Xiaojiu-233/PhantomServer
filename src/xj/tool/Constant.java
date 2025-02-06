package xj.tool;

// 常量池，用于储存非字符串的常量数据
public interface Constant {

    public static int DAY_SECONDS = 60 * 60 * 24;// 一天的秒数

    public static int RECOMMEND_FREE_TIME = 180;// 推荐的最大闲置时间

    public static int BYTES_UNIT_CAPACITY = 2048;// 字节存储运输单元大小

    public static int ZERO_COUNT_LIMIT = 25;// 零数据上限，当超过指定次数没有获取数据，则直接读取下一轮数据
}
