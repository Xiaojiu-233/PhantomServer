package xj.implement.monitor;

import xj.interfaces.component.MonitorPanel;

import javax.swing.*;

// IOC容器和日志可视化界面
public class IOCLogMonitorPanel implements MonitorPanel {

    @Override
    public String returnTitle() {
        return "IOC与日志";
    }

    @Override
    public JPanel drawPanel() {
        return null;
    }

    @Override
    public void refreshPanel() {

    }
}
