package xj.implement.monitor;

import xj.interfaces.component.MonitorPanel;

import javax.swing.*;
import java.util.Collections;
import java.util.Map;

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
        return Collections.emptyMap();
    }

    @Override
    public void setData(Map<String, Object> data) {

    }
}
