package xj.core.extern.chat;

import xj.abstracts.web.Response;
import xj.component.conf.ConfigureManager;
import xj.component.log.LogManager;
import xj.implement.web.TCPChatRequest;
import xj.implement.web.TCPChatResponse;
import xj.tool.ConfigPool;

import javax.security.auth.login.Configuration;
import java.time.LocalDateTime;

// 聊天室管理器，用于提供TCP聊天室的解决方案
public class ChatManager {

    // 成员属性
    private static volatile ChatManager instance;// 单例模式实现

    private ChatObject[][] messageCache;// 聊天记录缓存块，0为最新缓存块

    private LocalDateTime[] cacheTime;// 缓存块开始记录的时间

    private int pointer;// 消息指针，用于指明目前缓存块中最新的消息位置

    // 成员方法
    // 初始化
    public ChatManager() {
        LogManager.info_("【聊天室模块】开始初始化");
        initMessageCache();
        LogManager.info_("【聊天室模块】初始化完成");
    }

    // 获取单例（防止高并发导致资源访问问题进行双判空保护）
    public static ChatManager getInstance() {
        if(instance == null)
            synchronized (ChatManager.class){
                if(instance == null)
                    instance = new ChatManager();
            }
        return instance;
    }

    // 初始化缓存块
    private void initMessageCache() {
        LogManager.info_("【Jar包模块】正在初始化消息缓存块...");
        // 获取配置参数
        int cacheNum = (int) ConfigureManager.getInstance().getConfig(ConfigPool.CHAT.MESSAGE_CACHE_NUM);
        int cacheCapacity = (int) ConfigureManager.getInstance().getConfig(ConfigPool.CHAT.CACHE_CAPACITY);
        LogManager.info_("消息缓存块参数 -> 消息缓存块数量：{} 消息缓存块容量：{}",
                cacheNum, cacheCapacity);
        // 开辟空间
        messageCache = new ChatObject[cacheNum][cacheCapacity];
        cacheTime = new LocalDateTime[cacheNum];
    }

    // 处理数据
    public TCPChatResponse handle(TCPChatRequest req) {
        return null;
    }

}
