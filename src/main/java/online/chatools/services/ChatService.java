package online.chatools.services;

import online.chatools.controllers.request.WebMessage;
import online.chatools.domain.common.Dic;
import online.chatools.function.HttpSend;

import java.io.IOException;

/**
 * @author louye
 */
public interface ChatService {
    /**
     * 发送消息
     *
     * @param webMessage 消息
     * @param send       回调方法
     * @throws IOException 异常
     */
    void sendResponse(WebMessage webMessage, HttpSend<String> send) throws IOException;

    /**
     * 发送消息 通过 sse
     *
     * @param webMessage 消息
     * @param send       回调方法
     */
    default void sendResponseBySse(WebMessage webMessage, HttpSend<String> send) {
        try {
            sendResponse(webMessage, send);
        } catch (Exception e) {
            send.sendToClient(Dic.SEND_END);
        }
    }
}
