package xj.implement.mvc;

import xj.enums.web.ContentType;
import xj.interfaces.mvc.ContentTypeHandler;

import java.util.Collections;
import java.util.Map;

// JSON的ContentType数据处理器
public class JsonContentTypeHandler implements ContentTypeHandler {

    @Override
    public boolean isMatchContentType(ContentType contentType) {
        return contentType.equals(ContentType.APPLICATION_JSON);
    }

    @Override
    public Map<String, Object> handle(byte[] bytes) {
        return Collections.emptyMap();
    }
}
