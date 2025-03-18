package xj.component.monitor;

import java.io.Serializable;

// 可视化子界面信息
public class MonitorPanelInfo implements Serializable {

    // 成员属性
    private static final long serialVersionUID = 1L;

    private String panelTitle;

    private String panelName;

    // 成员方法
    public MonitorPanelInfo() {
    }

    public MonitorPanelInfo(String panelTitle, String panelName) {
        this.panelTitle = panelTitle;
        this.panelName = panelName;
    }

    public String getPanelTitle() {
        return panelTitle;
    }

    public void setPanelTitle(String panelTitle) {
        this.panelTitle = panelTitle;
    }

    public String getPanelName() {
        return panelName;
    }

    public void setPanelName(String panelName) {
        this.panelName = panelName;
    }
}
