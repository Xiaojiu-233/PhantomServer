package xj.interfaces.component;

import xj.enums.log.LogLevel;

import java.util.Date;

// 日志管理器的接口形式，为拓展程序用户提供调用日志的接口
public interface ILogManager {

    void info(String message,Object... args);

    void warn(String message,Object... args);

    void debug(String message,Object... args);

    void error(String message,Object... args);

}
