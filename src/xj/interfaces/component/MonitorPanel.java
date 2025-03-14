package xj.interfaces.component;

import javax.swing.*;
import java.awt.*;

/**
 * 可视化界面管理器使用的可视化界面接口，为可视化界面的实现提供规范
 * */
public interface MonitorPanel {

    /**
     * 返回界面标题
     * */
    String returnTitle();

    /**
     * 完成界面渲染
     * */
    JPanel drawPanel();

    /**
     * 刷新界面
     * */
    void refreshPanel();
}
