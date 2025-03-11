package xj.core.extern.chat;

import sun.rmi.runtime.Log;
import xj.component.conf.ConfigureManager;
import xj.component.log.LogManager;
import xj.core.threadPool.factory.ThreadTaskFactory;
import xj.enums.web.ChatType;
import xj.implement.web.TCPChatRequest;
import xj.implement.web.TCPChatResponse;
import xj.tool.ConfigPool;
import xj.tool.StrPool;

import java.io.*;
import java.time.LocalDateTime;
import java.util.UUID;

// 聊天室管理器，用于提供TCP聊天室的解决方案
public class ChatManager {

    // 成员属性
    private static volatile ChatManager instance;// 单例模式实现

    private ChatObject[][] messageCache;// 聊天记录缓存块，0为最新缓存块

    private String[] cacheIds;// 缓存块标记

    private int pointer;// 消息指针，用于指明目前缓存块中最新的消息位置

    private String chatImagePath;// 消息图片存储路径

    private int cacheNum;// 消息缓存块数量

    private int cacheCapacity;// 消息缓存块容量

    // 成员方法
    // 初始化
    public ChatManager() {
        LogManager.info_("【聊天室模块】开始初始化");
        initMessageCache();
        LogManager.info_("【聊天室模块】初始化完成");
    }

    // 获取单例（防止高并发导致资源访问问题进行双判空保护）
    public static ChatManager getInstance() {
        if(instance == null)
            synchronized (ChatManager.class){
                if(instance == null)
                    instance = new ChatManager();
            }
        return instance;
    }

    // 初始化缓存块
    private void initMessageCache() {
        LogManager.info_("【聊天室模块】正在初始化消息缓存块...");
        // 获取配置参数
        cacheNum = (int) ConfigureManager.getInstance().getConfig(ConfigPool.CHAT.MESSAGE_CACHE_NUM);
        cacheCapacity = (int) ConfigureManager.getInstance().getConfig(ConfigPool.CHAT.CACHE_CAPACITY);
        chatImagePath = (String) ConfigureManager.getInstance().getConfig(ConfigPool.CHAT.CHAT_IMAGE_PATH);
        LogManager.info_("消息缓存块参数 -> 消息缓存块数量：{} 消息缓存块容量：{} 消息图片存储路径: {}",
                cacheNum, cacheCapacity, chatImagePath);
        // 开辟空间
        messageCache = new ChatObject[cacheNum][cacheCapacity];
        cacheIds = new String[cacheNum];
        cacheIds[0] = UUID.randomUUID().toString();
        // 创建图片存储文件夹
        File imagePath = new File(chatImagePath);
        if(!imagePath.exists())
            imagePath.mkdirs();
    }

    // 处理数据
    public TCPChatResponse handle(TCPChatRequest req) {
        TCPChatResponse response = new TCPChatResponse();
        // 获取消息对象和请求数据类型
        ChatObject ob = req.getChatObject();
        ChatType type = ob.getType();
        // 根据请求类型执行策略
        if(ChatType.MESSAGE.equals(type)) {
            if(storeMessage(ob))
                response.setMessage("文字消息接收成功");
            else{
                response.setResult(StrPool.FAILURE);
                response.setMessage("文字消息接收失败");
            }
        }else if(ChatType.IMAGE.equals(type)){
            if(storeImage(ob,req,response))
                response.setMessage("图片消息接收成功");
            else
                response = new TCPChatResponse(StrPool.FAILURE,"图片消息接收失败");
        }else if(ChatType.OFFSET.equals(type)){
            if(receiveMessage(req,response))
                response.setMessage("偏移量消息接收成功");
            else
                response = new TCPChatResponse(StrPool.FAILURE,"偏移量消息接收失败");
        }
        return response;
    }

    // 存储消息
    private boolean storeMessage(ChatObject chatObject) {
        synchronized (ChatManager.class) {
            try{
                // 查看容量是否满了
                if(pointer >= cacheCapacity) {
                    // 容量满了则开始调整缓存区
                    for(int i = cacheNum - 2; i >= 0; i--) {
                        cacheIds[i+1] = cacheIds[i];
                        messageCache[i+1] = messageCache[i];
                    }
                    // 更新最新的缓存区以及缓存开始时间
                    pointer = 0;
                    cacheIds[0] = UUID.randomUUID().toString();
                    messageCache[0] = new ChatObject[cacheCapacity];
                }
                // 将数据装入缓存区
                messageCache[0][pointer++] = chatObject;
                return true;
            } catch (Exception e) {
                LogManager.error_("聊天室模块在存储消息时出现异常",e);
                return false;
            }
        }

    }

    // 存储图片
    private boolean storeImage(ChatObject ob,TCPChatRequest req,TCPChatResponse resp) {
        // 随机生成UUID作为存储的图片key
        String key = UUID.randomUUID().toString() + StrPool.PNG_POINT;
        resp.setFileKey(key);
        // 将图片二进制数据转化为输入流并存储在线程任务中
        ByteArrayInputStream bis = new ByteArrayInputStream(req.getBodyBytes());
        resp.setStreamIOTask(ThreadTaskFactory.getInstance().createStreamOutputTask
                (bis, chatImagePath + StrPool.SLASH + key));
        // 将消息存储在消息缓存块里
        ob.setMessage(key);
        return storeMessage(ob);
    }

    // 拿取消息
    private boolean receiveMessage(TCPChatRequest req,TCPChatResponse resp) {
        try {
            synchronized (ChatManager.class) {
                // 寻找缓存块
                int cachePos = -1;
                int pos = req.getOffsetData().getOffset();
                String cacheUid = req.getOffsetData().getUid();
                if(cacheUid == null || cacheUid.isEmpty() || StrPool.NULL.equals(cacheUid)){
                    cachePos = 0;
                }else{
                    for(int i = 0; i < cacheNum; i++)
                        if(cacheIds[i] != null && cacheIds[i].equals(req.getOffsetData().getUid())) {
                            cachePos = i;
                            break;
                        }
                    if(cachePos == -1)
                        return false;
                }
                // 一直读取，直到读到null或者图片为止
                while(true) {
                    if(pos >= cacheCapacity){
                        pos %= cacheCapacity;
                        if(--cachePos < 0){
                            cachePos++;
                            pos = cacheCapacity;
                            break;
                        }
                    }
                    // 数据获取与装填
                    ChatObject ob = messageCache[cachePos][pos];
                    if(ob == null)
                        break;
                    else if(ChatType.IMAGE.equals(ob.getType())) {
                        resp.getObs().add(ob);
                        pos++;
                        // 开启线程任务
                        String filePath = chatImagePath + StrPool.SLASH + ob.getMessage();
                        FileInputStream fis = new FileInputStream(filePath);
                        resp.setStreamIOTask(ThreadTaskFactory.getInstance().createStreamInputTask(fis));
                        break;
                    }
                    else
                        resp.getObs().add(ob);
                    // 读取下一个数据
                    pos++;
                }
                // 设置偏移量数据并返回结果
                resp.setOffsetData(new OffsetData(pos,cacheIds[cachePos]));
            }
            return true;
        } catch (FileNotFoundException e) {
            LogManager.error_("聊天室模块在读取图片文件时出现异常",e);
            return false;
        } catch (Exception e) {
            LogManager.error_("聊天室模块在获取消息时出现异常",e);
            return false;
        }
    }

}
