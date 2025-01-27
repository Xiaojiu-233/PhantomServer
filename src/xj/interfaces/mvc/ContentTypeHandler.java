package xj.interfaces.mvc;

import xj.core.extern.mvc.MVCManager;
import xj.enums.web.ContentType;

import java.util.Map;

// MVC框架使用的ContentType数据处理接口，统一ContentType映射数据处理的方法
public interface ContentTypeHandler {

    // 根据得到的ContentType进行匹配
    boolean isMatchContentType(ContentType contentType);

    // 对接收到的数据进行处，返回Map对象
    Map<String,Object> handle(byte[] bytes);

}
