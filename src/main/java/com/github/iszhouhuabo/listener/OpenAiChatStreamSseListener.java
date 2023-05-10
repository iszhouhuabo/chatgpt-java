package com.github.iszhouhuabo.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unfbx.chatgpt.entity.chat.ChatCompletionResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Objects;

import static com.github.iszhouhuabo.domain.common.SystemDic.GPT_MSG_END;

/**
 * @author louye
 */
@Slf4j
@RequiredArgsConstructor
public class OpenAiChatStreamSseListener extends EventSourceListener {

    private long tokens;

    private SseEmitter sseEmitter;

    public OpenAiChatStreamSseListener(SseEmitter sseEmitter) {
        this.sseEmitter = sseEmitter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onOpen(@NotNull EventSource eventSource, @NotNull Response response) {
        log.debug("OpenAI建立sse连接...");
    }

    /**
     * 接到消息
     */
    @SneakyThrows
    @Override
    public void onEvent(@NotNull EventSource eventSource, String id, String type, @NotNull String data) {
        log.info(data);
        tokens += 1;
        if (GPT_MSG_END.equals(data)) {
            sseEmitter.send(SseEmitter.event()
                    .id("[TOKENS]")
                    .data(tokens())
                    .reconnectTime(3000));
            sseEmitter.send(SseEmitter.event()
                    .id(GPT_MSG_END)
                    .data(GPT_MSG_END)
                    .reconnectTime(3000));
            // 传输完成后自动关闭sse
            sseEmitter.complete();
            return;
        }
        ObjectMapper mapper = new ObjectMapper();
        ChatCompletionResponse completionResponse = mapper.readValue(data, ChatCompletionResponse.class); // 读取Json
        try {
            sseEmitter.send(SseEmitter.event()
                    .id(completionResponse.getId())
                    .data(completionResponse.getChoices().get(0).getDelta())
                    .reconnectTime(3000));
        } catch (Exception e) {
            log.error("sse信息推送失败", e);
            eventSource.cancel();
        }
    }


    @Override
    public void onClosed(@NotNull EventSource eventSource) {
        log.info("流式输出返回值总共{}tokens", tokens() - 2);
        log.debug("OpenAI关闭sse连接...");
    }


    @SneakyThrows
    @Override
    public void onFailure(@NotNull EventSource eventSource, Throwable t, Response response) {
        if (Objects.isNull(response)) {
            return;
        }
        ResponseBody body = response.body();
        if (Objects.nonNull(body)) {
            log.error("OpenAI sse连接异常data：{}，异常：{}", body.string(), t.getMessage());
        } else {
            log.error("OpenAI sse连接异常data：{}，异常：{}", response, t.getMessage());
        }
        eventSource.cancel();
    }

    /**
     * tokens
     *
     * @return token
     */
    public long tokens() {
        return tokens;
    }
}

