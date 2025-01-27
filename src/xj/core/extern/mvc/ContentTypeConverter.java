package xj.core.extern.mvc;

import xj.abstracts.connect.ConnectHandler;
import xj.abstracts.web.Request;
import xj.component.log.LogManager;
import xj.core.threadPool.factory.ConnectHandlerFactory;
import xj.enums.web.ContentType;
import xj.implement.connect.HTTPConnectHandler;
import xj.implement.connect.TCPLongConnectHandler;
import xj.implement.connect.TCPShortConnectHandler;
import xj.implement.mvc.FileContentTypeHandler;
import xj.implement.mvc.JsonContentTypeHandler;
import xj.implement.mvc.TableContentTypeHandler;
import xj.interfaces.mvc.ContentTypeHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// MVC框架使用的ContentType数据转换器，通过请求指定的ContentType将字节数据转为指定数据
public class ContentTypeConverter {

    // 成员属性
    private static volatile ContentTypeConverter instance;// 单例模式实现

    private final List<ContentTypeHandler> handlerList = new ArrayList<>();//ContentType处理器

    // 成员方法
    // 获取单例（防止高并发导致资源访问问题进行双判空保护）
    public static ContentTypeConverter getInstance() {
        if(instance == null)
            synchronized (ContentTypeConverter.class){
                if(instance == null)
                    instance = new ContentTypeConverter();
            }
        return instance;
    }

    // 初始化
    public ContentTypeConverter(){
        LogManager.info_("ContentType数据转换器正在构建...");
        // 导入默认的ContentType处理器
        handlerList.add(new JsonContentTypeHandler());
        handlerList.add(new TableContentTypeHandler());
        handlerList.add(new FileContentTypeHandler());

    }

    // 根据提供的请求数据与ContentType，处理返回对应的Map数据
    public Map<String,Object> handleData(ContentType contentType, byte[] data){
        synchronized(ConnectHandlerFactory.class){
            // 遍历列表，寻找合适的处理器
            for(ContentTypeHandler handler : handlerList){
                // 判定是否匹配，匹配成功则复制并返回指定对象
                if(handler.isMatchContentType(contentType)){
                    return handler.handle(data);
                }
            }
            // 如果没有合适的处理器对象，将抛出错误并返回null
            LogManager.error_("没有找到合适的ContentType处理器对象",contentType);
            return null;
        }
    }
}
