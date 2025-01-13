package xj.core.ioc;

import xj.component.conf.ConfigureManager;
import xj.component.log.LogManager;

import java.util.HashMap;
import java.util.Map;

// IOC管理器，为IOC模块的核心部分，用于对拓展程序进行反射注入
public class IOCManager {

    // 成员属性
    private static volatile IOCManager instance;// 单例模式实现

    private Map<String,Object> IOCContainer;// IOC容器

    // 成员方法
    // 初始化
    public IOCManager() {
        LogManager.info_("【IOC模块】开始初始化");
        // 扫描程序包获取实例，导入到IOC容器
        scanFromPackageAndImport();
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

    // 扫描程序包，通过反射获取实例，导入到IOC容器
    private void scanFromPackageAndImport(){
        LogManager.info_("【IOC模块】正在扫描拓展程序包...");
        LogManager.info_("【IOC模块】正在扫描Web程序包...");
    }

    // 依赖注入
    private void autowiredInjection(){
        LogManager.info_("【IOC模块】正在进行实例的依赖注入...");
    }
}
