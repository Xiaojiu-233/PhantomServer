package xj.interfaces.log;

import xj.enums.log.LogLevel;

import java.io.IOException;
import java.io.Writer;
import java.util.Date;

/**
 * 日志服务接口<br>
 * 实现这个接口的类都可以作为LogManager的拓展程序使用
 */
public interface LogService {

    /**
     * 处理接入的消息
     */
    public String handleMessage(Date date, LogLevel level, String message, Object... args);

    /**
     * 确定日志输出文件的名字，以日期为参数
     */
    public String setLogFileName(Date date);

    /**
     * 确定输出流，参数为输出日志文件的完整路径
     */
    public Writer setOutputWriter(String outputFile) throws IOException;

    /**
     * 额外输出的方式，诸如命令行输出等
     */
    public void exOutputForm(String msg);
}
