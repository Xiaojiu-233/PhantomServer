package xj.interfaces.component;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * 可视化界面管理器使用的可视化界面接口，为可视化界面的实现提供规范
 * */
public interface MonitorPanel {

    /**
     * 返回界面标题
     * */
    String returnTitle();

    /**
     * 返回界面HTTP页面路径，返回null则从可视化界面主页目录附近查找
     * */
    String returnWebpagePath();

    /**
     * 获取数据
     * */
    Map<String, Object> getData(Map<String, Object> data);

    /**
     * 存储数据
     * */
    void setData(Map<String, Object> data);
}
