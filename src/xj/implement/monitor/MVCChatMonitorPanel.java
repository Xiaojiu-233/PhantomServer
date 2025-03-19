package xj.implement.monitor;

import xj.interfaces.component.MonitorPanel;

import javax.swing.*;
import java.util.Collections;
import java.util.Map;

// MVC框架与聊天室框架可视化界面
public class MVCChatMonitorPanel implements MonitorPanel {

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
        return Collections.emptyMap();
    }

    @Override
    public void setData(Map<String, Object> data) {

    }
}
