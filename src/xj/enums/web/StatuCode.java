package xj.enums.web;

/**
 * 状态码
 */
public enum StatuCode {

    OK(200,"200 OK"),// 正常
    CONTINUE(100,"100 Continue"),// 一切正常，客户端应继续请求
    MOVED_PERMANENTLY(301,"301 Moved Permanently"),// 资源（网页等）被永久转移到其它URL
    FORBIDDEN(403,"403 Forbidden"),// 被禁止访问
    NOT_FOUND(404,"404 Not Found"),// 没有找到资源
    METHOD_NOT_ALLOWED(405,"405 Method Not Allowed"),// 错误的请求方法
    INTERNAL_SERVER_ERROR(500,"500 Internal Server Error");// 服务器内部错误

    private final int code; // 状态码
    private final String name; // 状态码名称

    StatuCode(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}