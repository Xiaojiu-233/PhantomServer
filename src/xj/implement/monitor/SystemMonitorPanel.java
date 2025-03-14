package xj.implement.monitor;

import xj.interfaces.component.MonitorPanel;

import javax.swing.*;

// 系统可视化界面
public class SystemMonitorPanel implements MonitorPanel {

    @Override
    public String returnTitle() {
        return "系统";
    }

    @Override
    public JPanel drawPanel() {
        return null;
    }

    @Override
    public void refreshPanel() {

    }
}
