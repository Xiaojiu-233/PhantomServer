package xj.core.extern;

import xj.component.conf.ConfigureManager;
import xj.component.log.LogManager;
import xj.tool.ConfigPool;
import xj.tool.StrPool;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

// Jar包管理器，用于对拓展程序包进行程序读取与资源管理
public class JarManager {

    // 成员属性
    private static volatile JarManager instance;// 单例模式实现

    private List<String> jarPaths = new ArrayList<>();// jar包路径列表

    private URLClassLoader jarUrlClassLoader;// jar包资源类加载器，用于获取类等相关资源

    private String resourcePath;// Web包内资源映射路径

    private String webpagePath;// Web包内网页映射路径

    // 成员方法
    // 初始化
    public JarManager() {
        LogManager.info_("【Jar包模块】开始初始化");
        // 读取相关配置
        String extProgramPath = (String) ConfigureManager.getInstance().getConfig(ConfigPool.IOC.EXT_PROGRAM_PATH);
        String webProgramPath = (String) ConfigureManager.getInstance().getConfig(ConfigPool.MVC.WEB_PROGRAM_PATH);
        resourcePath = (String) ConfigureManager.getInstance().getConfig(ConfigPool.MVC.RESOURCE_PATH);
        webpagePath = (String) ConfigureManager.getInstance().getConfig(ConfigPool.MVC.WEBPAGE_PATH);
        LogManager.info_("Jar包模块参数 -> 拓展程序jar包扫描路径：{} Web程序jar包扫描路径：{} Web包内资源映射路径：{}" +
                " Web包内网页映射路径：{}", extProgramPath, webProgramPath,resourcePath,webpagePath);
        // 递归扫描程序包获取实例
        LogManager.info_("【Jar包模块】正在扫描拓展程序包...");
        scanPackageFromPath(extProgramPath);
        LogManager.info_("【Jar包模块】正在扫描Web程序包...");
        scanPackageFromPath(webProgramPath);
        setJarUrlClassLoader();
        LogManager.info_("【Jar包模块】初始化完成");
    }

    // 获取单例（防止高并发导致资源访问问题进行双判空保护）
    public static JarManager getInstance() {
        if(instance == null)
            synchronized (JarManager.class){
                if(instance == null)
                    instance = new JarManager();
            }
        return instance;
    }

    // 递归扫描指定路径程序包
    private void scanPackageFromPath(String path){
        if(path == null || path.isEmpty())
            return;
        // 读取文件对象
        File root = new File(path);
        // 判定文件是否存在
        if(!root.exists())
            LogManager.error_("目标文件不存在",root.getName());
        // 判定文件对象是否为目录
        if(root.isDirectory()){
            // 如果对象是目录，则递归查找
            for(File f : Objects.requireNonNull(root.listFiles())){
                scanPackageFromPath(path + StrPool.SLASH + f.getName());
            }
        }else{
            // 如果对象是文件，则判定是否为jar包文件
            String fileName = root.getName();
            String extName = fileName.substring(fileName.lastIndexOf(StrPool.POINT)+1).toLowerCase();
            if(extName.equals(StrPool.JAR)){
                jarPaths.add(path);
            }
        }
    }

    // 解析拓展程序包，整理成资源类加载器
    private void setJarUrlClassLoader(){
        LogManager.info_("【Jar包模块】正在解析jar包与生成类加载器...");
        // 准备URL容器
        List<URL> urls = new ArrayList<>();
        // 读取jar包，将其URL存入容器
        for(String jarPath : jarPaths){
            File file = new File(jarPath);
            try{
                urls.add(file.toURI().toURL());
            } catch (MalformedURLException e) {
                LogManager.error_("在读取jar包URL路径时出现错误",e);
            }
        }
        // URL集合作为参数生成资源类加载器
        jarUrlClassLoader = new URLClassLoader(urls.toArray(new URL[0]),
                Thread.currentThread().getContextClassLoader());
    }

    // 读取jar包所有类对象
    public List<Class<?>> getClassObjects() {
        List<Class<?>> classObjects = new ArrayList<>();
        // 读取jar包
        for(String jarPath : jarPaths){
            try (JarFile jarFile = new JarFile(new File(jarPath))){
                // 开始获取jar中的.class文件
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry nextElement = entries.nextElement();
                    String name = nextElement.getName();
                    if (name.endsWith(StrPool.CLASS_POINT)) {
                        String replace = name.replace(StrPool.CLASS_POINT, StrPool.EMPTY).replace(StrPool.SLASH, StrPool.POINT);
                        classObjects.add(jarUrlClassLoader.loadClass(replace));
                    }
                }
            } catch (IOException e) {
                LogManager.error_("读取jar包的时候发生错误",e);
            } catch (ClassNotFoundException e) {
                LogManager.error_("加载拓展程序类对象的时候发生错误",e);
            }
        }
        return classObjects;
    }

    // 获取网页资源
    public InputStream getWebpage(String path){
        return jarUrlClassLoader.getResourceAsStream(webpagePath + path);
    }

    // 获取其他资源
    public InputStream getResource(String path){
        return jarUrlClassLoader.getResourceAsStream(resourcePath + path);
    }
}
