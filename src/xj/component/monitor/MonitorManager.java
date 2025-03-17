package xj.component.monitor;

import xj.component.conf.ConfigureManager;
import xj.component.log.LogManager;
import xj.core.extern.IOCManager;
import xj.core.server.ServerManager;
import xj.core.threadPool.ThreadPoolManager;
import xj.implement.monitor.*;
import xj.implement.observer.SocketSelectorObserver;
import xj.implement.thread.TCPSelectorTask;
import xj.interfaces.component.MonitorPanel;
import xj.tool.ConfigPool;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 可视化界面管理器，用于为用户提供可视化的服务器监控管理与健康检查系统
public class MonitorManager {

    // 成员属性
    private static volatile MonitorManager instance;// 单例模式实现

    private final Map<Class<?>,MonitorPanel> panelMapping;// 可视化界面映射

    private float refreshTime;// 界面刷新时间

    // 成员方法
    // 初始化
    public MonitorManager() {
        LogManager.info_("【可视化界面模块】开始初始化");
        // 成员属性设置
        panelMapping = new HashMap<>();
        // 相关工厂初始化
        PanelComponentFactory.getInstance();
        // 读取基础配置
        refreshTime = (Integer) ConfigureManager.getInstance().getConfig(ConfigPool.MONITOR.REFRESH_TIME);
        // 读取本地界面对象
        panelMapping.put(WebMonitorPanel.class, new WebMonitorPanel());
        panelMapping.put(ThreadPoolMonitorPanel.class, new ThreadPoolMonitorPanel());
        panelMapping.put(IOCLogMonitorPanel.class, new IOCLogMonitorPanel());
        panelMapping.put(SystemMonitorPanel.class, new SystemMonitorPanel());
        panelMapping.put(MVCChatMonitorPanel.class, new MVCChatMonitorPanel());
        // 读取拓展界面对象
        List<Object> extPanels = IOCManager.getInstance().returnImplInstancesByClass(MonitorPanel.class);
        for(Object panel : extPanels){
            panelMapping.put(panel.getClass(), (MonitorPanel) panel);
        }
        // 启动可视化界面
        openWebListenThread();
        // 启动界面渲染线程
        LogManager.info_("【可视化界面模块】初始化完成");
    }

    // 获取单例（防止高并发导致资源访问问题进行双判空保护）
    public static MonitorManager getInstance() {
        if(instance == null)
            synchronized (MonitorManager.class){
                if(instance == null){
                    // 判定是否满足可视化条件
                    if(!(Boolean)ConfigureManager.getInstance().getConfig(ConfigPool.MONITOR.ENABLE)){
                        LogManager.info_("可视化界面 并未在配置文件中开启，无法实现可视化界面模块");
                        return null;
                    }
                    // 满足则进行可视化模组实现
                    instance = new MonitorManager();
                }
            }
        return instance;
    }

    // 启动web监听请求线程
    private void openWebListenThread(){
        LogManager.info_("【可视化界面模块】正在设置与启动界面...");

    }

}
