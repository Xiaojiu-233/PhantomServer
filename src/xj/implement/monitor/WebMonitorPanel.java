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
        int pageSize = (int) data.get("pageSize");
        int pageNum = (int) data.get("pageNum");
        pageSize = Math.max(pageSize, 1);
        pageNum = Math.max(pageNum, 1);
        // 基础参数
        ret.put("监听端口", ConfigureManager.getInstance().getConfig(ConfigPool.SERVER.PORT));
        ret.put("请求单元分隔符", ConfigureManager.getInstance().getConfig(ConfigPool.SERVER.UNIT_SPLIT_BREAK));
        ret.put("服务器名称", ConfigureManager.getInstance().getConfig(ConfigPool.SERVER.NAME));
        ret.put("socket最大连接等待时间", ConfigureManager.getInstance().getConfig(ConfigPool.SERVER.SOCKET_MAX_WAIT_TIME));
        List<Object> list = ServerManager.getInstance().getSelectorTask().getChannelsInfo();
        List<Object> channelInfo = new ArrayList<>();
        if(pageNum > 0)
            for(int i = (pageNum-1)*pageSize,j = 0;j < pageSize && i < list.size();i++,j++){
                channelInfo.add(list.get(i));
            }
        ret.put("服务器channel连接信息",channelInfo);
        ret.put("信息分页总页数",(int)Math.ceil((double) list.size() / pageSize));
        // 存储数据
        return ret;
    }

    @Override
    public void setData(Map<String, Object> data) {
        ServerManager.getInstance().getSelectorTask().clearInfos();
    }
}
