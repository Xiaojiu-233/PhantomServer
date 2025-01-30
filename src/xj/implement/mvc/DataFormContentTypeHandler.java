package xj.implement.mvc;

import xj.enums.web.ContentType;
import xj.interfaces.mvc.ContentTypeHandler;
import xj.tool.StrPool;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// 表单数据的ContentType数据处理器
public class DataFormContentTypeHandler implements ContentTypeHandler {

    @Override
    public boolean isMatchContentType(ContentType contentType) {
        return contentType.equals(ContentType.MULTIPART_FORM_DATA);
    }

    @Override
    public Map<String, Object> handle(byte[] bytes,Map<String, String> contentTypeArgs) {
        // 数据准备
        Map<String, Object> ret = new HashMap<String, Object>();
        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)));
        String line = null;
        String boundary = contentTypeArgs.get(StrPool.BOUNDARY);
        // 开始读取
        try {
            while ((line = br.readLine()) != null) {
                // TODO:解析数据封装为Map，尤其注意文件单体和文件数组的value处理
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // 返回结果
        return ret;
    }
}