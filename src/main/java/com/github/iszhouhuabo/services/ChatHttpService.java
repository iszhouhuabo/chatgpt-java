package com.github.iszhouhuabo.services;

import cn.hutool.core.util.StrUtil;
import com.github.iszhouhuabo.listener.AbstractStreamListener;
import com.github.iszhouhuabo.web.request.ChatRequest;
import com.unfbx.chatgpt.OpenAiStreamClient;
import com.unfbx.chatgpt.entity.chat.ChatCompletion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

/**
 * @author louye
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatHttpService {
    private final OpenAiStreamClient openAiStreamClient;

    public void sendResponse(ChatRequest chatRequest, Consumer<String> send) {
        ChatCompletion chatCompletion = ChatCompletion
                .builder()
                .messages(chatRequest.getMessage())
                .model(StrUtil.isBlank(chatRequest.getModel()) ? ChatCompletion.Model.GPT_3_5_TURBO.getName() : chatRequest.getModel())
                .build();
        CountDownLatch countDownLatch = new CountDownLatch(1);

        AbstractStreamListener listener = new AbstractStreamListener() {
            @Override
            public void onMsg(String message) {
                send.accept(message);
            }

            @Override
            public void onError(Throwable throwable, String response) {
                send.accept("聊天出现异常,请稍后再尝试!" + throwable);
                countDownLatch.countDown();
            }
        };
        listener.setOnComplete((rep) -> countDownLatch.countDown());

        this.openAiStreamClient.streamChatCompletion(chatCompletion, listener);
        try {
            // hold 连接,让消息发完
            countDownLatch.await();
        } catch (InterruptedException e) {
            log.error("await 异常", e);
        }
    }
}
