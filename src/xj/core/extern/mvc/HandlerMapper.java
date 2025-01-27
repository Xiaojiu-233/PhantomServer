package xj.core.extern.mvc;

import xj.enums.web.RequestMethod;

import java.lang.reflect.Method;

// MVC框架使用的映射处理器对象，用于存储映射相关的数据与映射处理方法
public class HandlerMapper {

    // 成员属性
    private RequestMethod method;// 请求方法

    private Method handleMethod;// 映射处理方法

    // 成员方法
    // 初始化
    public HandlerMapper(RequestMethod method, Method handleMethod) {
        this.method = method;
        this.handleMethod = handleMethod;
    }

    public RequestMethod getMethod() {
        return method;
    }

    public Method getHandleMethod() {
        return handleMethod;
    }
}
