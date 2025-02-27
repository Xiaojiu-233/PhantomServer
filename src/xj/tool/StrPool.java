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

    // 协议
    String HTTP = "HTTP";
    String TCP = "TCP";

    // 处理类型
    String CHAT = "CHAT";
    String CLOUD = "CLOUD";

    // 请求头
    String SERVER = "Server";
    String CONTENT_TYPE = "Content-Type";
    String COOKIE = "Cookie";
    String SET_COOKIE = "Set-Cookie";

    // 特殊名词
    String FILE = "file";
    String NAME = "name";
    String BOUNDARY = "boundary";
    String SUCCESS = "success";
    String FAILURE = "failure";
    String RESULT = "result";
    String MESSAGE = "message";
    String NEW_OFFSET = "newOffset";

    // 日期格式
    String STANDARD_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

}
