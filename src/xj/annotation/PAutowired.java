package xj.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 服务器MVC框架使用的依赖注入，用于标注该参数可被IOC容器识别并注入对应类的对象<br>
 * 这个注解标注的参数需要在@PController、@PService等注解下的类中才能使用
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PAutowired {
}
