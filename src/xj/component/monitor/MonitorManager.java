package xj.component.monitor;

import xj.component.conf.ConfigureManager;
import xj.component.log.LogManager;
import xj.interfaces.component.MonitorPanel;
import xj.interfaces.mvc.ContentTypeHandler;
import xj.tool.ConfigPool;
import xj.tool.StrPool;

import java.util.ArrayList;
import java.util.List;

// 可视化界面管理器，用于为用户提供可视化的服务器监控管理与健康检查系统
public class MonitorManager {

    // 成员属性
    private static volatile MonitorManager instance;// 单例模式实现

    private final List<MonitorPanel> panelList = new ArrayList<>();// 可视化界面列表

    // 成员方法
    // 初始化
    public MonitorManager() {
        LogManager.info_("【可视化界面模块】开始初始化");
        // 读取拓展界面对象
        // 设置本地界面对象
        // 启动可视化界面
        openVisualPanel();
        // 启动界面渲染线程
        openPanelRefreshThread();
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
                    if(!((String)ConfigureManager.getInstance().getConfig(ConfigPool.SYSTEM_ARG.OS_NAME))
                            .contains(StrPool.WINDOWS)){
                        LogManager.info_("操作系统非Windows系统，无法实现可视化界面模块");
                        return null;
                    }
                    // 满足则进行可视化模组实现
                    instance = new MonitorManager();
                }
            }
        return instance;
    }

    // 启动可视化界面
    private void openVisualPanel(){

    }

    // 启动界面刷新线程
    private void openPanelRefreshThread(){

    }

    // 界面刷新线程任务
    private void panelThreadTask(){

    }

}
