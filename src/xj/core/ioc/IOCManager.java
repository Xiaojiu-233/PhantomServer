package xj.core.ioc;

import sun.rmi.runtime.Log;
import xj.component.conf.ConfigureManager;
import xj.component.log.LogManager;
import xj.tool.ConfigPool;
import xj.tool.JarUtils;
import xj.tool.StrPool;

import java.io.File;
import java.io.IOException;
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

    private Map<String,Object> iocContainer;// IOC容器

    private List<String> jarPaths;// jar包路径列表

    private String extProgramPath;// 拓展程序jar包扫描路径

    private String webProgramPath;// Web程序jar包扫描路径

    private String resourcePath;// Web包内资源映射路径

    private String webpagePath;// Web包内网页映射路径

    // 成员方法
    // 初始化
    public IOCManager() {
        LogManager.info_("【IOC模块】开始初始化");
        // 初始化IOC容器
        iocContainer = new HashMap<>();
        // 读取相关配置
        extProgramPath = (String) ConfigureManager.getInstance().getConfig(ConfigPool.IOC.EXT_PROGRAM_PATH);
        webProgramPath = (String) ConfigureManager.getInstance().getConfig(ConfigPool.MVC.WEB_PROGRAM_PATH);
        resourcePath = (String) ConfigureManager.getInstance().getConfig(ConfigPool.MVC.RESOURCE_PATH);
        webpagePath = (String) ConfigureManager.getInstance().getConfig(ConfigPool.MVC.WEBPAGE_PATH);
        LogManager.info_("IOC容器参数 -> 拓展程序jar包扫描路径：{} Web程序jar包扫描路径：{} Web包内资源映射路径：{}" +
                " Web包内网页映射路径：{}",extProgramPath,webProgramPath,resourcePath,webpagePath);
        // 递归扫描程序包获取实例
        LogManager.info_("【IOC模块】正在扫描拓展程序包...");
        scanPackageFromPath(extProgramPath);
        LogManager.info_("【IOC模块】正在扫描Web程序包...");
        scanPackageFromPath(webProgramPath);
        // 将jar包程序实例化导入到IOC容器
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

    // 递归扫描指定路径程序包
    // TODO:拓展程序包启动也需要递归扫描jar包，看看需不需要把该服务调到别的管理器比如JarManager
    private void scanPackageFromPath(String path){
        if(path == null || path.isEmpty())
            return;
        // 读取文件对象
        File root = new File(path);
        if(root.isDirectory()){
            // 如果对象是目录，则递归查找
            for(File f : Objects.requireNonNull(root.listFiles())){
                scanPackageFromPath(path + StrPool.SLASH + f.getName());
            }
        }else{
            // 如果对象是文件，则判定是否为jar包文件
            String fileName = root.getName();
            String extName = fileName.substring(fileName.lastIndexOf("."));
            if(extName.equals(StrPool.JAR)){
                jarPaths.add(path + StrPool.SLASH + fileName);
            }
        }
    }

    // 将jar包程序通过反射获取实例，导入到IOC容器
    // TODO:把这个烂摊子收拾一下
    private void doInstanceFromJars(){
        LogManager.info_("【IOC模块】正在实例化包程序并导入至容器...");
        for(String jarPath : jarPaths){
            File file = new File(jarPath);
            URL url1 = null;
            try{
                url1 = file.toURI().toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
            URLClassLoader jarUrlClassLoader = new URLClassLoader(new URL[] { url1 },
                    Thread.currentThread().getContextClassLoader());
            try (JarFile jarFile = new JarFile(file)){
                // 开始获取jar中的.class文件
                Enumeration<JarEntry> entries = jarFile.entries();
                List<String> classNames = JarUtils.getClassNames(entries);
                classNames.forEach(x -> {
                    try {
                        ClassLoader classLoader = jarUrlClassLoader;
                        // 需要使用其他的classLoader加载
                        Class<?> c = classLoader.loadClass(x);
                        Method m = c.getMethod("Service");
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                });

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    // 依赖注入
    private void autowiredInjection(){
        LogManager.info_("【IOC模块】正在对容器实例进行依赖注入...");

    }
}
