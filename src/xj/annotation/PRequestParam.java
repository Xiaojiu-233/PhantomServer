package xj.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 服务器MVC框架使用的请求参数，用于标注该方法参数将被注入HTTP的请求参数<br>
 * 这个注解标注的方法参数需要在@PRequestMapping注解下的方法中才能使用
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PRequestParam {
}
