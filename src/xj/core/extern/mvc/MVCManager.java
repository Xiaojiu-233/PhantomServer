package xj.core.extern.mvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import xj.abstracts.web.Response;
import xj.annotation.*;
import xj.component.log.LogManager;
import xj.core.extern.IOCManager;
import xj.core.extern.JarManager;
import xj.core.threadPool.factory.ThreadTaskFactory;
import xj.enums.web.CharacterEncoding;
import xj.enums.web.ContentType;
import xj.enums.web.RequestMethod;
import xj.enums.web.StatuCode;
import xj.implement.web.HTTPRequest;
import xj.implement.web.HTTPResponse;
import xj.interfaces.mvc.ContentTypeHandler;
import xj.interfaces.web.IHttpRequest;
import xj.interfaces.web.IHttpResponse;
import xj.tool.ConfigPool;
import xj.tool.FileIOUtil;
import xj.tool.StrPool;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// MVC管理器，为MVC模块的核心部分，用于为HTTP请求提供资源映射与后端处理的解决方案
public class MVCManager {

    // 成员属性
    private static volatile MVCManager instance;// 单例模式实现

    private final Map<String, HandlerMapper> handlerMappers = new HashMap<>();// 请求拦截处理器，用于将指定url的请求交给特定方法处理

    // 成员方法
    // 初始化
    public MVCManager() {
        LogManager.info_("【MVC模块】开始初始化");
        // 初始化其他主要组件
        ContentTypeConverter.getInstance();
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
        // 读取所有控制器注解类
        List<Object> annotationObjects = IOCManager.getInstance()
                .returnInstancesByAnnotation(PController.class);
        // 遍历处理
        for(Object annotationObject : annotationObjects){
            // 如果类存在RequestMapping，通过RequestMapping获取基础路径
            String baseUrl = "";
            if(annotationObject.getClass().isAnnotationPresent(PRequestMapping.class)){
                baseUrl = annotationObject.getClass().getAnnotation(PRequestMapping.class).value();
            }
            // 获取类的所有方法，将被RequestMapping注释的方法装入到容器中
            Method[] methods = annotationObject.getClass().getDeclaredMethods();
            for(Method method : methods){
                if(method.isAnnotationPresent(PRequestMapping.class)){
                    // 获取路径作处理器的key，如果存在多个键，则直接报错
                    PRequestMapping mapping = method.getAnnotation(PRequestMapping.class);
                    String urlKey = baseUrl + mapping.value();
                    if(handlerMappers.containsKey(urlKey)){
                        LogManager.error_("MVC映射处理器读取映射方法时出现异常:" +
                                "路径[{}]存在映射多个方法的情况，方法[{}]将被舍弃！",urlKey,method.getName());
                        continue;
                    }
                    handlerMappers.put(urlKey, new HandlerMapper(mapping.method(),method));
                }
            }
        }
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
        if(!req.getMethod().equals(RequestMethod.GET)){
            // 如果不是，则返回405响应
            return (HTTPResponse) getHttpRespByStatuCode(StatuCode.METHOD_NOT_ALLOWED);
        }
        // 对路径进行解析，获取扩展名
        String url = req.getUrl();
        String extName = url.substring(url.lastIndexOf(StrPool.POINT)).toLowerCase();
        // 使用静态资源映射
        InputStream in = JarManager.getInstance().getResource(url);
        if(in == null){
            // 如果没有找到资源，返回404响应
            response = (HTTPResponse) getHttpRespByStatuCode(StatuCode.NOT_FOUND);
        }else{
            // 寻找到资源则根据扩展名类型确定对应的Content-Type
            response = new HTTPResponse(StatuCode.OK,CharacterEncoding.UTF_8,null);
            try {
                response.storeData(FileIOUtil.getByteByInputStream(in));
            } catch (IOException e) {
                LogManager.error_("MVC模块读取资源时出现异常",e);
            }
            response.setHeaders(StrPool.CONTENT_TYPE,ContentType.getContentTypeByExtName(extName));
        }
        // 返回HTTP响应
        return response;
    }

