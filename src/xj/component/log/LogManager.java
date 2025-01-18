package xj.component.log;

import xj.component.conf.ConfigureManager;
import xj.core.extern.IOCManager;
import xj.implement.log.DefaultLogServiceImpl;
import xj.enums.log.LogLevel;
import xj.interfaces.component.ILogManager;
import xj.interfaces.log.LogService;
import xj.tool.ConfigPool;
import xj.tool.StrPool;

import java.io.*;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

// 日志管理器，用于管理与输出服务器产生的各种日志、选择处理日志拓展程序
public class LogManager implements ILogManager {

    // 成员属性
    private static volatile LogManager instance;// 单例模式实现

    private final String outputFilePath = "log";// 输出文件路径
    private final String prepareFileName = "preparePeriodLog";// 准备阶段日志文件名

    private Writer outputWriter;// 字符输出流
    private final Object writerLock = new Object();// 写锁

    private LogService logService;// 日志的服务接口实现类

    private Queue<String> messageQueue = new LinkedList<>();// 消息队列

    // 成员方法
    // 初始化（服务器准备阶段，由默认配置进行日志记录）
    public LogManager() {
        // 默认对象初始化
        logService = new DefaultLogServiceImpl();
        // 读取配置并执行相应策略
        String outputFile = outputFilePath + StrPool.BACK_SLASH + prepareFileName
                + StrPool.LOG_POINT;
        // 初始化输出流等资源
        try {
            outputWriter = logService.setOutputWriter(outputFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // 开启日志线程
        startLog();
    }

    //初始化（服务器开机阶段，可以执行用户的自定义日志模块）
    public void initLogManager() {
        info_("【日志模块】开始自定义初始化");
        // 读取配置并执行相应策略
        String chooseLogService = (String) ConfigureManager.getInstance().getConfig(ConfigPool.LOG.CHOOSE_CLASS);
        // 通过IOC容器找到对应的logService对象，如果没找到则继续使用默认的
        Object service = IOCManager.getInstance().returnInstanceByName(chooseLogService);
        if(service instanceof LogService){
            info_("日志模块寻找到拓展程序包中的日志服务对象",chooseLogService);
            logService = (LogService) service;
        }else{
            info_("日志模块未能寻找到拓展程序包中的日志服务对象，使用默认日志服务对象");
        }
        // 读取输出文件名
        String outputFile = ConfigureManager.getInstance().getConfig("workpath") + StrPool.BACK_SLASH + outputFilePath
                + StrPool.BACK_SLASH + logService.setLogFileName(new Date()) + StrPool.LOG_POINT;
        try {
            // 没有文件时创建文件
            File file = new File(outputFile);
            if(!file.exists())file.createNewFile();
            synchronized (writerLock){
                // 停止旧的输出流等资源
                if(outputWriter != null){
                    outputWriter.flush();
                    outputWriter.close();
                }
                // 初始化新的输出流等资源
                outputWriter = logService.setOutputWriter(outputFile);
            }
        } catch (Exception e) {
            error_("服务器开机初始化日志模块时出现异常", e);
        }
        info_("【日志模块】自定义初始化完成");
    }

    // 获取单例（防止高并发导致资源访问问题进行双判空保护）
    public static LogManager getInstance() {
        if(instance == null)
            synchronized (LogManager.class){
                if(instance == null)
                    instance = new LogManager();
            }
        return instance;
    }

    // 日志存入队列，交给日志线程处理
    @Override
    public void info(String message,Object... args){
        info_(message, args);
    }
    @Override
    public void warn(String message,Object... args){
        warn_(message, args);
    }
    @Override
    public void debug(String message,Object... args){
        debug_(message, args);
    }
    @Override
    public void error(String message,Object... args){
        error_(message, args);
    }

    // 系统内部静态静态调用，日志存入队列，交给日志线程处理
    public static void info_(String message,Object... args){
        String handledMsg = getInstance().logService.handleMessage(new Date(), LogLevel.INFO,message,args);
        pushIntoQueue(handledMsg);
    }
    public static void warn_(String message,Object... args){
        String handledMsg = getInstance().logService.handleMessage(new Date(), LogLevel.WARNING,message,args);
        pushIntoQueue(handledMsg);
    }
    public static void debug_(String message,Object... args){
        String handledMsg = getInstance().logService.handleMessage(new Date(), LogLevel.DEBUG,message,args);
        pushIntoQueue(handledMsg);
    }
    public static void error_(String message,Object... args){
        String handledMsg = getInstance().logService.handleMessage(new Date(), LogLevel.ERROR,message,args);
        // 寻找异常信息
        Exception e = (Exception) Arrays.stream(args)
                .filter(item -> item instanceof Exception).findFirst().orElse(null);
        // 打印异常信息
        if (e != null) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            e.printStackTrace(printWriter);
            handledMsg += ConfigureManager.getInstance().getConfig("lineBreak") +
                    stringWriter.toString();
            try {
                stringWriter.close();
                printWriter.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

        }
        // 装入日志队列
        pushIntoQueue(handledMsg);
    }

    // 消息存入队列
    private static void pushIntoQueue(String msg){
        synchronized (LogManager.class){
            getInstance().messageQueue.add(msg);
        }
    }

    // 开启日志线程，监听、消耗、输出队列里的日志信息
    public void startLog(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    if(outputWriter != null){
                        String msg = null;
                        // 当队列存在消息时消费消息
                        synchronized (LogManager.class){
                            if(!messageQueue.isEmpty()){
                                msg = (String) messageQueue.poll();
                            }
                        }
                        if(msg == null)continue;
                        // 输出日志
                        try {
                            synchronized (writerLock){
                                outputWriter.write(msg + "\n");
                                outputWriter.flush();
                            }
                        } catch (IOException e) {
                            error_("输出日志记录时出现异常", e);
                        }
                        // 执行service的额外输出任务，诸如命令行显示等其他输出方式
                        logService.exOutputForm(msg);
                    }
                }
            }
        }).start();
    }

    // 销毁时
    public void destroy() {
        try {
            synchronized (writerLock){
                // 停止输出流等资源
                if(outputWriter != null){
                    outputWriter.flush();
                    outputWriter.close();
                }
            }
        } catch (IOException e) {
            error_("关闭日志模块时出现异常", e);
        }

    }
}
