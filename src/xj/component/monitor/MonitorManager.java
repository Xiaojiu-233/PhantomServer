package xj.component.monitor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import xj.abstracts.web.Request;
import xj.component.conf.ConfigureManager;
import xj.component.log.LogManager;
import xj.core.extern.IOCManager;
import xj.enums.web.CharacterEncoding;
import xj.enums.web.ContentType;
import xj.enums.web.RequestMethod;
import xj.enums.web.StatuCode;
import xj.implement.monitor.*;
import xj.implement.web.HTTPRequest;
import xj.implement.web.HTTPResponse;
import xj.interfaces.component.MonitorPanel;
import xj.tool.ConfigPool;
import xj.tool.FileIOUtil;
import xj.tool.StrPool;

import java.io.IOException;
import java.util.*;

// 可视化界面管理器，用于为用户提供可视化的服务器监控管理与健康检查系统
public class MonitorManager {

    // 成员属性
    private static volatile MonitorManager instance;// 单例模式实现

    private final Map<String,MonitorPanel> panelMapping;// 可视化界面映射

    private Set<String> allowIpList;// 请求IP白名单

    private String monitorWebPath;// 可视化界面web请求根路径

    private String monitorIndex;// 可视化界面主页

    // 成员方法
    // 初始化
    public MonitorManager() {
        LogManager.info_("【可视化界面模块】开始初始化");
        // 成员属性设置
        panelMapping = new HashMap<>();
        // 读取基础配置
        monitorWebPath = (String) ConfigureManager.getInstance().getConfig(ConfigPool.MONITOR.WEB_PATH);
        monitorIndex = (String)ConfigureManager.getInstance().getConfig(ConfigPool.MONITOR.INDEX_PATH);
        allowIpList = new HashSet<>((List<String>) ConfigureManager.getInstance().getConfig(ConfigPool.MONITOR.ALLOW_IPS));
        allowIpList.add(StrPool.LOCAL_ADDRESS_IPV4);
        allowIpList.add(StrPool.LOCAL_ADDRESS_IPV6);
        LogManager.info_("可视化界面参数 -> Web请求根路径：{} 界面主页：{}", monitorWebPath,monitorIndex);
        // 读取本地界面对象
        panelMapping.put(WebMonitorPanel.class.getSimpleName(), new WebMonitorPanel());
        panelMapping.put(ThreadPoolMonitorPanel.class.getSimpleName(), new ThreadPoolMonitorPanel());
        panelMapping.put(IOCLogMonitorPanel.class.getSimpleName(), new IOCLogMonitorPanel());
        panelMapping.put(SystemMonitorPanel.class.getSimpleName(), new SystemMonitorPanel());
        panelMapping.put(MVCChatMonitorPanel.class.getSimpleName(), new MVCChatMonitorPanel());
        // 读取拓展界面对象
        List<Object> extPanels = IOCManager.getInstance().returnImplInstancesByClass(MonitorPanel.class);
        for(Object panel : extPanels){
            panelMapping.put(panel.getClass().getSimpleName(), (MonitorPanel) panel);
        }
        LogManager.info_("【可视化界面模块】初始化完成");
    }

    // 获取单例（防止高并发导致资源访问问题进行双判空保护）
    public static MonitorManager getInstance() {
        if(instance == null)
            synchronized (MonitorManager.class){
                if(instance == null){
                    // 判定是否满足可视化条件
                    if(!(Boolean)ConfigureManager.getInstance().getConfig(ConfigPool.MONITOR.ENABLE)){
                        LogManager.warn_("可视化界面 并未在配置文件中开启，无法实现可视化界面模块");
                        return null;
                    }
                    // 满足则进行可视化模组实现
                    instance = new MonitorManager();
                }
            }
        return instance;
    }

    // 当满足条件时，处理web请求
    public HTTPResponse handle(HTTPRequest req) {
        // 如果请求IP为通过白名单，则不能被可视化界面模块处理
        if (!allowIpList.contains(req.getRemoteIp()))
            return null;
        // 得到请求对象，并拆分URL
        String[] splitUrl = req.getUrl().substring(1).split(StrPool.SLASH);
        // 如果第一节为请求不为根路径或者没有第一节，则直接返回失败
        if (splitUrl.length == 0 || !monitorWebPath.equals(splitUrl[0]))
            return null;
        // 如果只有一节，则返回可视化界面主页路径
        if (splitUrl.length == 1){
            // 在服务器资源目录中搜索
            HTTPResponse resp = new HTTPResponse(StatuCode.OK, CharacterEncoding.UTF_8,
                    FileIOUtil.getFileContent(monitorIndex));
            resp.setHeaders(StrPool.CONTENT_TYPE, ContentType.TEXT_HTML.contentType);
            return resp;
        }
        // 如果只有两节
        if (splitUrl.length == 2){
            // 如果第二节为list且请求方法为GET，则返回所有的子界面列表
            if(RequestMethod.POST.equals(req.getMethod()) && StrPool.LIST.equals(splitUrl[1])){
                List<MonitorPanelInfo> panelInfos = new ArrayList<>();
                for(Map.Entry<String,MonitorPanel> panel : panelMapping.entrySet())
                    panelInfos.add(new MonitorPanelInfo(panel.getValue().returnTitle(),panel.getKey()));
                return returnJsonHttpResponse(panelInfos);
            }
            // 如果请求方法为POST并且寻找到了对应的可视化界面，则将对应的界面路径传给请求对象后交给MVC处理
            if(RequestMethod.POST.equals(req.getMethod())){
                MonitorPanel panel = panelMapping.get(splitUrl[1]);
                if(panel != null){
                    req.setUrl(panel.returnWebpagePath());
                    return null;
                }
            }
        }
        // 如果有三节，则寻找对应方法并做相应处理
        if (splitUrl.length == 3 && RequestMethod.POST.equals(req.getMethod())){
            // 判定是否找到对应的可视化界面
            MonitorPanel panel = panelMapping.get(splitUrl[1]);
            if(panel == null)
                return null;
            // 解析获取的JSON数据，转化为Map对象
            Map<String,Object> jsonParam = new HashMap<>();
            Map<String,Object> ret = new HashMap<>();
            if(req.getBodyBytes().length > 0){
                try {
                    jsonParam = new ObjectMapper().readValue
                            (new String(req.getBodyBytes()),new TypeReference<Map<String, Object>>(){});
                } catch (IOException e) {
                    LogManager.error_("可视化界面模块在将请求数据转化为json的时候出现错误",e);
                    throw new RuntimeException(e);
                }
            }
            // 根据请求的内容返回对应结果
            if (StrPool.GET_DATA.equals(splitUrl[2])){
                ret = panel.getData(jsonParam);
            }else if (StrPool.SET_DATA.equals(splitUrl[2])){
                panel.setData(jsonParam);
                ret.put("data","数据传入成功");
            }
            return returnJsonHttpResponse(ret);
        }
        return null;
    }

    // 返回携带JSON的HTTP响应对象
    private HTTPResponse returnJsonHttpResponse(Object ob) {
        HTTPResponse resp = null;
        try {
            resp = new HTTPResponse(StatuCode.OK, CharacterEncoding.UTF_8,
                    new ObjectMapper().writeValueAsBytes(ob));
            resp.setHeaders(StrPool.CONTENT_TYPE, ContentType.TEXT_HTML.contentType);
            return resp;
        } catch (JsonProcessingException e) {
            LogManager.error_("可视化界面模块在解析对象为JSON时出现异常",e);
        }
        return resp;
    }
}
