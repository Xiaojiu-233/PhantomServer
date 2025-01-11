package xj.tool;

import xj.abstracts.web.Request;
import xj.implement.web.ProtocolRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

// 协议相关的工具程序包，提供将数据转化为请求等功能
public class ProtocolUtils {

    // 将socket输入流数据转化为协议请求对象，如果不存在输入数据则返回null
    public static Request getProtocolRequest(InputStream in) throws IOException {
        byte[] buffer = new byte[Constant.BYTES_UNIT_CAPACITY];
        int bytesRead = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while ((bytesRead = in.read(buffer)) != -1) {
            bos.write(buffer, 0, bytesRead);
        }
        Request req = new ProtocolRequest(bos.toByteArray());
        bos.close();
        return req;
    }
}
