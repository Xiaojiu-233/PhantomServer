package xj.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 服务器MVC框架使用的服务注解，用于标注该类为服务层
 */
@EnableInject
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PService {
}
