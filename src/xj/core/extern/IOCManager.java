package xj.core.extern;

import xj.annotation.ComponentImport;
import xj.annotation.PAutowired;
import xj.component.conf.ConfigureManager;
import xj.component.log.LogManager;
import xj.core.threadPool.ThreadPoolManager;
import xj.core.threadPool.factory.ThreadTaskFactory;
import xj.interfaces.component.IConfigureManager;
import xj.interfaces.component.ILogManager;
import xj.interfaces.component.IThreadPoolManager;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

// IOC管理器，为IOC模块的核心部分，用于对拓展程序进行反射注入
public class IOCManager {

    // 成员属性
    private static volatile IOCManager instance;// 单例模式实现

    private Map<String,Object> iocContainer = new HashMap<>();// IOC容器

    private Map<Class<?>,Object> instanceBuffer = new HashMap<>();// IOC实例缓存（通过类对象映射）

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
            for(Class<?> classObject : classObjects)
                iocContainer.put(classObject.getSimpleName(),classObject.newInstance());
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
                Field[] fields = clazz.getDeclaredFields();
                for(Field field : fields){
                    field.setAccessible(true);
                    // 对于系统组件的依赖注入
                    if(clazz.isAnnotationPresent(ComponentImport.class)){
                        if(field.getType().equals(ILogManager.class))
                            field.set(bean,LogManager.getInstance());
                        else if(field.getType().equals(IConfigureManager.class))
                            field.set(bean, ConfigureManager.getInstance());
                        else if(field.getType().equals(IThreadPoolManager.class))
                            field.set(bean, ThreadPoolManager.getInstance());
                    }
                    // 对于其他自定义对象的依赖注入
                    if(field.isAnnotationPresent(PAutowired.class)){
                        field.set(bean,returnImplInstanceByClass(field.getType()));
                    }
                }
            } catch (Exception e) {
                LogManager.error_("对IOC容器内实例依赖注入时出现异常",e);
            }
        }
    }

    // 返回指定实现类对象的IOC实例
    public List<Object> returnImplInstancesByClass(Class<?> clazz){
        List<Object> ret = new ArrayList<>();
        for(Map.Entry<String,Object> entry : iocContainer.entrySet()){
            Object bean = entry.getValue();
            if(clazz.isInstance(bean)){
                ret.add(bean);
            }
        }
        return ret;
    }

    // 返回指定单个 由接口类对象实现/指定类对象 的IOC实例
    public Object returnImplInstanceByClass(Class<?> clazz){
        // 读取缓存
        Object ret = instanceBuffer.get(clazz);
        // 缓存没有则查看IOC容器
        boolean notNormalClass = clazz.isInterface() || clazz.getModifiers() == Modifier.ABSTRACT;
        if(ret == null){
            List<Object> retList = new ArrayList<>();
            for(Map.Entry<String,Object> entry : iocContainer.entrySet()){
                Object bean = entry.getValue();
                if(notNormalClass ? clazz.isInstance(bean) : bean.getClass().equals(clazz))
                    retList.add(bean);
            }
            if(retList.size() > 1){
                LogManager.error_("通过类对象[{}]读取单个IOC实例时获得了[{}]个实例，无法确定该使用哪一个实例"
                        ,clazz.getName(),retList.size());
                return null;
            }else{
                ret = retList.get(0);
                instanceBuffer.put(clazz,ret);
            }
        }
        return ret;
    }

    // 返回拥有指定注解的IOC实例
    public List<Object> returnInstancesByAnnotation(Class<? extends Annotation> annotation){
        List<Object> ret = new ArrayList<>();
        for(Map.Entry<String,Object> entry : iocContainer.entrySet()){
            Object bean = entry.getValue();
            if(bean.getClass().isAnnotationPresent(annotation)){
                ret.add(bean);
            }
        }
        return ret;
    }

    // 返回指定名称的IOC实例
    public Object returnInstanceByName(String name){
        return iocContainer.get(name);
    }

    // 返回IOC容器实例信息
    public List<Map<String,Object>> returnIOCInstancesInfo(){
        List<Map<String,Object>> ret = new ArrayList<>();
        for(Map.Entry<String,Object> entry : iocContainer.entrySet()){
            Map<String,Object> map = new HashMap<>();
            map.put("实例名称",entry.getKey());
            map.put("实例类全限名",entry.getValue().getClass().getName());
            ret.add(map);
        }
        return ret;
    }
}
