package xj.interfaces.ioc;

/**
 * 实例的初始化接口，为需要进行初始化操作的实例提供安全初始化的接口接口规范
 */
public interface Instantiation {

    /**
     * 初始化
     */
    void instantiate();
}
