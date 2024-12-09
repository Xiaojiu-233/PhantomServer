package xj.interfaces.observer;

// socket连接用观察者，用于处理对socket连接的监听 E为事件数据
public interface SocketObserver<E> {

    // 监听触发时执行方法
    public void whenAccept(E event);
}
