package xj.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 导入服务器组件的注解，只能注解在类上，可以让系统为该类对象注入服务器组件
// 服务器组件：如LogManager、ConfigureManager……
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ComponentImport {
}
