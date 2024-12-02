package xj.component.log;

import xj.component.conf.ConfigureManager;
import xj.interfaces.LogService;
import xj.tool.StrPool;

import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

// 日志管理器，用于管理与输出服务器产生的各种日志、选择处理日志拓展程序
public class LogManager {

    // 成员属性
    private static LogManager instance;// 单例模式实现

    private String outputFilePath;// 输出文件路径

    private Writer outputWriter;// 字符输出流

    private LogService logService;// 日志的服务接口实现类

    // 成员行为
    // 初始化
    public LogManager(){
        // 读取配置并执行相应策略
        outputFilePath = (String) ConfigureManager.getInstance().getConfig("log.output-file-path");
        String outputFile = outputFilePath + StrPool.SLASH + logService.logFileName(new Date())
                + StrPool.LOG_POINT;
        // 初始化输出流等资源
        try {
            outputWriter = new BufferedWriter(new FileWriter(outputFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    // 获取单例
    public static LogManager getInstance(){
        if(instance == null){
            instance = new LogManager();
        }
        return instance;
    }

    // 日志记录
    public void log(String message,Object... args){
        // 通过service初步处理消息
        String handledMsg = logService.handleMessage(message,args);
        // 执行日志输出等操作
    }

    // 销毁时
    public void destroy(){
        // 停止输出流等资源
        try {
            outputWriter.flush();
            outputWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
