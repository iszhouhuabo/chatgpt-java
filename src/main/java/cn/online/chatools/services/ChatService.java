package cn.online.chatools.services;

import cn.online.chatools.controllers.request.Message;
import cn.online.chatools.controllers.response.Resp;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * @author louye
 */
public interface ChatService {
    /**
     * 查询余额
     *
     * @param key api key
     * @return rep
     */
    default Resp creditQuery(String key) {
        return Resp.fail().msg("功能正在开发中...");
    }

    /**
     * 发送消息
     *
     * @param message 消息
     * @param send    回调方法
     * @throws IOException 异常
     */
    void sendResponse(Message message, Consumer<String> send) throws IOException;
}
