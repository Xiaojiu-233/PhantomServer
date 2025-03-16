package xj.implement.monitor;

import xj.component.conf.ConfigureManager;
import xj.component.monitor.PanelComponentFactory;
import xj.interfaces.component.MonitorPanel;
import xj.tool.ConfigPool;
import xj.tool.Constant;

import javax.swing.*;
import java.awt.*;

// Web请求可视化界面
public class WebMonitorPanel implements MonitorPanel {

    // 成员属性
    Box args;// 参数显示框

    String[][] tableValues;

    // 成员行为
    @Override
    public String returnTitle() {
        return "Web请求";
    }

    @Override
    public JPanel drawPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        // 左边为参数框
        args = Box.createVerticalBox();
        args.add(PanelComponentFactory.getInstance().createArgTextUnit("服务器名称："));
        args.add(PanelComponentFactory.getInstance().createArgTextUnit(
                (String)ConfigureManager.getInstance().getConfig(ConfigPool.SERVER.NAME)
        ));
        args.add(PanelComponentFactory.getInstance().createArgTextUnit("服务器监听接口："));
        args.add(PanelComponentFactory.getInstance().createArgTextUnit(
                String.valueOf(ConfigureManager.getInstance().getConfig(ConfigPool.SERVER.PORT))
        ));
        args.add(PanelComponentFactory.getInstance().createArgTextUnit("服务器请求单元分隔符："));
        args.add(PanelComponentFactory.getInstance().createArgTextUnit(
                (String)ConfigureManager.getInstance().getConfig(ConfigPool.SERVER.UNIT_SPLIT_BREAK)
        ));
        args.add(PanelComponentFactory.getInstance().createArgTextUnit("Socket最大等待输入时间(毫秒)："));
        args.add(PanelComponentFactory.getInstance().createArgTextUnit(
                String.valueOf(ConfigureManager.getInstance()
                        .getConfig(ConfigPool.SERVER.SOCKET_MAX_WAIT_TIME))
        ));
        int componentHeight = Constant.MONITOR_SIZE_Y - Constant.MONITOR_NAVIGATE_HEIGHT - Constant.MONITOR_MERGER_Y;
        JScrollPane argsPanel = new JScrollPane(args);
        argsPanel.setBackground(Color.white);
        argsPanel.setBounds(Constant.MONITOR_MERGER_X,0,(int)(Constant.MONITOR_SIZE_X * 0.2)
                ,componentHeight);
        panel.add(argsPanel);
        // 右边为channel数据框
        String[] columnNames = {"编号", "来源IP", "连接持续时间", "平均QPS", "使用处理器", "状态"}; // 定义表格列
        String[][] tableValues = new String[][]{{"", "", "", "", "", ""}};
        JTable channelTable = new JTable(tableValues,columnNames);
        channelTable.setBackground(Color.white);
        JScrollPane scrollTable = new JScrollPane(channelTable);
        scrollTable.setBounds((int)(Constant.MONITOR_SIZE_X * 0.2) + Constant.MONITOR_MERGER_X * 2
                ,0,(int)(Constant.MONITOR_SIZE_X * 0.75),componentHeight);
        panel.add(scrollTable);
        return panel;
    }

    @Override
    public void refreshPanel() {
    }
}
