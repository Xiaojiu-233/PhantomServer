package xj.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 服务器MVC框架使用的文件数据，用于标注该方法参数将被注入HTTP的请求文件数据<br>
 * 这个注解标注的方法参数需要在@PRequestMapping注解下的方法中才能使用
 * <h5>这个注解会给参数注入byte[]数据，还请注意！</h5>
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PUploadFile {
}
