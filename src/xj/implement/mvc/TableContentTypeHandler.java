package xj.implement.mvc;

import xj.enums.web.ContentType;
import xj.interfaces.mvc.ContentTypeHandler;

import java.util.Collections;
import java.util.Map;

// 表单数据的ContentType数据处理器
public class TableContentTypeHandler implements ContentTypeHandler {

    @Override
    public boolean isMatchContentType(ContentType contentType) {
        return contentType.equals(ContentType.APPLICATION_X_WWW_FORM);
    }

    @Override
    public Map<String, Object> handle(byte[] bytes) {
        return Collections.emptyMap();
    }
}