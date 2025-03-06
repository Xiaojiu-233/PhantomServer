package xj.component.conf;

import org.yaml.snakeyaml.Yaml;
import sun.rmi.runtime.Log;
import xj.annotation.PRequestBody;
import xj.component.log.LogManager;
import xj.interfaces.component.IConfigureManager;
import xj.tool.ConfigPool;
import xj.tool.StrPool;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

// 配置管理器，用于读取与管理与服务器相关的各种配置
public class ConfigureManager implements IConfigureManager {

    // 成员属性
    private static volatile ConfigureManager instance;// 单例模式实现

    private final String configPath = ConfigPool.SYSTEM_PATH.CONFIG_FILE_PATH;// 配置读取路径

    private Map<String,Object> configList = new HashMap<>();// 配置列表

    // 成员方法
    // 初始化
    public ConfigureManager() {
        LogManager.info_("【配置模块】开始初始化");
        initConfig();
        initSystemConfig();
        LogManager.info_("【配置模块】初始化完成");
    }

    // 获取单例（防止高并发导致资源访问问题进行双判空保护）
    public static ConfigureManager getInstance() {
        if(instance == null)
            synchronized (ConfigureManager.class){
                if(instance == null)
                    instance = new ConfigureManager();
            }
        return instance;
    }

    // 读取配置文件
    public void initConfig() {
        LogManager.info_("【配置模块】正在读取配置文件...");
        // 创建Yaml对象
        Yaml yaml = new Yaml();
        // 读取配置文件并解析
        Map<String,Object> map;
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(configPath);
        } catch (FileNotFoundException e) {
            LogManager.error_("配置读取时出现异常", e);
        }
        map = yaml.load(fileInputStream);
        // 遍历Map对象，处理读取到的数据
        readConfigFromMap(map,"");
    }

    // 读取系统配置（如工作路径等）
    public void initSystemConfig(){
        LogManager.info_("【配置模块】正在读取系统配置...");
        // 读取工作路径
        String workpath = System.getProperty("user.dir");
        configList.put(ConfigPool.SYSTEM_ARG.WORK_PATH,workpath);
        LogManager.info_("当前的工作路径为",workpath);
        // 读取操作系统名称
        String osName = System.getProperty("os.name");
        configList.put(ConfigPool.SYSTEM_ARG.OS_NAME,osName);
        LogManager.info_("当前的操作系统为",osName);
        // 读取换行符
        String lineBreak = "\r\n"; //System.getProperty("line.separator","\n");
        configList.put(ConfigPool.SYSTEM_ARG.LINE_BREAK,lineBreak);
    }

    // dfs遍历读取map容器数据并存入配置列表
    private void readConfigFromMap(Map<String, Object> map, String prefix){
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            String configPath = prefix.isEmpty() ? key : (prefix + StrPool.POINT + key);
            if(value instanceof LinkedHashMap){
                readConfigFromMap((Map<String, Object>) value,configPath);
            }else{
                configList.put(configPath,value);
            }
        }
    }

    // 返回配置
    @Override
    public Object getConfig(String key){
        synchronized (ConfigureManager.class){
            return configList.getOrDefault(key,null);
        }
    }
}
