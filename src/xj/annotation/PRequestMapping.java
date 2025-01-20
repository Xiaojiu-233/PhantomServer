package xj.annotation;

import xj.enums.web.RequestMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 服务器MVC框架使用的请求映射方法，用于标注该方法可用于对指定路径的请求进行处理<br>
 * 这个注解标注的方法需要在@PController注解下的类中才能使用
 */
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PRequestMapping {

    String value() default "";
    RequestMethod method() default RequestMethod.GET;
}