    // 后端API请求的处理
    private HTTPResponse apiHandle(HTTPRequest req){
        // 初始化响应对象
        HTTPResponse resp = null;
        // 对路径进行解析，获取扩展名
        String url = req.getUrl();
        // 通过HandlerMapping将url映射到对应处理方法
        HandlerMapper m = handlerMappers.get(url);
        // 判定方法是否存在
        if(m == null){
            // 如果没有找到资源，返回404响应
            resp = (HTTPResponse) getHttpRespByStatuCode(StatuCode.NOT_FOUND);
        }else if(!m.getMethod().equals(req.getMethod())){
            // 判定方法是否符合请求对象的请求类型，不符合则返回405响应
            resp = (HTTPResponse) getHttpRespByStatuCode(StatuCode.METHOD_NOT_ALLOWED);
        } else{
            // 根据请求头的ContentType，处理请求对象数据封装为参数Map
            String contentTypeData = req.getHeaders().get(StrPool.CONTENT_TYPE);
            ContentType contentType = null;
            Map<String, String> contentTypeArgs = new HashMap<>();
            if(contentTypeData != null){
                String[] contentTypeDatas = contentTypeData.split(StrPool.SEMICOLON);
                contentType = ContentType.getContentTypeByString(contentTypeDatas[0]);
                for(int i = 1; i < contentTypeDatas.length; i++){
                    String[] arg = contentTypeDatas[i].trim().split(StrPool.EQUAL);
                    contentTypeArgs.put(arg[0], arg[1]);
                }
            }
            Map<String,Object> requestBody = ContentTypeConverter.getInstance()
                    .handleData(contentType,contentTypeArgs,req.getBodyBytes());
            // 设置响应体初始数据
            resp = new HTTPResponse(StatuCode.OK,CharacterEncoding.UTF_8,null);
            // 将获取的各种参数通过注解来注入到方法中，处理方法得到返回结果
            Method handleMethod = m.getHandleMethod();
            handleMethod.setAccessible(true);
            Parameter[] params = handleMethod.getParameters();
            Object[] pars = new Object[params.length];
            for(int i=0;i<params.length;i++){
                Object par = null;
                if(params[i].isAnnotationPresent(PRequestBody.class)){
                    par = requestBody;
                }else if(params[i].isAnnotationPresent(PRequestParam.class)){
                    par = req.getUrlParams();
                }else if(params[i].isAnnotationPresent(PUploadFile.class)){
                    par = requestBody.get(StrPool.FILE);
                }else if(params[i].getType().equals(IHttpRequest.class)){
                    par = req;
                }else if(params[i].getType().equals(IHttpResponse.class)){
                    par = resp;
                }
                pars[i] = par;
            }
            // 执行方法
            String clazzName = handleMethod.getDeclaringClass().getName();
            clazzName = clazzName.substring(clazzName.lastIndexOf(StrPool.POINT)+1);
            Object clazzObj = IOCManager.getInstance().returnInstanceByName(clazzName);
            // 根据得到的结果数据情况，封装为byte数组
            byte[] data = null;
            try {
                Object ret = handleMethod.invoke(clazzObj,pars);
                data = new ObjectMapper().writeValueAsBytes(ret);
            } catch (IllegalAccessException | InvocationTargetException | IOException e) {
                LogManager.error_("MVC模块在调用HTTP请求映射方法的时候出现错误",e);
            }
            // 如果没有指定ContentType，则使用json
            String respContentType = resp.getHeaderArg(StrPool.CONTENT_TYPE);
            if(respContentType == null){
                resp.storeData(data);
                resp.setHeaders(StrPool.CONTENT_TYPE,ContentType.APPLICATION_JSON.contentType);
            }
        }
        // 返回HTTP响应
        return resp;
    }

    // 根据响应码返回对应的响应体
    public static Response getHttpRespByStatuCode(StatuCode status){
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
        // 准备响应体并返回
        HTTPResponse resp = new HTTPResponse(status,CharacterEncoding.UTF_8, bytes);
        resp.setHeaders(StrPool.CONTENT_TYPE, ContentType.TEXT_HTML.contentType);
        return resp;
    }

    // 返回MVC请求拦截处理器信息
    public List<Map<String,Object>> returnHandlerMappingInfo(){
        List<Map<String,Object>> ret = new ArrayList<>();
        for(Map.Entry<String,HandlerMapper> entry : handlerMappers.entrySet()){
            Map<String,Object> map = new HashMap<>();
            map.put("处理路径",entry.getKey());
            map.put("处理器请求方法",entry.getValue().getMethod());
            ret.add(map);
        }
        return ret;
    }

    // 返回MVC的ContentType转移器信息
    public List<Map<String,Object>> returnContentTypeConverterInfo(){
        List<Map<String,Object>> ret = new ArrayList<>();
        for(ContentTypeHandler handler : ContentTypeConverter.getInstance().getHandlerList()){
            Map<String,Object> map = new HashMap<>();
            map.put("转移器名称",handler.getClass().getSimpleName());
            map.put("转移器全限名",handler.getClass().getName());
            ret.add(map);
        }
        return ret;
    }

}
