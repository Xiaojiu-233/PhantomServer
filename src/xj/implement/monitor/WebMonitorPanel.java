package xj.implement.monitor;

import xj.interfaces.component.MonitorPanel;

import javax.swing.*;

// Web请求可视化界面
public class WebMonitorPanel implements MonitorPanel {

    @Override
    public String returnTitle() {
        return "Web请求";
    }

    @Override
    public JPanel drawPanel() {
        return null;
    }

    @Override
    public void refreshPanel() {

    }
}
