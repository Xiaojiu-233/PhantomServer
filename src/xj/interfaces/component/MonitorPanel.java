package xj.interfaces.component;

import java.awt.*;

/**
 * 可视化界面管理器使用的可视化界面接口，为可视化界面的实现提供规范
 * */
public interface MonitorPanel {

    /**
     * 完成界面渲染
     * */
    void drawPanel();

    /**
     * 刷新界面
     * */
    void refreshPanel();
}
