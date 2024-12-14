package xj.implement.observer;

import xj.interfaces.observer.ObserverContainer;
import xj.interfaces.observer.SocketObserver;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

// socket连接用观察者容器，用于管理装载socket连接用观察者
public class SocketObserverContainer<E> implements ObserverContainer<SocketObserver<E>, E> {

    private final List<SocketObserver<E>> observers = new ArrayList<>();

    @Override
    public void addObserver(SocketObserver<E> observer) {
        observers.add(observer);
    }

    @Override
    public void delObserver(SocketObserver<E> observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(E event) {
        for (SocketObserver<E> ob : observers)
            ob.whenAccept(event);
    }
}
