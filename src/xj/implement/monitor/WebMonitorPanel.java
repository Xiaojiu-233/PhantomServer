package xj.implement.monitor;

import xj.component.conf.ConfigureManager;
import xj.core.server.ServerManager;
import xj.interfaces.component.MonitorPanel;
import xj.tool.ConfigPool;

import java.util.*;

// Web请求可视化界面
public class WebMonitorPanel implements MonitorPanel {

    @Override
    public String returnTitle() {
        return "Web请求";
    }

    @Override
    public String returnWebpagePath() {
        return null;
    }

    @Override
    public Map<String, Object> getData(Map<String, Object> data) {
        Map<String, Object> ret = new HashMap<>();
        // 基础参数
        ret.put("监听端口", ConfigureManager.getInstance().getConfig(ConfigPool.SERVER.PORT));
        ret.put("请求单元分隔符", ConfigureManager.getInstance().getConfig(ConfigPool.SERVER.UNIT_SPLIT_BREAK));
        ret.put("服务器名称", ConfigureManager.getInstance().getConfig(ConfigPool.SERVER.NAME));
        ret.put("socket最大连接等待时间", ConfigureManager.getInstance().getConfig(ConfigPool.SERVER.SOCKET_MAX_WAIT_TIME));
        ret.put("服务器channel连接信息", ServerManager.getInstance().getSelectorTask().getChannelsInfo());
        // 存储数据
        return ret;
    }

    @Override
    public void setData(Map<String, Object> data) {

    }
}
