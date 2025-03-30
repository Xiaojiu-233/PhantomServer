package xj.tool;

import jdk.internal.util.xml.impl.Input;
import xj.component.conf.ConfigureManager;
import xj.component.log.LogManager;
import xj.implement.web.ProtocolRequest;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
        } catch (Exception e) {
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
            if(bytesRead > 0) {
                bos.write(buffer, 0, bytesRead);
            }
        }
        return bos.toByteArray();
    }

    // 从InputStream中获取数据存入文件系统里
    public static void writeFileByInputStream(String targetPath, InputStream source) {
        try {
            // 生成路径文件夹
            if(targetPath.lastIndexOf(File.separator) != -1){
                String dirPath = targetPath.substring(0, targetPath.lastIndexOf(File.separator));
                File dir = new File(dirPath);
                if(!dir.exists()){
                    if(!dir.mkdirs()){
                        LogManager.error_("执行数据存入文件系统任务时，无法创建文件夹: {}",dirPath);
                        return;
                    }
                }
            }
            // 打开文件
            File file = new File(targetPath);
            if (!file.exists())
                if(!file.createNewFile()){
                    LogManager.error_("执行数据存入文件系统任务时，无法创建文件: {}",targetPath);
                    return;
                }
            // 数据准备
            byte[] buffer = new byte[Constant.BYTES_UNIT_CAPACITY];// 开始读取数据
            OutputStream target = Files.newOutputStream(file.toPath());
            // 1.读取数据并存入文件中
            int getByte = 0;
            while ((getByte = source.read(buffer))!= -1) {
                // 数据
                target.write(buffer,0,getByte);
            }
            // 3.读取结束
            source.close();
            target.close();
        } catch (IOException e) {
            LogManager.error_("执行数据存入文件系统任务时，出现异常：{}",e);
        }
    }

    // 根据换行符分割字符数组(返回值中，奇数为数据段开头，偶数为数据段结尾)
    public static List<Integer> splitByteArrayByLineBreak(byte[] data,boolean emptySign){
        String lineBreak = (String) ConfigureManager.getInstance().getConfig(ConfigPool.SYSTEM_ARG.LINE_BREAK);
        byte[] lineBreakBytes = lineBreak.getBytes();
        int lineBreakLen = lineBreakBytes.length;
        List<Integer> splitPos = new ArrayList<>();
        int start = 0;
        int correctScan = -1;// 扫描换行符计数器
        for(int i = 0;i < data.length;i++){
            if(correctScan > -1){
                if(data[i] != lineBreakBytes[correctScan+1]){
                    correctScan = -1;
                }else{
                    correctScan++;
                    if(correctScan == lineBreakLen - 1){
                        int end = i - lineBreakLen;
                        if(emptySign && start >= end){
                            splitPos.add(-1);
                            splitPos.add(-1);
                        }else if(start < end){
                            splitPos.add(start);
                            splitPos.add(end);
                        }
                        start = i + 1;
                        correctScan = -1;
                    }
                }
            }else if(data[i] == lineBreakBytes[0]){
                correctScan = 0;
            }
        }
        return splitPos;
    }
}
