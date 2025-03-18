package xj.implement.monitor;

import xj.interfaces.component.MonitorPanel;

import javax.swing.*;
import java.util.Collections;
import java.util.Map;

// IOC容器和日志可视化界面
public class IOCLogMonitorPanel implements MonitorPanel {

    @Override
    public String returnTitle() {
        return "IOC与日志";
    }

    @Override
    public String returnWebpagePath() {
        return "";
    }

    @Override
    public Map<String, Object> getData(Map<String, Object> data) {
        return Collections.emptyMap();
    }

    @Override
    public void setData(Map<String, Object> data) {

    }
}
