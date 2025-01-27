package xj.enums.web;

// HTTP协议使用的主要ContentType枚举，用于实现Content-Type相关的处理功能
public enum ContentType {

    // 主要的ContentType
    TEXT_HTML("text/html","text",".html"),
    TEXT_PLAIN("text/plain","text",null),
    APPLICATION_JAVASCRIPT("application/javascript","application",".js"),
    APPLICATION_JSON("application/json","application",".json"),
    APPLICATION_XML("application/xml","application",".xml"),
    APPLICATION_PDF("application/pdf","application",".pdf"),
    APPLICATION_X_WWW_FORM("application/x-www-form-urlencoded","application",null),
    APPLICATION_MSWORD("application/msword","application",".docx"),
    IMAGE_GIF("image/gif","image",".gif"),
    IMAGE_JPEG("image/jpeg","image",".jpeg"),
    IMAGE_JPG("image/jpg","image",".jpg"),
    IMAGE_PNG("image/png","image",".png"),
    AUDIO_MPEG("audio/mpeg","audio",".mpeg"),
    AUDIO_MP3("audio/mp3","audio",".mp3"),
    AUDIO_OGG("audio/ogg","audio",".ogg"),
    VIDEO_MP4("video/mp4","video",".mp4"),
    MULTIPART_FORM_DATA("multipart/form-data","multipart",null),
    // 次要的重复ContentType
    TEXT_JAVASCRIPT("text/javascript","text",".js"),
    TEXT_CSS("text/css","text",".css"),
    TEXT_XML("text/xml","text",".xml"),
    VIDEO_MPEG("video/mpeg","video",".mpeg"),
    VIDEO_OGG("video/ogg","video",".ogg");

    // 成员属性
    public final String contentType; // ContentType名称
    public final String category; // ContentType类型
    public final String extName; // 文件拓展名

    // 成员方法
    // 构造方法
    ContentType(String contentType, String category, String extName) {
        this.contentType = contentType;
        this.category = category;
        this.extName = extName;
    }

    // 通过扩展名找到Content-Type，没找到则返回text/plain
    public static String getContentTypeByExtName(String extName) {
        for(ContentType contentType : ContentType.values())
            if(contentType.extName.equals(extName))
                return contentType.contentType;
        return TEXT_PLAIN.contentType;
    }
}