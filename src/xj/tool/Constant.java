package xj.tool;

// 常量池，用于储存非字符串的常量数据
public interface Constant {

    int DAY_SECONDS = 60 * 60 * 24;// 一天的秒数

    int RECOMMEND_FREE_TIME = 180;// 推荐的最大闲置时间

    int BYTES_UNIT_CAPACITY = 2048;// 字节存储运输单元大小

    int MB = 1024 * 1024;// 一MB为多少B

    int SELECTOR_CHANNEL_ID_MAXMIZE = 1000;// SelectorChannel的Id最大值，用于取模

    int ZERO_COUNT_LIMIT = 25;// 零数据上限，当超过指定次数没有获取数据，则直接读取下一轮数据


}
