package xj.core.extern;

import xj.annotation.ComponentImport;
import xj.component.conf.ConfigureManager;
import xj.component.log.LogManager;
import xj.enums.web.CharacterEncoding;
import xj.enums.web.RequestMethod;
import xj.enums.web.StatuCode;
import xj.implement.web.HTTPRequest;
import xj.implement.web.HTTPResponse;
import xj.interfaces.component.IConfigureManager;
import xj.interfaces.component.ILogManager;
import xj.tool.ConfigPool;
import xj.tool.FileIOUtil;
import xj.tool.StrPool;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// MVC管理器，为MVC模块的核心部分，用于为HTTP请求提供资源映射与后端处理的解决方案
public class MVCManager {

    // 成员属性
    private static volatile MVCManager instance;// 单例模式实现

    private Map<String, Method> handlerMapping = new HashMap<>();// 请求拦截处理器，用于将指定url的请求交给特定方法处理

    // 成员方法
    // 初始化
    public MVCManager() {
        LogManager.info_("【MVC模块】开始初始化");
        // 实现映射处理器
        setHandlerMapping();
        LogManager.info_("【MVC模块】初始化完成");
    }

    // 获取单例（防止高并发导致资源访问问题进行双判空保护）
    public static MVCManager getInstance() {
        if(instance == null)
            synchronized (MVCManager.class){
                if(instance == null)
                    instance = new MVCManager();
            }
        return instance;
    }

    // 通过IOC容器的实例制定映射处理
    private void setHandlerMapping(){
        LogManager.info_("【MVC模块】正在读取IOC容器实例以实现映射处理...");
    }

    // 通过传入的HTTP请求进行处理，返回HTTP响应
    public HTTPResponse handle(HTTPRequest req) {
        // 对路径进行解析
        String url = req.getUrl();
        // 通过是否存在扩展名判定是否为资源映射请求
        if(url.contains(StrPool.POINT))
            return resourceHandle(req);
        return apiHandle(req);
    }

    // 静态资源映射请求的处理
    private HTTPResponse resourceHandle(HTTPRequest req){
        // 初始化响应对象
        HTTPResponse response = null;
        // 判定是否为GET请求
        if(req.getMethod().equals(RequestMethod.GET)){
            // 如果不是，则返回405响应
            response = new HTTPResponse(StatuCode.METHOD_NOT_ALLOWED,CharacterEncoding.UTF_8,
                    getWebpageByStatuCode(StatuCode.METHOD_NOT_ALLOWED));
            response.setHeaders(StrPool.CONTENT_TYPE,ContentType.TEXT_HTML.contentType);
        }
        // 对路径进行解析，获取扩展名
        String url = req.getUrl();
        String extName = url.substring(url.lastIndexOf(StrPool.POINT)).toLowerCase();
        // 根据扩展名选择使用网页还是其他资源映射
        InputStream in = extName.equals(StrPool.HTML_POINT) ?
                JarManager.getInstance().getWebpage(url) : JarManager.getInstance().getResource(url);
        if(in == null){
            // 如果没有找到资源，返回404响应
            response = new HTTPResponse(StatuCode.NOT_FOUND,CharacterEncoding.UTF_8,
                    getWebpageByStatuCode(StatuCode.NOT_FOUND));
            response.setHeaders(StrPool.CONTENT_TYPE,ContentType.TEXT_HTML.contentType);
        }else{
            // 寻找到资源则根据扩展名类型确定对应的Content-Type
            byte[] data = null;
            try {
                data = FileIOUtil.getByteByInputStream(in);
            } catch (IOException e) {
                LogManager.error_("读取文件资源时出现异常", e);
            }
            response = new HTTPResponse(StatuCode.OK,CharacterEncoding.UTF_8,data);
            response.setHeaders(StrPool.CONTENT_TYPE,ContentType.getContentTypeByExtName(extName));
        }
        // 返回HTTP响应
        return response;
    }

