package xj.implement.observer;

import xj.interfaces.observer.SocketObserver;

import java.net.Socket;

// 服务器socket连接观察者
public class ServerSocketObserver implements SocketObserver<Socket> {

    @Override
    public void whenAccept(Socket event) {

    }
}
