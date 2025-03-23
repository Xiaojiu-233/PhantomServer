package xj.entity.monitor;

import xj.component.log.LogManager;
import xj.tool.StrPool;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 可视化界面使用的图表数据对象，用于快捷生成图表数据
 * */
public class MonitorChart {

    // 成员属性
    private boolean isAccel;// 是否为增量图表

    private List<Float[]> list;// 数据数组

    private float storeNumber;// 暂存的单位数据

    private long storageTimer,recordTimer;// 数据刷新计时器 记录计时器

    private boolean isInput;// 是否填写数据

    private float accelCounter;// 增量计数器

    // 常量
    private final int MONITOR_CHART_UNIT_TIME = 5;// 可视化界面单元时间（秒）

    private final int MONITOR_CHART_X_RANGE = 5;// 可视化界面图表X轴大小

    private String TIME_FORMAT = "%02d";

    private String FLOAT_FORMAT = "%.2f";

    // 成员方法
    /**
     * 初始化
     * */
    public MonitorChart(boolean isAccel) {
        this.isAccel = isAccel;
        list = new ArrayList<>();
        storageTimer = System.currentTimeMillis();
        recordTimer = System.currentTimeMillis();
    }

    /**
     * 填入数据
     * */
    public void inputData(int number) {
        // 数据存储或增值
        storeNumber = isAccel ? (number + storeNumber) : number;
        isInput = true;
        refreshInputData();
    }
    public void inputData(float number) {
        // 数据存储或增值
        storeNumber = isAccel ? (number + storeNumber) : number;
        isInput = true;
        refreshInputData();
    }

    // 刷新填入数据
    private void refreshInputData(){
        // 查看时间
        long nowTime = System.currentTimeMillis();
        long timeDelta = nowTime - storageTimer;
        // 如果计时器达到刷新时间点即最小存储单位时间时，存储数据
        if(timeDelta >= MONITOR_CHART_UNIT_TIME * 1000L){
            if(isInput){
                int timeNum = (int) ((nowTime - recordTimer) / 1000);
                list.add(new Float[]{(float) timeNum,storeNumber});
                accelCounter += isAccel ? storeNumber : 0;
                storeNumber = 0;
                isInput = false;
            }
            storageTimer = nowTime;
        }
    }

    /**
     * 输出图表数据
     * */
    public Map<String,Object> outputChart(int k,boolean retFloat) {
        refreshInputData();
        // 数据准备
        Map<String,Object> ret =  new HashMap<>();
        List<String> x = new ArrayList<>();
        List<Object> y = new ArrayList<>();
        long nowTime = System.currentTimeMillis();
        LocalDateTime now = LocalDateTime.now();
        int nowTimeNum = (int) ((nowTime - recordTimer) / 1000);
        int expireRange = nowTimeNum - MONITOR_CHART_X_RANGE * 60;
        // 数据整理
        if (!list.isEmpty()){
            Float[] last = list.get(list.size()-1);
            if (last[0] < expireRange) {
                list.clear();
                if (!isAccel)
                    list.add(last);
            }else
                list = list.stream().filter(item -> item[0] >= expireRange).collect(Collectors.toList());
        }
        // 分析数据
        float[] yRet = new float[MONITOR_CHART_X_RANGE + 1];
        int[] xRange = new int[MONITOR_CHART_X_RANGE + 1];
        for (int i = 0; i < xRange.length; i++)
            xRange[i] = nowTimeNum - (MONITOR_CHART_X_RANGE - i) * k * MONITOR_CHART_UNIT_TIME;
        for (Float[] integers : list)
            for (int j = 0; j < xRange.length; j++)
                if (integers[0] <= xRange[j]) {
                    yRet[j] = isAccel ? (yRet[j] + integers[1]) : integers[1];
                    break;
                }
        for(int i=1;i<xRange.length;i++){
            // x轴
            LocalDateTime minusTime =
                    now.minusSeconds((long) (MONITOR_CHART_X_RANGE - i) * k * MONITOR_CHART_UNIT_TIME);
            x.add(String.format(TIME_FORMAT,minusTime.getMinute()) + StrPool.COLON
                    + String.format(TIME_FORMAT,minusTime.getSecond()));
            // y轴
            if (!isAccel)
                yRet[i] = yRet[i] == 0 ? yRet[i-1] : yRet[i];
            y.add(retFloat ? Float.parseFloat(String.format(FLOAT_FORMAT,yRet[i])) : (int)yRet[i]);
        }
        // 刷新数据
        recordTimer = nowTime;
        for(Float[] f : list){
            f[0] -= nowTimeNum;
        }
        // 返回结果
        ret.put("x", x);
        ret.put("y", y);
        return ret;
    }

    public List<Float[]> getList() {
        return list;
    }

    public long getAccelCounter() {
        return (long) accelCounter;
    }
}
