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
    String LIST = "list";
    String GET_DATA = "getData";
    String SET_DATA = "setData";
    String BOUNDARY = "boundary";
    String SUCCESS = "success";
    String FAILURE = "failure";
    String RESULT = "result";
    String MESSAGE = "message";
    String NEW_OFFSET = "newOffset";
    String NULL = "null";
    String LOCAL_ADDRESS_IPV4 = "127.0.0.1";// 本地地址IPV4
    String LOCAL_ADDRESS_IPV6 = "0:0:0:0:0:0:0:1";// 本地地址IPV6

    // 日期格式
    String STANDARD_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

}
