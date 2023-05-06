package com.github.iszhouhuabo.function;

/**
 * @author louye
 */
@FunctionalInterface
public interface HttpSend<T> {
    /**
     * 向 web 客户端发送消息
     *
     * @param t 消息
     */
    void sendToClient(T t);
}
