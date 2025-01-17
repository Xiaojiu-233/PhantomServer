package xj.core.extern;

import xj.annotation.ComponentImport;
import xj.component.conf.ConfigureManager;
import xj.component.log.LogManager;
import xj.interfaces.component.IConfigureManager;
import xj.interfaces.component.ILogManager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// MVC管理器，为MVC模块的核心部分，用于为HTTP请求提供资源映射与后端处理的解决方案
public class MVCManager {

    // 成员属性
    private static volatile MVCManager instance;// 单例模式实现

    // 成员方法
    // 初始化
    public MVCManager() {
        LogManager.info_("【MVC模块】开始初始化");
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


}
