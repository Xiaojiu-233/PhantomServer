package xj.tool;

// 字符串常量池，用于将常用的字符串诸如符号等存储起来
public interface StrPool {

    // 符号
    String POINT = ".";
    String EMPTY = "";
    String BACK_SLASH = "\\";
    String QUOTATION_MARK = "\"";
    String SLASH = "/";
    String COLON = ":";
    String SEMICOLON = ";";
    String SPACE = " ";
    String UNDERLINE = "_";
    String HYPHEN = "-";
    String QUESTION_MARK = "?";
    String AND = "&";
    String EQUAL = "=";

    // 后缀
    String LOG_POINT = ".log";
    String CLASS_POINT = ".class";
    String JAR = "jar";
    String HTML_POINT = ".html";
    String PNG_POINT = ".png";

    // 协议
    String HTTP = "HTTP";
    String TCP = "TCP";

    // 处理类型
    String CHAT = "CHAT";

    // 请求头
    String SERVER = "Server";
    String CONTENT_TYPE = "Content-Type";
    String COOKIE = "Cookie";
    String SET_COOKIE = "Set-Cookie";
    String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";

    // 特殊名词
    String FILE = "file";
    String NAME = "name";
    String BOUNDARY = "boundary";
    String SUCCESS = "success";
    String FAILURE = "failure";
    String RESULT = "result";
    String MESSAGE = "message";
    String NEW_OFFSET = "newOffset";
    String NULL = "null";
    String NONE = "无";
    String LOCAL_ADDRESS_IPV4 = "127.0.0.1";// 本地地址IPV4
    String LOCAL_ADDRESS_IPV6 = "0:0:0:0:0:0:0:1";// 本地地址IPV6

    // 可视化界面
    String WEB_LIST = "list";
    String WEB_GET = "getData";
    String WEB_SET = "setData";
    String WEB_FAIL_END = "已失败";
    String WEB_SUCC_END = "已结束";
    String WEB_PREPARE = "准备中";
    String WEB_RUNNING = "执行中";
    String CHANNEL_ID = "编号";
    String CHANNEL_STATU = "当前的状态";
    String THREAD_WAITING = "等待";
    String THREAD_RUNNING = "执行";
    String THREAD_COMMON = "普通线程";
    String THREAD_CORE = "核心线程";

    // 日期格式
    String STANDARD_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

}
