package xj.implement.monitor;

import xj.interfaces.component.MonitorPanel;

import javax.swing.*;

// 动态线程池可视化界面
public class ThreadPoolMonitorPanel implements MonitorPanel {

    @Override
    public String returnTitle() {
        return "线程池";
    }

    @Override
    public JPanel drawPanel() {
        return null;
    }

    @Override
    public void refreshPanel() {

    }
}
