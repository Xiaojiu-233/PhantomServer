package xj.interfaces.component;

import xj.interfaces.thread.StreamIOTask;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 线程任务工厂的接口
 */
public interface IThreadTaskFactory {

    /**
     * 创建数据流输入IO任务
     */
    StreamIOTask createStreamInputTask(InputStream in);

    /**
     * 创建数据流输出IO任务
     */
    StreamIOTask createStreamOutputTask(InputStream out,String targetPath);
}
