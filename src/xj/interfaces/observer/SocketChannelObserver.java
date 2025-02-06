package xj.interfaces.observer;

/**
 * socketChannel连接用观察者，用于处理对socket连接的监听 E为事件数据
 */
public interface SocketChannelObserver<E> {

    /**
     * 监听触发时执行方法
     */
    public void whenAccept(E event);
}
