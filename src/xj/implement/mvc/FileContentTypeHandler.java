package xj.implement.mvc;

import xj.enums.web.ContentType;
import xj.interfaces.mvc.ContentTypeHandler;

import java.util.Collections;
import java.util.Map;

// 文件以及默认数据的ContentType数据处理器
public class FileContentTypeHandler implements ContentTypeHandler {

    @Override
    public boolean isMatchContentType(ContentType contentType) {
        return true;
    }

    @Override
    public Map<String, Object> handle(byte[] bytes) {
        return Collections.emptyMap();
    }
}