package xj.component.conf;

import org.yaml.snakeyaml.Yaml;
import xj.tool.StrPool;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

// 配置管理器，用于读取与管理与服务器相关的各种配置
public class ConfigureManager {

    //成员属性
    private static ConfigureManager instance;// 单例模式实现

    private final String configPath = "resource/config.yml";// 配置读取路径

    private Map<String,Object> configList = new HashMap<>();// 配置列表

    // 成员行为
    // 初始化
    public ConfigureManager(){
        initConfig();
    }

    // 获取单例
    public static ConfigureManager getInstance(){
        if(instance == null){
            instance = new ConfigureManager();
        }
        return instance;
    }

    // 读取配置文件
    public void initConfig(){
        // 创建Yaml对象
        Yaml yaml = new Yaml();
        // 读取配置文件并解析
        Map<String,Object> map;
        try {
            FileInputStream fileInputStream = new FileInputStream(configPath);
            map = yaml.load(fileInputStream);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        // 遍历Map对象，处理读取到的数据
        readConfigFromMap(map,"");
    }

    // dfs遍历读取map容器数据并存入配置列表
    private void readConfigFromMap(Map<String, Object> map,String prefix){
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
    public Object getConfig(String key){
        return configList.getOrDefault(key,null);
    }
}