    // 后端API请求的处理
    private HTTPResponse apiHandle(HTTPRequest req){
        // 初始化响应对象
        HTTPResponse response = null;
        // 对路径进行解析，获取扩展名
        String url = req.getUrl();
        // 通过HandlerMapping将url映射到对应处理方法
        Method m = handlerMapping.get(url);
        // 判定方法是否存在
        if(m == null){
            // 如果没有找到资源，返回404响应
            response = new HTTPResponse(StatuCode.NOT_FOUND,CharacterEncoding.UTF_8,
                    getWebpageByStatuCode(StatuCode.NOT_FOUND));
            response.setHeaders(StrPool.CONTENT_TYPE,ContentType.TEXT_HTML.contentType);
        }else{
            // 判定方法是否符合请求对象的请求类型
            // 根据请求头的ContentType，处理请求对象数据封装为参数Map
            // 将获取的各种参数通过注解来注入到方法中，处理方法得到返回结果
            // 根据得到的结果数据情况，封装为byte数组
            byte[] data = null;
            // 数据装入到响应对象中
            response = new HTTPResponse(StatuCode.OK,CharacterEncoding.UTF_8,null);
            response.setHeaders(StrPool.CONTENT_TYPE,ContentType
                    .getContentTypeByExtName(response.getHeaderArg(StrPool.CONTENT_TYPE)));
        }
        // 返回HTTP响应
        return response;
    }

    // 根据响应码返回对应的服务器网页，如果没找到则使用unknown
    private byte[] getWebpageByStatuCode(StatuCode status){
        byte[] bytes = null;
        // 确定要寻找的网页的路径
        String webpagePath = ConfigPool.SYSTEM_PATH.SYSTEM_WEBPAGE_PATH + status.getCode()
                + StrPool.HTML_POINT;
        // 在服务器资源目录中搜索
        bytes = FileIOUtil.getFileContent(webpagePath);
        // 如果没找到则搜索unknown网页并返回
        if(bytes == null)
            bytes = FileIOUtil.getFileContent(ConfigPool.SYSTEM_PATH.SYSTEM_WEBPAGE_PATH
                    + ConfigPool.SYSTEM_PATH.UNKNOWN_WEBPAGE + StrPool.HTML_POINT);
        // 返回对应数据
        return bytes;
    }

    // HTTP协议使用的主要ContentType枚举，用于实现Content-Type相关的处理功能
    public enum ContentType {

        // 主要的ContentType
        TEXT_HTML("text/html","text",".html"),
        TEXT_PLAIN("text/plain","text",null),
        APPLICATION_JAVASCRIPT("application/javascript","application",".js"),
        APPLICATION_JSON("application/json","application",".json"),
        APPLICATION_XML("application/xml","application",".xml"),
        APPLICATION_PDF("application/pdf","application",".pdf"),
        APPLICATION_MSWORD("application/msword","application",".docx"),
        IMAGE_GIF("image/gif","image",".gif"),
        IMAGE_JPEG("image/jpeg","image",".jpeg"),
        IMAGE_JPG("image/jpg","image",".jpg"),
        IMAGE_PNG("image/png","image",".png"),
        AUDIO_MPEG("audio/mpeg","audio",".mpeg"),
        AUDIO_MP3("audio/mp3","audio",".mp3"),
        AUDIO_OGG("audio/ogg","audio",".ogg"),
        VIDEO_MP4("video/mp4","video",".mp4"),
        // 次要的重复ContentType
        TEXT_JAVASCRIPT("text/javascript","text",".js"),
        TEXT_CSS("text/css","text",".css"),
        TEXT_XML("text/xml","text",".xml"),
        VIDEO_MPEG("video/mpeg","video",".mpeg"),
        VIDEO_OGG("video/ogg","video",".ogg");

        // 成员属性
        final String contentType; // ContentType名称
        final String category; // ContentType类型
        final String extName; // 文件拓展名

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

}
