package xj.entity.monitor;

import xj.tool.Constant;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 可视化界面使用的图表数据对象，用于快捷生成图表数据
 * */
public class MonitorChart {

    // 成员属性
    private boolean isAccel;// 是否为增量图表

    private int[] arr;// 数据数组

    private int storeNumber;// 暂存的单位数据

    private long storageTimer;// 数据刷新计时器

    private int dataPointer;// 数据指针

    // 常量
    private final int MONITOR_CHART_DATA_CAPACITY = 60;// 可视化界面图表存储数据容量

    private final int MONITOR_CHART_ACCEL_MAXMIZE = 100000;// 可视化界面增量存储数据最大值，用于取模

    private final int MONITOR_CHART_UNIT_TIME = 5;// 可视化界面单元时间（秒）

    private final int MONITOR_CHART_X_RANGE = 5;// 可视化界面图表X轴大小

    // 成员方法
    /**
     * 初始化
     * */
    public MonitorChart(boolean isAccel) {
        this.isAccel = isAccel;
        arr = new int[MONITOR_CHART_DATA_CAPACITY];
        storageTimer = System.currentTimeMillis();
    }

    /**
     * 填入数据
     * */
    public void inputData(int number) {
        // 数据存储或增值
        storeNumber = isAccel ? number + storeNumber : number;
        // 查看时间
        long timeDelta = System.currentTimeMillis() - storageTimer;
        // 如果计时器达到刷新时间点即最小存储单位时间时，存储数据
        if(timeDelta >= MONITOR_CHART_UNIT_TIME * 1000L){
            arr[dataPointer] = !isAccel ? storeNumber
                    : (arr[(dataPointer - 1) % MONITOR_CHART_DATA_CAPACITY] + storeNumber)
                    % MONITOR_CHART_ACCEL_MAXMIZE;
            storageTimer = System.currentTimeMillis();
            storeNumber = 0;
            dataPointer++;
        }
    }

    /**
     * 输出图表数据
     * */
    public Map<String,Object> outputChart(int k) {
        // 数据准备
        Map<String,Object> ret =  new HashMap<>();
        List<String> x = new ArrayList<>();
        List<Integer> y = new ArrayList<>();
        // 分析数据
        LocalDateTime nowTime = LocalDateTime.now();
        for(int i = (MONITOR_CHART_X_RANGE - 1) * k;i >= 0; i -= k){
            int pos = (dataPointer - i) % MONITOR_CHART_DATA_CAPACITY;
            LocalDateTime minusTime = nowTime.minusSeconds((long) i * MONITOR_CHART_UNIT_TIME);
            x.add(new SimpleDateFormat("mm:ss").format(minusTime));
            y.add(!isAccel ? arr[pos] : (arr[pos] - arr[(pos - 1) % MONITOR_CHART_DATA_CAPACITY])
                    % MONITOR_CHART_ACCEL_MAXMIZE);
        }
        // 返回结果
        ret.put("x", x);
        ret.put("y", y);
        return ret;
    }
}
