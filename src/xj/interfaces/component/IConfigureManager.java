package xj.interfaces.component;

// 配置管理器的接口形式，为拓展程序用户提供调用配置的接口
public interface IConfigureManager {

    // 返回配置
    Object getConfig(String key);

}
