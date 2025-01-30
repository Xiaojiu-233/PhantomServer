package xj.implement.mvc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import xj.component.log.LogManager;
import xj.enums.web.ContentType;
import xj.interfaces.mvc.ContentTypeHandler;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

// JSON的ContentType数据处理器
public class JsonContentTypeHandler implements ContentTypeHandler {

    @Override
    public boolean isMatchContentType(ContentType contentType) {
        return contentType.equals(ContentType.APPLICATION_JSON);
    }

    @Override
    public Map<String, Object> handle(byte[] bytes,Map<String, String> contentTypeArgs) {
        try {
            return new ObjectMapper().readValue(new String(bytes),new TypeReference<Map<String, Object>>(){});
        } catch (IOException e) {
            LogManager.error_("MVC模块在将请求数据转化为json的时候出现错误",e);
            throw new RuntimeException(e);
        }
    }
}
