package xj.implement.web;

import xj.abstracts.web.Request;
import xj.component.conf.ConfigureManager;
import xj.component.log.LogManager;
import xj.enums.web.RequestMethod;
import xj.tool.ConfigPool;
import xj.tool.StrPool;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

// HTTP协议请求对象，用于处理HTTP协议
public class HTTPRequest extends Request {

    // 成员属性
    private RequestMethod method;// 请求方法

    private String url;// 请求路径

    private String httpVersion;// HTTP协议版本

    private Map<String, String> urlParams = new HashMap<>();//请求路径参数

    private Map<String, String> headers = new HashMap<>();// 请求头

    private byte[] bodyBytes = new byte[0];// 请求体二进制版本

    // 成员方法
    // 构造方法
    public HTTPRequest(Request request) {
        super(request.getData());
        selfAnalysis();
    }

    // 自解析
    private void selfAnalysis(){
        // 拆解数据
        String[] lines = encodeToString();
        int rowRead = 0;
        int byteRead = 0;
        // 确定请求方法，请求路径url与路径参数，版本
        String[] headArgs = lines[0].split(StrPool.SPACE );
        method = RequestMethod.valueOf(headArgs[0]);
        String[] urls = headArgs[1].contains(StrPool.QUESTION_MARK) ?
                headArgs[1].split(StrPool.QUESTION_MARK) : new String[]{headArgs[1]};
        url = urls[0];
        // url特判，如果只是 / 且为GET请求的话，改为主页位置
        if(url.equals(StrPool.SLASH) && method.equals(RequestMethod.GET)) url =
                (String) ConfigureManager.getInstance().getConfig(ConfigPool.MVC.INDEX_PATH);
        // 如果url有参数的话，读取路径参数
        if(urls.length > 1){
            String[] params = urls[1].split(StrPool.AND);
            for(String param : params){
                String[] keyValue = param.split(StrPool.EQUAL);
                if(keyValue.length != 2){
                    LogManager.error_("处理HTTP请求的路径参数时出现异常参数",param);
                    continue;
                }
                urlParams.put(keyValue[0], keyValue[1]);
            }
        }
        httpVersion = headArgs[2].split(StrPool.SLASH)[1];
        byteRead += lines[rowRead++].getBytes().length;
        // 确定请求头
        while(rowRead < lines.length && !lines[rowRead].isEmpty()){
            String[] args = lines[rowRead].split(StrPool.COLON + StrPool.SPACE);
            headers.put(args[0], args[1]);
            byteRead += lines[rowRead++].getBytes().length;
        }
        // 如果是POST请求方法，则根据上述数据划分请求体，得到二进制数据
        if(method.equals(RequestMethod.POST)){
            byteRead += ++rowRead * 2;
            bodyBytes = Arrays.copyOfRange(data,byteRead,data.length);
        }
    }

    public byte[] getBodyBytes() {
        return bodyBytes;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, String> getUrlParams() {
        return urlParams;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public String getUrl() {
        return url;
    }

    public RequestMethod getMethod() {
        return method;
    }
}
