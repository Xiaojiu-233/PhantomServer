package xj.core.extern;

import xj.annotation.ComponentImport;
import xj.component.conf.ConfigureManager;
import xj.component.log.LogManager;
import xj.enums.web.CharacterEncoding;
import xj.enums.web.StatuCode;
import xj.implement.web.HTTPRequest;
import xj.implement.web.HTTPResponse;
import xj.interfaces.component.IConfigureManager;
import xj.interfaces.component.ILogManager;
import xj.tool.StrPool;

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
        // 对路径进行解析，获取扩展名
        String url = req.getUrl();
        String extName = url.substring(url.lastIndexOf(StrPool.POINT) + 1).toLowerCase();
        // 根据扩展名选择使用网页还是其他资源映射
        InputStream in = extName.equals(StrPool.HTML) ?
                JarManager.getInstance().getWebpage(url) : JarManager.getInstance().getResource(url);
        if(in == null){
            // 如果没有找到资源，返回404响应
            // TODO: 准备404网页资源映射地址
            response = new HTTPResponse(StatuCode.NOT_FOUND,CharacterEncoding.UTF_8,null);
            response.setHeaders(StrPool.CONTENT_TYPE, "text/html");
        }else{
            // 寻找到资源则根据扩展名类型确定对应的Content-Type
            // TODO: 在config.yml里写好映射map配置，使用完InputStream记得回收
            response = new HTTPResponse(StatuCode.OK,CharacterEncoding.UTF_8,null);
        }
        // 返回HTTP响应
        return response;
    }

    // 后端API请求的处理
    private HTTPResponse apiHandle(HTTPRequest req){
        // 返回HTTP响应
        return null;
    }

}
