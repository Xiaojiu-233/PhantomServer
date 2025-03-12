package xj.implement.mvc;

import xj.component.conf.ConfigureManager;
import xj.component.log.LogManager;
import xj.enums.web.ContentType;
import xj.interfaces.mvc.ContentTypeHandler;
import xj.tool.ConfigPool;
import xj.tool.FileIOUtil;
import xj.tool.StrPool;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

// 表单数据的ContentType数据处理器
public class DataFormContentTypeHandler implements ContentTypeHandler {

    @Override
    public boolean isMatchContentType(ContentType contentType) {
        return contentType != null && contentType.equals(ContentType.MULTIPART_FORM_DATA);
    }

    @Override
    public Map<String, Object> handle(byte[] bytes,Map<String, String> contentTypeArgs) {
        // 数据准备
        List<Integer> splitPos = FileIOUtil.splitByteArrayByLineBreak(bytes,true);
        String lineBreak = (String) ConfigureManager.getInstance()
                .getConfig(ConfigPool.SYSTEM_ARG.LINE_BREAK);
        Map<String, Object> ret = new HashMap<String, Object>();
        String line = null;
        String boundary = StrPool.HYPHEN + StrPool.HYPHEN + contentTypeArgs.get(StrPool.BOUNDARY);
        String boundaryEnd = boundary + StrPool.HYPHEN + StrPool.HYPHEN;
        int boundaryEndLen = boundaryEnd.length();
        boolean readConfig = false;
        String parName = null;
        String parFileName = null;
        String parContentType = null;
        String textBuffer = null;
        int fileBytesStartByte = -1;
        // 开始读取
        for(int i = 0;i < splitPos.size();i+=2) {
            line = splitPos.get(i) == -1 ? "" :
                    new String(Arrays.copyOfRange(bytes,splitPos.get(i),splitPos.get(i+1)+1));
            if(!readConfig) {
                // 数据读取阶段
                if(line.length() > boundaryEndLen || (!line.equals(boundaryEnd) && !line.equals(boundary))) {
                    // 将文件或文本数据存至缓冲
                    if(parFileName != null){
                        if(fileBytesStartByte == -1){
                            fileBytesStartByte = splitPos.get(i);
                        }
                    }else{
                        textBuffer = line;
                    }
                }else {
                    if(parName == null){
                        readConfig = true;
                        continue;
                    }
                    // 将当前数据存至结果
                    Object parValue = null;
                    Object beforeValue = ret.get(parName);
                    if(parFileName != null){
                        parValue = new MultipartFileImpl(parName, parFileName, parContentType
                                , Arrays.copyOfRange(bytes, fileBytesStartByte
                                , splitPos.get(i)));
                        fileBytesStartByte = -1;
                    }
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
                    readConfig = true;
                }
            }else{
                // 配置读取阶段
                if(line.isEmpty()){
                    readConfig = false;
                    continue;
                }
                // 数据不为空则开始解析数据
                String[] args = line.replace(StrPool.QUOTATION_MARK,StrPool.EMPTY)
                        .replace(StrPool.SEMICOLON,StrPool.EMPTY).split(StrPool.SPACE);
                for(String arg : args) {
                    if(arg.startsWith(StrPool.NAME))
                        parName = arg.split(StrPool.EQUAL)[1];
                    else if(arg.startsWith(StrPool.FILE + StrPool.NAME + StrPool.EQUAL))
                        parFileName = arg.split(StrPool.EQUAL)[1];
                }
            }
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