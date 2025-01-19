package xj.interfaces.observer;

/**
 * 观察者模式的装载容器，用于管理与装载观察者 T为观察者 E为事件数据
 */
public interface ObserverContainer<T,E> {

    /**
     * 装载观察者
     */
    public void addObserver(T observer);

    /**
     * 卸载观察者
     */
    public void delObserver(T observer);

    /**
     * 用事件E，呼叫所有观察者
     */
    public void notifyObservers(E event);
}
