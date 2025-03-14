package xj.implement.monitor;

import xj.interfaces.component.MonitorPanel;

import javax.swing.*;

// MVC框架与聊天室框架可视化界面
public class MVCChatMonitorPanel implements MonitorPanel {

    @Override
    public String returnTitle() {
        return "MVC与聊天室";
    }

    @Override
    public JPanel drawPanel() {
        return null;
    }

    @Override
    public void refreshPanel() {

    }
}
