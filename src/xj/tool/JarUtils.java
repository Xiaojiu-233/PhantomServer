package xj.tool;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;

// jar包处理工具包
public class JarUtils{

    // 读取JAR包所有类名
    public static List<String> getClassNames(Enumeration<JarEntry> entries) {
        List<String> classNames = new ArrayList<String>();
        while (entries.hasMoreElements()) {
            JarEntry nextElement = entries.nextElement();
            String name = nextElement.getName();
            // 这个获取的就是一个实体类class java.util.jar.JarFile$JarFileEntry
            // Class<? extends JarEntry> class1 = nextElement.getClass();
            System.out.println("entry name=" + name);
            // 这样就获取所有的jar中的class文件

            // 加载某个class文件，并实现动态运行某个class
            if (name.endsWith(".class")) {
                String replace = name.replace(".class", "").replace("/", ".");
                classNames.add(replace);
            }
        }
        return classNames;
    }
}