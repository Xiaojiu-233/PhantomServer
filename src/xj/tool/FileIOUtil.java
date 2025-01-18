package xj.tool;

import jdk.internal.util.xml.impl.Input;
import xj.component.log.LogManager;
import xj.implement.web.ProtocolRequest;

import java.io.*;
import java.nio.file.Files;

// 文件资源IO工具包，用于对服务器的文件资源、IO需求进行处理
public class FileIOUtil {

    // 读取指定路径文件
    public static byte[] getFileContent(String filePath) {
        // 获得文件对象
        File file = new File(filePath);
        // 如果文件对象不存在或者为目录，则返回null
        if(!file.exists() || file.isDirectory()) return null;
        // 开始读取文件内容
        try (InputStream in = Files.newInputStream(file.toPath())){
            return getByteByInputStream(in);
        } catch (IOException e) {
            LogManager.error_("读取文件资源 [{}] 时出现异常: {}", filePath, e);
        }
        return null;
    }

    // 从InputStream中获取byte数组
    public static byte[] getByteByInputStream(InputStream in) throws IOException {
        byte[] buffer = new byte[Constant.BYTES_UNIT_CAPACITY];
        int bytesRead = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while (in.available() > 0) {
            bytesRead = in.read(buffer);
            bos.write(buffer, 0, bytesRead);
        }
        return bos.toByteArray();
    }
}
