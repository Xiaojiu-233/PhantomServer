package xj.implement.web;

import com.sun.deploy.util.ArrayUtil;
import org.yaml.snakeyaml.util.ArrayUtils;
import xj.abstracts.web.Response;
import xj.component.conf.ConfigureManager;
import xj.enums.web.CharacterEncoding;
import xj.enums.web.StatuCode;
import xj.interfaces.web.IHttpResponse;
import xj.tool.ConfigPool;
import xj.tool.Constant;
import xj.tool.StrPool;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

// HTTP协议响应对象，用于响应HTTP协议
public class HTTPResponse extends Response implements IHttpResponse {

    // 成员属性
    private StatuCode statuCode;// 状态码

    private CharacterEncoding encoding;// 编码形式

    private String httpVersion = "1.1";// HTTP协议版本

    private Map<String, String> headers = new HashMap<>();// 响应头

    private byte[] bodyBytes;// 响应体二进制版本

    // 成员方法
    // 构造方法
    public HTTPResponse(StatuCode statuCode, CharacterEncoding encoding, byte[] bodyBytes) {
        this.statuCode = statuCode;
        this.encoding = encoding;
        this.bodyBytes = bodyBytes;
    }

    // 设置响应体
    public HTTPResponse setHeaders(String key, String value) {
        if(key.equals(StrPool.CONTENT_TYPE)) {
            value += "; charset=" + encoding.getShowStyle();
        }
        headers.put(key, value);
        return this;
    }

    @Override
    public void setRespHeaders(String key, String value) {
        headers.put(key, value);
    }

    // 设置数据信息
    @Override
    public void setBodyBytes(byte[] bodyBytes) {
        this.bodyBytes = bodyBytes;
    }

    @Override
    public void writeMessage(SocketChannel os) throws IOException {
        // 添加统一的响应
        headers.put(StrPool.SERVER,(String)ConfigureManager.getInstance()
                .getConfig(ConfigPool.SERVER.NAME));
        // 整体响应报文构建
        StringBuilder sb = new StringBuilder();
        // 输出版本信息和状态码
        sb.append(StrPool.HTTP).append(StrPool.SLASH).append(httpVersion).append(StrPool.SPACE)
                .append(statuCode.getCode()).append(lineBreak);
        // 输出响应头
        for(Map.Entry<String, String> header : headers.entrySet()) {
            sb.append(header.getKey()).append(StrPool.COLON).append(StrPool.SPACE)
                    .append(header.getValue()).append(lineBreak);
        }
        // 输出响应体
        sb.append(lineBreak);
        byte[] headBytes = sb.toString().getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(headBytes.length + bodyBytes.length + lineBreak.getBytes().length);
        buffer.put(headBytes);
        buffer.put(bodyBytes);
        buffer.put(lineBreak.getBytes());
        buffer.flip();
        os.write(buffer);
    }

    // 获取响应头参数
    @Override
    public String getHeaderArg(String key) {
        return headers.get(key);
    }
}
