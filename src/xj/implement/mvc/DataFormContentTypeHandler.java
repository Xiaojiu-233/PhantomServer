package xj.implement.mvc;

import xj.component.log.LogManager;
import xj.enums.web.ContentType;
import xj.interfaces.mvc.ContentTypeHandler;
import xj.tool.StrPool;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

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
        String boundaryEnd = contentTypeArgs.get(StrPool.BOUNDARY) + StrPool.HYPHEN + StrPool.HYPHEN;
        int boundaryEndLen = boundaryEnd.length();
        boolean readConfig = false;
        String parName = null;
        String parFileName = null;
        String parContentType = null;
        String textBuffer = null;
        ByteArrayOutputStream fileBos = new ByteArrayOutputStream();
        // 开始读取
        try {
            while ((line = br.readLine()) != null) {
                if(!readConfig) {
                    // 数据读取阶段
                    if(line.length() > boundaryEndLen) {
                        // 将文件或文本数据存至缓冲
                        if(parFileName != null){
                            fileBos.write(line.getBytes());
                            fileBos.write(StrPool.ENTER.getBytes());
                        }else{
                            textBuffer = line;
                        }
                    }else if(parName != null && line.equals(boundaryEnd) || line.equals(boundaryEndLen)) {
                        // 将当前数据存至结果
                        Object parValue = null;
                        Object beforeValue = ret.get(parName);
                        if(parFileName != null)
                            parValue = new MultipartFileImpl(parName, parFileName, parContentType, fileBos.toByteArray());
                        else
                            parValue = textBuffer;
                        if(beforeValue == null) {
                            ret.put(parName, parValue);
                        }else{
                            Class<?> elementClass = null;
                            if(beforeValue instanceof List)
                                elementClass = ((List<?>)beforeValue).get(0).getClass();
                            else
                                elementClass = beforeValue.getClass();
                            if(parValue.getClass().equals(elementClass)) {
                                List<Object> list = new ArrayList<>();
                                if(beforeValue instanceof List)
                                    list = (List) beforeValue;
                                else
                                    list.add(beforeValue);
                                list.add(parValue);
                                ret.put(parName, list);
                            }else{
                                LogManager.error_("插入参数类型[{}]与原本参数类型[{}]不一致，无法存储于同一列表中！",parValue.getClass(),elementClass);
                                continue;
                            }
                        }
                        // 准备新的数据读取，进入配置读取阶段
                        parName = null;
                        parFileName = null;
                        parContentType = null;
                        textBuffer = null;
                        fileBos = new ByteArrayOutputStream();
                        readConfig = true;
                    }
                }else{
                    // 配置读取阶段
                    if(line.isEmpty()) readConfig = false;
                    // 数据不为空则开始解析数据
                    String[] args = line.replace(StrPool.QUOTATION_MARK,StrPool.EMPTY)
                            .replace(StrPool.SEMICOLON,StrPool.EMPTY).split(StrPool.SPACE);
                    for(String arg : args) {
                        if(arg.equals(StrPool.NAME))
                            parName = arg.split(StrPool.COLON)[1];
                        else if(arg.equals(StrPool.FILE + StrPool.NAME))
                            parFileName = arg.split(StrPool.COLON)[1];
                    }
                }
            }
        } catch (IOException e) {
            LogManager.error_("表单ContentType数据解析数据时出现了异常",e);
        }
        // 处理结果数据，将值由列表转化为数组
        ret = ret.entrySet().stream()
                .peek(obj -> obj.setValue(
                        obj.getValue() instanceof List ? ((List<?>)(obj.getValue())).toArray()
                                : obj.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey,Map.Entry::getValue));
        // 返回结果
        return ret;
    }
}