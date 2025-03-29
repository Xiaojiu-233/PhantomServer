package xj.implement.monitor;

import xj.component.conf.ConfigureManager;
import xj.component.log.LogManager;
import xj.core.extern.IOCManager;
import xj.core.extern.chat.ChatManager;
import xj.core.extern.mvc.MVCManager;
import xj.interfaces.component.MonitorPanel;
import xj.tool.ConfigPool;

import javax.swing.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// MVC框架与聊天室框架可视化界面
public class MVCChatMonitorPanel implements MonitorPanel {

    private final int MAX_MESSAGE_SHOW = 20;

    @Override
    public String returnTitle() {
        return "MVC与聊天室";
    }

    @Override
    public String returnWebpagePath() {
        return null;
    }

    @Override
    public Map<String, Object> getData(Map<String, Object> data) {
        int k = (int) data.getOrDefault("k",1);
        k = Math.max(k, 1);
        int maxMsg = (int) data.getOrDefault("maxMsg",MAX_MESSAGE_SHOW);
        maxMsg = Math.max(maxMsg, 1);
        Map<String, Object> ret = new HashMap<>();
        Map<String, Object> MVCRet = new HashMap<>();
        Map<String, Object> ChatRet = new HashMap<>();
        // 基础参数
        MVCRet.put("MVC主页路径", ConfigureManager.getInstance().getConfig(ConfigPool.MVC.INDEX_PATH));
        MVCRet.put("MVC静态资源映射路径", ConfigureManager.getInstance().getConfig(ConfigPool.MVC.RESOURCE_PATH));
        MVCRet.put("MVC请求拦截处理器信息", MVCManager.getInstance().returnHandlerMappingInfo());
        MVCRet.put("MVC的ContentType转移器信息", MVCManager.getInstance().returnContentTypeConverterInfo());
        ChatRet.put("聊天室消息缓存块数量", ConfigureManager.getInstance().getConfig(ConfigPool.CHAT.MESSAGE_CACHE_NUM));
        ChatRet.put("聊天室消息缓存块容量", ChatManager.getInstance().getCacheCapacity());
        ChatRet.put("聊天室消息图片存储路径", ConfigureManager.getInstance().getConfig(ConfigPool.CHAT.CHAT_IMAGE_PATH));
        ChatRet.put("聊天室消息缓存块详情", ChatManager.getInstance().returnMessageCacheInfos(k,maxMsg));
        // 存储数据
        ret.put("MVC",MVCRet);
        ret.put("Chat",ChatRet);
        return ret;
    }

    @Override
    public void setData(Map<String, Object> data) {
        int cacheCapacity = (int)  data.getOrDefault("cacheCapacity",1);
        cacheCapacity = Math.max(cacheCapacity, 1);
        ChatManager.getInstance().setCacheCapacity(cacheCapacity);
    }
}
