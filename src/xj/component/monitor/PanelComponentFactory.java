package xj.component.monitor;

import xj.component.conf.ConfigureManager;
import xj.component.log.LogManager;
import xj.tool.ConfigPool;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

// 可视化界面组件工厂，能快捷生成相关组件
public class PanelComponentFactory {

    // 成员属性
    private static volatile PanelComponentFactory instance;// 单例模式实现

    // 成员方法
    // 初始化
    public PanelComponentFactory() {
        LogManager.info_("界面组件工厂开始初始化");
        LogManager.info_("界面组件工厂初始化完成");
    }

    // 获取单例（防止高并发导致资源访问问题进行双判空保护）
    public static PanelComponentFactory getInstance() {
        if(instance == null)
            synchronized (PanelComponentFactory.class){
                if(instance == null)
                    instance = new PanelComponentFactory();
            }
        return instance;
    }

    // 创造参数文本单元
    public JTextArea createArgTextUnit(String text){
        JTextArea textArea = new JTextArea(text);
        textArea.setLineWrap(true); // 启用自动换行
        textArea.setWrapStyleWord(true); // 只在单词边界换行
        textArea.setEditable(false); // 如果不需要编辑，设置为不可编辑
        textArea.setBackground(Color.white);
        textArea.setBorder(BorderFactory.createEmptyBorder());
        textArea.setFont(new Font("雅黑宋体", Font.PLAIN, 18));
        return textArea;
    }
}
