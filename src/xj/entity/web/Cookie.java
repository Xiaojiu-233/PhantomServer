package xj.entity.web;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * HTTP协议请求和响应使用的Cookie对象，用于存储和使用Cookie
 */
public class Cookie implements Serializable {

    // 成员属性
    private String name;// 名称

    private String value;// 值

    private String domain;// 域

    private String path;// 路径

    private String expires;// 过期时间

    private Integer maxAge;// 过期时限（秒）

    private boolean secure;// 安全性

    private boolean httpOnly;// 是否禁止JS访问

    // 成员方法
    /**
     * 初始化
     */
    public Cookie(String name, String value, String domain, String path,
                  int expireSeconds, Integer maxAgeSeconds, boolean secure, boolean httpOnly) {
        this.name = name;
        this.value = value;
        this.domain = domain;
        this.path = path;
        this.expires = getHandledExpires(expireSeconds);
        this.maxAge = maxAgeSeconds;
        this.secure = secure;
        this.httpOnly = httpOnly;
    }
    public Cookie(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * 变为字符串形式
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("=").append(value).append("; ");
        if (domain != null)
            sb.append("Domain=").append(domain).append("; ");
        if (path != null)
            sb.append("Path=").append(path).append("; ");
        if (expires != null)
            sb.append("Expires=").append(expires).append("; ");
        if (maxAge != null){
            maxAge = maxAge > -1 ? maxAge : -1 ;
            sb.append("Max-Age=").append(maxAge).append("; ");
        }
        if (secure)
            sb.append("Secure=true").append("; ");
        if (httpOnly)
            sb.append("HttpOnly=true").append("; ");
        return sb.toString();
    }

    /**
       通过字符串获取Cookie列表
     */
    public static List<Cookie> getCookies(String source){
        String[] cookies = source.split("; ");
        List<Cookie> cookiesList = new ArrayList<Cookie>();
        for(String cookie : cookies){
            String[] cookieParts = cookie.split("=");
            if(cookieParts.length == 2)
                cookiesList.add(new Cookie(cookieParts[0], cookieParts[1]));
        }
        return cookiesList;
    }

    /**
     * 过期时间特殊处理
     */
    private String getHandledExpires(int seconds){
        long time = System.currentTimeMillis() + (long) seconds * 1000;
        Date date = new Date(time);
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(date);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getExpires() {
        return expires;
    }

    public void setExpires(int expireSeconds) {
        this.expires = getHandledExpires(expireSeconds);
    }

    public Integer getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(Integer maxAgeSeconds) {
        this.maxAge = maxAgeSeconds;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public boolean isHttpOnly() {
        return httpOnly;
    }

    public void setHttpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
    }
}
