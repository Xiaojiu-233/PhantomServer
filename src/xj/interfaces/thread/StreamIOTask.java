package xj.interfaces.thread;

import xj.interfaces.server.IReceiver;

/**
 * 线程的数据流IO任务接口，为数据流的IO提供统一的管理接口
 */
public interface StreamIOTask {

    void setReceiver(IReceiver receiver);
}
