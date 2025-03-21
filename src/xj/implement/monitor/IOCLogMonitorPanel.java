package xj.implement.monitor;

import xj.component.conf.ConfigureManager;
import xj.component.log.LogManager;
import xj.core.extern.IOCManager;
import xj.core.server.ServerManager;
import xj.interfaces.component.MonitorPanel;
import xj.tool.ConfigPool;

import javax.swing.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// IOC容器和日志可视化界面
public class IOCLogMonitorPanel implements MonitorPanel {

    @Override
    public String returnTitle() {
        return "IOC与日志";
    }

    @Override
    public String returnWebpagePath() {
        return null;
    }

    @Override
    public Map<String, Object> getData(Map<String, Object> data) {
        Map<String, Object> ret = new HashMap<>();
        Map<String, Object> IOCRet = new HashMap<>();
        Map<String, Object> LogRet = new HashMap<>();
        // 基础参数
        IOCRet.put("拓展程序jar包扫描路径", ConfigureManager.getInstance().getConfig(ConfigPool.IOC.SCAN_PACKAGE));
        IOCRet.put("拓展程序注入class扫描路径", ConfigureManager.getInstance().getConfig(ConfigPool.IOC.EXT_PROGRAM_PATH));
        IOCRet.put("IOC容器实例信息", IOCManager.getInstance().returnIOCInstancesInfo());
        LogRet.put("当前使用的日志服务类", LogManager.getInstance().getLogServiceName());
        // 存储数据
        ret.put("IOC",IOCRet);
        ret.put("Log",LogRet);
        return ret;
    }

    @Override
    public void setData(Map<String, Object> data) {

    }
}
