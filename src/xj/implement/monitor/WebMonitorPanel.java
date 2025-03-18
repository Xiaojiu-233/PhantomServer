package xj.implement.monitor;

import xj.interfaces.component.MonitorPanel;

import java.util.Collections;
import java.util.Map;

// Web请求可视化界面
public class WebMonitorPanel implements MonitorPanel {

    @Override
    public String returnTitle() {
        return "Web请求";
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
