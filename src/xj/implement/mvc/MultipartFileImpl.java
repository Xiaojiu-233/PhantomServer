package xj.implement.mvc;

import xj.interfaces.mvc.MultipartFile;

// 通过multipart表单数据上传的文件对象实现
public class MultipartFileImpl implements MultipartFile {

    // 成员属性
    private String name;

    private String fileName;

    private String contentType;

    private byte[] dataBytes;

    // 成员方法
    // 构造方法
    public MultipartFileImpl(String name, String fileName, String contentType, byte[] dataBytes) {
        this.name = name;
        this.fileName = fileName;
        this.contentType = contentType;
        this.dataBytes = dataBytes;
    }

    // 获取数据
    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public byte[] getDataBytes() {
        return dataBytes;
    }
}
