package xj.interfaces;

import java.util.Date;

// 日志服务接口
// 实现这个接口的类都可以作为LogManager的拓展程序使用
public interface LogService {

    //处理接入的消息
    public String handleMessage(String message,Object... args);

    //确定日志输出文件的名字，以日期为参数
    public String logFileName(Date date);
}
