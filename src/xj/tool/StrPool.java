package xj.tool;

// 字符串常量池，用于将常用的字符串诸如符号等存储起来
public interface StrPool {

    // 符号
    public static final String POINT = ".";
    public static final String EMPTY = "";
    public static final String BACK_SLASH = "\\";
    public static final String SLASH = "/";
    public static final String COLON = ":";
    public static final String SEMICOLON = ";";
    public static final String SPACE = " ";
    public static final String UNDERLINE = "_";
    public static final String HYPHEN = "-";
    public static final String QUESTION_MARK = "?";
    public static final String AND = "&";
    public static final String EQUAL = "=";

    // 后缀
    public static final String LOG_POINT = ".log";
    public static final String CLASS_POINT = ".class";
    public static final String JAR = "jar";
    public static final String HTML = "html";
    public static final String HTML_POINT = ".html";

    // 协议
    public static final String HTTP = "HTTP";

    // jar包类型
    public static final String EXT = "ext";
    public static final String WEB = "web";

    // 请求头
    public static final String SERVER = "Server";
    public static final String CONTENT_TYPE = "Content-Type";

    // 特殊名词
    public static final String FILE = "file";
    public static final String BOUNDARY = "boundary";
}
