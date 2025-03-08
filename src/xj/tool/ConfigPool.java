package xj.tool;

// 配置常量池，用于统一存储配置相关字符串
public interface ConfigPool {

    // 线程池
    interface THREAD_POOL {
        String MAX_THREAD = "thread-pool.pool.max-thread";
        String CORE_THREAD = "thread-pool.pool.core-thread";
        String QUEUE_CAPACITY = "thread-pool.pool.queue-capacity";
        String LONG_CONNECT_MAX_THREAD = "thread-pool.pool.long-connect-max-thread";
        String THREAD_NAME = "thread-pool.thread.thread-name";
        String MAX_FREE_TIME = "thread-pool.thread.max-free-time";
        String REJECT_STRATEGY = "thread-pool.pool.reject-strategy";
    }

    // 服务器
    interface SERVER {
        String PORT = "server.port";
        String NAME = "server.name";
        String UNIT_SPLIT_BREAK = "server.unit-split-break";
        String SOCKET_MAX_WAIT_TIME = "server.socket-max-wait-time";
        String ACCESS_CONTROL_ALLOW_ORIGIN = "server.access-control-allow-origin";
    }

    // 日志
    interface LOG {
        String CHOOSE_CLASS = "log.choose-class";
    }

    // MVC模块
    interface MVC {
        String INDEX_PATH = "mvc.index-path";
        String RESOURCE_PATH = "mvc.resource-path";
    }

    // IOC容器
    interface IOC {
        String EXT_PROGRAM_PATH = "ioc.ext-program-path";
        String SCAN_PACKAGE = "ioc.scan-package";
    }

    // 系统路径
    interface SYSTEM_PATH {
        String CONFIG_FILE_PATH = "resource/config.yml";
        String SYSTEM_WEBPAGE_PATH = "resource/webpage/";
        String UNKNOWN_WEBPAGE = "unknown.html";
    }

    // 系统参数
    interface SYSTEM_ARG {
        String WORK_PATH = "workpath";
        String OS_NAME = "osName";
        String LINE_BREAK = "lineBreak";
    }

    // TCP聊天室模块
    interface CHAT {
        String MESSAGE_CACHE_NUM = "chat.message-cache-num";
        String CACHE_CAPACITY = "chat.cache-capacity";
        String CHAT_IMAGE_PATH = "chat.chat-image-path";
    }

}
