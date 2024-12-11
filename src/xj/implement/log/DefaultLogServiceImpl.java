package xj.implement.log;

import xj.enums.log.LogLevel;
import xj.interfaces.log.LogService;

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
        // 前缀处理
        sb.append("[").append(new SimpleDateFormat("yyyy_MM_dd HH:mm:ss").format(date)).append("]")
                .append("[").append(level.name()).append("] ");
        // 消息处理：如果消息中存在{}，则将参数填充进去
        String[] msg = message.split("\\{\\}");
        int msgLen = msg.length;
        if(msgLen > 1){
            sb.append(msg[0]);
            for(int i = 1;i < msgLen;i++){
                sb.append(i-1 < args.length ? args[i-1].toString() : "").append(msg[i]);
            }
            if(message.endsWith("{}")){
                sb.append(args[msgLen-1].toString());
            }
        }else{
            sb.append(message);
            if(args.length > 0){
                sb.append(" : ").append(args[0].toString());
                for(int i = 1 ;i < args.length ;i++){
                    sb.append(" , ").append(args[i].toString());
                }
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
