package xj.core.extern;

import xj.annotation.ComponentImport;
import xj.component.conf.ConfigureManager;
import xj.component.log.LogManager;
import xj.interfaces.component.IConfigureManager;
import xj.interfaces.component.ILogManager;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

// IOC管理器，为IOC模块的核心部分，用于对拓展程序进行反射注入
public class IOCManager {

    // 成员属性
    private static volatile IOCManager instance;// 单例模式实现

    private Map<String,Object> iocContainer = new HashMap<>();// IOC容器

    // 成员方法
    // 初始化
    public IOCManager() {
        LogManager.info_("【IOC模块】开始初始化");
        // 对jar包读取到的类对象进行实例化
        doInstanceFromJars();
        // 对IOC容器的实例进行依赖注入
        autowiredInjection();
        LogManager.info_("【IOC模块】初始化完成");
    }

    // 获取单例（防止高并发导致资源访问问题进行双判空保护）
    public static IOCManager getInstance() {
        if(instance == null)
            synchronized (IOCManager.class){
                if(instance == null)
                    instance = new IOCManager();
            }
        return instance;
    }

    // 将jar包程序通过反射获取实例，导入到IOC容器
    private void doInstanceFromJars(){
        LogManager.info_("【Jar包模块】正在实例化包程序并导入至容器...");
        // 读取类对象列表
        List<Class<?>> classObjects = JarManager.getInstance().getClassObjects();
        // 遍历类对象，反射获取实例，存于IOC容器中
        try {
            for(Class<?> classObject : classObjects){
                iocContainer.put(classObject.getName(),classObject.newInstance());
            }
        } catch (InstantiationException | IllegalAccessException e) {
            LogManager.error_("实例化类对象注入IOC容器时出现异常",e);
        }
    }

    // 依赖注入
    private void autowiredInjection(){
        LogManager.info_("【IOC模块】正在对容器实例进行依赖注入...");
        for(Map.Entry<String,Object> entry : iocContainer.entrySet()){
            Object bean = entry.getValue();
            Class<?> clazz = bean.getClass();
            try {
                // 对于系统组件的依赖注入
                if(clazz.isAnnotationPresent(ComponentImport.class)){
                    Field[] fields = clazz.getFields();
                    for(Field field : fields){
                        field.setAccessible(true);
                        if(field.getType().equals(ILogManager.class))
                            field.set(bean,LogManager.getInstance());
                        else if(field.getType().equals(IConfigureManager.class))
                            field.set(bean, ConfigureManager.getInstance());
                    }
                }
                // 对于其他自定义对象的依赖注入
            } catch (Exception e) {
                LogManager.error_("对IOC容器内实例依赖注入时出现异常",e);
            }
        }
    }
}
