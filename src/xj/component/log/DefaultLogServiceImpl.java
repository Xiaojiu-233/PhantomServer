package xj.component.log;

import xj.interfaces.LogLevel;
import xj.interfaces.LogService;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

//默认的日志服务接口实现类
public class DefaultLogServiceImpl implements LogService {

    @Override
    public String handlePrefixFormat(Date date, LogLevel level) {
        return "[" + new SimpleDateFormat("yyyy_MM_dd HH:mm:ss").format(date) + "]"
                + "[" + level.name() + "] ";
    }

    @Override
    public String handleMessage(String message, Object... args) {
        String arg = args.toString();
        return message + " : " + arg;
    }

    @Override
    public String setLogFileName(Date date) {
        return "log_"+ new SimpleDateFormat("yyyy_MM_dd HH:mm:ss").format(date);
    }

    @Override
    public Writer setOutputWriter(String outputFile) throws IOException {
        return new BufferedWriter(new FileWriter(outputFile));
    }

    @Override
    public void exOutputForm(String msg) {
        System.out.println(msg);
    }
}
