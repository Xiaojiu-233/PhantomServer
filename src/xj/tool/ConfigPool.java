package xj.tool;

// 配置常量池，用于统一存储配置相关字符串
public interface ConfigPool {

    // 线程池
    public interface THREAD_POOL {
        public static final String MAX_THREAD = "thread-pool.pool.max-thread";
        public static final String CORE_THREAD = "thread-pool.pool.core-thread";
        public static final String QUEUE_CAPACITY = "thread-pool.pool.queue-capacity";
        public static final String LONG_CONNECT_MAX_THREAD = "thread-pool.pool.long-connect-max-thread";
        public static final String THREAD_NAME = "thread-pool.thread.thread-name";
        public static final String MAX_FREE_TIME = "thread-pool.thread.max-free-time";
        public static final String REJECT_STRATEGY = "thread-pool.pool.reject-strategy";
    }

    // 服务器
    public interface SERVER {
        public static final String PORT = "server.port";
        public static final String NAME = "server.name";
    }

    // 日志
    public interface LOG {
        public static final String CHOOSE_CLASS = "log.choose-class";
    }

    // MVC模块
    public interface MVC {
        public static final String INDEX_PATH = "mvc.index-path";
        public static final String RESOURCE_PATH = "mvc.resource-path";
    }

    // IOC容器
    public interface IOC {
        public static final String EXT_PROGRAM_PATH = "ioc.ext-program-path";
        public static final String SCAN_PACKAGE = "ioc.scan-package";
    }

    // 系统路径
    public interface SYSTEM_PATH {
        public static final String CONFIG_FILE_PATH = "resource/config.yml";
        public static final String SYSTEM_WEBPAGE_PATH = "resource/webpage/";
        public static final String UNKNOWN_WEBPAGE = "unknown.html";
    }

}
