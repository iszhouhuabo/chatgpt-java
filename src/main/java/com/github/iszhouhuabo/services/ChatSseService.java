package com.github.iszhouhuabo.services;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.github.iszhouhuabo.configrations.LocalCache;
import com.github.iszhouhuabo.domain.SystemConfig;
import com.github.iszhouhuabo.listener.OpenAiChatStreamSseListener;
import com.github.iszhouhuabo.web.request.ChatRequest;
import com.github.iszhouhuabo.web.response.ChatResponse;
import com.github.iszhouhuabo.web.response.Resp;
import com.unfbx.chatgpt.OpenAiStreamClient;
import com.unfbx.chatgpt.entity.chat.ChatCompletion;
import com.unfbx.chatgpt.entity.chat.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.github.iszhouhuabo.domain.common.SystemDic.CACHE_KEY_MSG_PRE;

/**
 * 通过 sse 实现交流
 *
 * @author louye
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ChatSseService {

    private final SystemConfig systemConfig;
    private final OpenAiStreamClient openAiStreamClient;

    public SseEmitter createSse(String uid) {
        //默认30秒超时,设置为0L则永不超时
        SseEmitter sseEmitter = new SseEmitter(0L);
        //完成后回调
        sseEmitter.onCompletion(() -> {
            log.info("[{}]结束连接...................", uid);
            LocalCache.CACHE.remove(uid);
        });
        //超时回调
        sseEmitter.onTimeout(() -> {
            log.info("[{}]连接超时...................", uid);
        });
        //异常回调
        sseEmitter.onError(
                throwable -> {
                    try {
                        log.info("[{}]连接异常,{}", uid, throwable.toString());
                        sseEmitter.send(SseEmitter.event()
                                .id(uid)
                                .name("发生异常！")
                                .data(Message.builder().content("发生异常请重试！").build())
                                .reconnectTime(3000));
                        LocalCache.CACHE.put(uid, sseEmitter);
                    } catch (IOException e) {
                        log.error("消息发送失败！", e);
                    }
                }
        );
        try {
            sseEmitter.send(SseEmitter.event().reconnectTime(5000));
        } catch (IOException e) {
            log.error("消息发送失败！", e);
        }
        LocalCache.CACHE.put(uid, sseEmitter);
        log.info("[{}]创建sse连接成功！", uid);
        return sseEmitter;
    }

    public Resp closeSse(String uid) {
        SseEmitter sse = (SseEmitter) LocalCache.CACHE.get(uid);
        if (sse != null) {
            sse.complete();
            //移除
            LocalCache.CACHE.remove(uid);
        }
        return Resp.ok();
    }

    public Resp sseChat(String uid, ChatRequest chatRequest) {
        String messageContext = (String) LocalCache.CACHE.get(CACHE_KEY_MSG_PRE + uid);
        List<Message> messages = new ArrayList<>();
        if (StrUtil.isNotBlank(messageContext)) {
            messages = JSONUtil.toList(messageContext, Message.class);
            if (messages.size() >= systemConfig.getContextNum()) {
                messages = messages.subList(1, systemConfig.getContextNum());
            }
            Message currentMessage = Message.builder().content(chatRequest.getMessage()).role(Message.Role.USER).build();
            messages.add(currentMessage);
        } else {
            Message currentMessage = Message.builder().content(chatRequest.getMessage()).role(Message.Role.USER).build();
            messages.add(currentMessage);
        }

        SseEmitter sseEmitter = (SseEmitter) LocalCache.CACHE.get(uid);
        if (sseEmitter == null) {
            log.warn("聊天消息推送失败uid:[{}],没有创建连接，请重试。", uid);
            return Resp.fail().msg("聊天连接未创建, 请先创建连接!");
        }
        OpenAiChatStreamSseListener openAiChatStreamSseListener = new OpenAiChatStreamSseListener(sseEmitter);
        ChatCompletion completion = ChatCompletion
                .builder()
                .messages(messages)
                .model(ChatCompletion.Model.GPT_3_5_TURBO.getName())
                .build();
        openAiStreamClient.streamChatCompletion(completion, openAiChatStreamSseListener);
        LocalCache.CACHE.put(CACHE_KEY_MSG_PRE + uid, JSONUtil.toJsonStr(messages), LocalCache.TIMEOUT);
        return Resp.ok().data(ChatResponse.builder().questionTokens(completion.tokens()));
    }
}
