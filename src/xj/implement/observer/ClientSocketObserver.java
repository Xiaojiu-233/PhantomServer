package xj.implement.observer;

import xj.interfaces.observer.SocketObserver;

// 客户端连接用socket观察者
public class ClientSocketObserver implements SocketObserver<String> {

    @Override
    public void whenAccept(String event) {

    }
}
