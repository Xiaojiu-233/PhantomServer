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
    public String handleMessage(Date date, LogLevel level,String message, Object... args) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(new SimpleDateFormat("yyyy_MM_dd HH:mm:ss").format(date)).append("]")
                .append("[").append(level.name()).append("] ");
        sb.append(message);
        if(args.length > 0){
            sb.append(" : ").append(args[0].toString());
            for(int i = 1 ;i < args.length ;i++){
                sb.append(" , ").append(args[i].toString());
            }
        }
        return sb.toString();
    }

    @Override
    public String setLogFileName(Date date) {
        return "log_"+ new SimpleDateFormat("yyyyMMdd_HHmmss").format(date);
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
