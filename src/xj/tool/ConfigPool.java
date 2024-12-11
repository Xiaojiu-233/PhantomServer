package xj.tool;

// 配置常量池，用于统一存储配置相关字符串
public interface ConfigPool {

    // 线程池
    public interface THREAD_POOL {
        public static final String MAX_THREAD = "thread-pool.max-thread";
        public static final String CORE_THREAD = "thread-pool.core-thread";
        public static final String QUEUE_CAPACITY = "thread-pool.queue-capacity";
        public static final String LONG_CONNECT_MAX_THREAD = "thread-pool.long-connect-max-thread";
        public static final String THREAD_NAME = "thread-pool.thread-name";
        public static final String REJECT_STRATEGY = "thread-pool.reject-strategy";
    }

    // 服务器
    public interface SERVER {
        public static final String PORT = "server.port";
    }

    // 日志
    public interface LOG {
        public static final String CHOOSE_CLASS = "log.choose-class";
    }

}
