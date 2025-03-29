package xj.implement.monitor;

import xj.component.conf.ConfigureManager;
import xj.component.log.LogManager;
import xj.core.extern.IOCManager;
import xj.core.server.ServerManager;
import xj.interfaces.component.MonitorPanel;
import xj.tool.ConfigPool;
import xj.tool.Constant;
import xj.tool.StrPool;

import javax.swing.*;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

// 系统可视化界面
public class SystemMonitorPanel implements MonitorPanel {

    @Override
    public String returnTitle() {
        return "系统";
    }

    @Override
    public String returnWebpagePath() {
        return null;
    }

    @Override
    public Map<String, Object> getData(Map<String, Object> data) {
        int k = (int) data.getOrDefault("k",1);
        k = Math.max(k, 1);
        Map<String, Object> ret = new HashMap<>();
        // 读取数据
        ret.put("JVM堆内存最大值(单位MB)", ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax() / Constant.MB);
        ret.put("JVM使用版本", ManagementFactory.getRuntimeMXBean().getVmVersion());
        ret.put("JVM使用Java版本", System.getProperty("java.version"));
        StringBuilder sb = new StringBuilder();
        ManagementFactory.getGarbageCollectorMXBeans().forEach(gc -> {
            sb.append(gc.getName()).append(StrPool.SEMICOLON);
        });
        ret.put("JVM目前使用的垃圾回收机制", sb.toString());
        ret.put("JVM目前加载的类数量", ManagementFactory.getClassLoadingMXBean().getTotalLoadedClassCount());
        ret.put("JVM详细数据图表", ServerManager.getInstance().returnJVMInfo(k));
        return ret;
    }

    @Override
    public void setData(Map<String, Object> data) {

    }
}
