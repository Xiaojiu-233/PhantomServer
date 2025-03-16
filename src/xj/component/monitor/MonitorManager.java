package xj.component.monitor;

import com.sun.org.apache.bcel.internal.Const;
import xj.abstracts.connect.ConnectHandler;
import xj.component.conf.ConfigureManager;
import xj.component.log.LogManager;
import xj.core.extern.IOCManager;
import xj.implement.monitor.*;
import xj.interfaces.component.MonitorPanel;
import xj.interfaces.mvc.ContentTypeHandler;
import xj.tool.ConfigPool;
import xj.tool.Constant;
import xj.tool.StrPool;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// 可视化界面管理器，用于为用户提供可视化的服务器监控管理与健康检查系统
public class MonitorManager {

    // 成员属性
    private static volatile MonitorManager instance;// 单例模式实现

    private final Map<Class<?>,MonitorPanel> panelMapping;// 可视化界面映射

    private JFrame frame;// 主框架

    private JPanel mainPanel;// 主界面

    private MonitorPanel mainMoniterPanel;// 主可视化界面对象

    private long refreshTime,refreshTimer;// 刷新渲染时间及计时器

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
                    String osName = (String)ConfigureManager.getInstance()
                            .getConfig(ConfigPool.SYSTEM_ARG.OS_NAME);
                    if(!osName.contains(StrPool.WINDOWS) && !osName.contains(StrPool.LINUX)){
                        LogManager.info_("操作系统非Windows系统也非Linux系统，无法实现可视化界面模块");
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
        LogManager.info_("【可视化界面模块】正在设置与启动界面...");
        // 开启可视化界面配置
        frame = new JFrame();
        frame.setTitle("PhantomServer可视化操作界面");
        frame.setLocation(Constant.MONITOR_LOCATION_X,Constant.MONITOR_LOCATION_Y);
        frame.setSize( Constant.MONITOR_SIZE_X, Constant.MONITOR_SIZE_Y );
        frame.setResizable(false);
        frame.setLayout(null);
        // 读取每一个界面，设置其导航栏按钮，设置相关点击属性
        JPanel navigatePanel = new JPanel();
        navigatePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        navigatePanel.setBounds(0,0,Constant.MONITOR_SIZE_X,Constant.MONITOR_NAVIGATE_HEIGHT);
        List<MonitorPanel> panels = new ArrayList<>(panelMapping.values());
        for(MonitorPanel panel : panels){
            JButton navigateButton = new JButton(panel.returnTitle());
            navigateButton.addActionListener(e -> {
                synchronized (MonitorManager.class){
                    if(mainPanel != null)
                        frame.remove(mainPanel);
                    mainMoniterPanel = panel;
                    mainPanelDraw();
                }
            });
            navigatePanel.add(navigateButton);
        }
        frame.add(navigatePanel);
        // 设置默认界面渲染
        mainMoniterPanel = panelMapping.get(WebMonitorPanel.class);
        mainPanelDraw();
        frame.add(mainPanel);
        // 启动可视化界面
        frame.setVisible(true);
    }

    // 启动界面刷新线程
    private void openPanelRefreshThread(){
        LogManager.info_("【可视化界面模块】正在启动界面渲染线程...");
        new Thread(() -> {
            while(true){
                synchronized (MonitorManager.class){
                    long nowTime = System.currentTimeMillis();
                    if(nowTime - refreshTimer > refreshTime){
                        mainMoniterPanel.refreshPanel();
                        frame.repaint();
                        refreshTimer = nowTime;
                    }
                }
            }
        }).start();
    }

    // 修改渲染刷新时间
    public void setRefreshTime(long refreshTime){
        synchronized (MonitorManager.class){
            this.refreshTime = refreshTime;
        }
    }

    // 让主界面开始渲染
    private void mainPanelDraw(){
        if(mainMoniterPanel == null){
            LogManager.error_("当前主可视化界面对象为空，无法进行渲染");
            return;
        }
        mainPanel = mainMoniterPanel.drawPanel();
        if(mainPanel == null){
            LogManager.error_("渲染得到可视化界面为空，无法进行渲染");
            return;
        }
        mainPanel.setBounds(0,Constant.MONITOR_NAVIGATE_HEIGHT,Constant.MONITOR_SIZE_X,
                Constant.MONITOR_SIZE_Y - Constant.MONITOR_NAVIGATE_HEIGHT);
    }

}
