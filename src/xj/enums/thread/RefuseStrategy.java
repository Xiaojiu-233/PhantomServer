package xj.enums.thread;

import xj.component.log.LogManager;

// 动态线程池对任务的拒绝策略
public enum RefuseStrategy {

    THROW_EXCEPTION,// 抛出异常
    THROW_TASK,// 不执行该任务
    THROW_QUEUE_TASK;// 将线程池里最早进入队列的任务

    public static RefuseStrategy getStrategyByString(String strategy){
        try{
            return RefuseStrategy.valueOf(strategy);
        }catch (IllegalArgumentException e){
            LogManager.error("获取拒绝策略时出错，已使用默认方案-不执行任务",e);
            return RefuseStrategy.THROW_TASK;
        }
    }

}
