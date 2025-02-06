package xj.implement.observer;

import xj.interfaces.observer.ObserverContainer;
import xj.interfaces.observer.SocketChannelObserver;

import java.util.ArrayList;
import java.util.List;

// socketChannel连接用观察者容器，用于管理装载socketChannel连接用观察者
public class SocketChannelObserverContainer<E> implements ObserverContainer<SocketChannelObserver<E>, E> {

    private final List<SocketChannelObserver<E>> observers = new ArrayList<>();

    @Override
    public void addObserver(SocketChannelObserver<E> observer) {
        observers.add(observer);
    }

    @Override
    public void delObserver(SocketChannelObserver<E> observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(E event) {
        for (SocketChannelObserver<E> ob : observers)
            ob.whenAccept(event);
    }
}
