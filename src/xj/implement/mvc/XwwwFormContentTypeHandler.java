package xj.implement.mvc;

import xj.enums.web.ContentType;
import xj.interfaces.mvc.ContentTypeHandler;
import xj.tool.StrPool;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// 网页特殊表单数据的ContentType数据处理器
public class XwwwFormContentTypeHandler implements ContentTypeHandler {

    @Override
    public boolean isMatchContentType(ContentType contentType) {
        return contentType != null && contentType.equals(ContentType.APPLICATION_X_WWW_FORM);
    }

    @Override
    public Map<String, Object> handle(byte[] bytes,Map<String, String> contentTypeArgs) {
        Map<String, Object> ret = new HashMap<String, Object>();
        String[] formData = new String(bytes).split(StrPool.AND);
        for (String formDataItem : formData) {
            String[] keyValue = formDataItem.split(StrPool.EQUAL);
            ret.put(keyValue[0], keyValue[1]);
        }
        return ret;
    }
}