package xj.tool;

// 字符串常量池，用于将常用的字符串诸如符号等存储起来
public interface StrPool {

    // 符号
    public static final String POINT = ".";
    public static final String BACK_SLASH = "\\";
    public static final String BACK_SLASH_REGEX = "\\\\";
    public static final String SLASH = "/";
    public static final String COLON = ":";
    public static final String SPACE = " ";
    public static final String UNDERLINE = "_";
    public static final String HYPHEN = "-";
    public static final String QUESTION_MARK = "?";
    public static final String AND = "&";
    public static final String EQUAL = "=";

    // 后缀
    public static final String LOG_POINT = ".log";

    // 协议
    public static final String HTTP = "HTTP";
}
