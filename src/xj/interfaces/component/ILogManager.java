package xj.interfaces.component;

import xj.enums.log.LogLevel;

import java.util.Date;

/**
 * 日志管理器的接口形式，为拓展程序用户提供调用日志的接口 <br>
 * 默认日志服务下，如果存在两个及以上的参数时，可以在内容中添加 {} 把参数放到对应位置
 */
public interface ILogManager {

    /**
     * 普通通知
     * @param message 内容
     * @param args 参数
     */
    void info(String message,Object... args);

    /**
     * 测试用通知
     * @param message 内容
     * @param args 参数
     */
    void warn(String message,Object... args);

    /**
     * 警告通知
     * @param message 内容
     * @param args 参数
     */
    void debug(String message,Object... args);

    /**
     * 报错通知
     * @param message 内容
     * @param args 参数
     */
    void error(String message,Object... args);

}
