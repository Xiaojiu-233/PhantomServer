package xj.implement.monitor;

import xj.component.conf.ConfigureManager;
import xj.component.log.LogManager;
import xj.core.server.ServerManager;
import xj.core.threadPool.ThreadPoolManager;
import xj.interfaces.component.MonitorPanel;
import xj.tool.ConfigPool;

import javax.swing.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 动态线程池可视化界面
public class ThreadPoolMonitorPanel implements MonitorPanel {

    @Override
    public String returnTitle() {
        return "线程池";
    }

    @Override
    public String returnWebpagePath() {
        return null;
    }

    @Override
    public Map<String, Object> getData(Map<String, Object> data) {
        Map<String, Object> ret = new HashMap<>();
        int k = (int) data.getOrDefault("k",1);
        // 基础参数
        ret.put("核心线程数", ThreadPoolManager.getInstance().getCoreThread());
        ret.put("最大线程数", ThreadPoolManager.getInstance().getMaxThread());
        ret.put("工作线程模板名", ThreadPoolManager.getInstance().getThreadName());
        ret.put("任务队列大小", ThreadPoolManager.getInstance().getQueueCapacity());
        ret.put("线程最大闲置时间(秒)", ThreadPoolManager.getInstance().getThreadMaxFreeTime());
        ret.put("拒绝策略", ThreadPoolManager.getInstance().getStrategy());
        ret.put("工作线程情况", ThreadPoolManager.getInstance().returnThreadInfos());
        ret.put("普通线程数图表", ThreadPoolManager.getInstance().getCommonThreadChart().outputChart(k,false));
        ret.put("所有线程数图表", ThreadPoolManager.getInstance().getAllThreadChart().outputChart(k,false));
        ret.put("回收线程数图表", ThreadPoolManager.getInstance().getRecycledThreadChart().outputChart(k,false));
        ret.put("队列任务数图表", ThreadPoolManager.getInstance().getQueueTaskChart().outputChart(k,false));
        // 存储数据
        return ret;
    }

    @Override
    public void setData(Map<String, Object> data) {
        int commonThreadNum = (int) data.getOrDefault("commonThreadNum",10);
        int queueTaskNum = (int)  data.getOrDefault("queueTaskNum",10);
        String recycleStrategy = (String) data.getOrDefault("recycleStrategy","timeout");
        String refuseStrategy = (String) data.getOrDefault("refuseStrategy","THROW_TASK");
        ThreadPoolManager.getInstance().changeProperties(commonThreadNum,queueTaskNum,recycleStrategy,refuseStrategy);
    }
}
