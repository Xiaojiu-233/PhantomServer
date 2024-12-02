package xj.interfaces;

// 连接处理器，为连接用动态线程池处理TCP连接的解决方案接口
// 实现该接口的类都能作为服务器处理TCP连接的解决方案程序
public interface ConnectHandler {

    // Request的内容是否满足处理条件
    boolean isMatchedRequest();

    // 处理相关内容
    void handle();

    // 返回对应的Response
    void returnResponse();
}
