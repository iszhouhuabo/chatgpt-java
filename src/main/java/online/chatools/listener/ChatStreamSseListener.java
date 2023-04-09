package online.chatools.listener;

import cn.hutool.json.JSONUtil;
import com.unfbx.chatgpt.entity.chat.ChatCompletionResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import online.chatools.domain.common.Dic;
import online.chatools.function.HttpSend;
import online.chatools.utils.MessageUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;

/**
 * @author louye
 */
@Slf4j
public class ChatStreamSseListener extends EventSourceListener {

    private final HttpSend<String> httpSend;
    private final CountDownLatch countDownLatch;
    private final StringBuilder errorMessage;

    public ChatStreamSseListener(HttpSend<String> httpSend, CountDownLatch countDownLatch) {
        this.httpSend = httpSend;
        this.countDownLatch = countDownLatch;
        this.errorMessage = new StringBuilder();
    }

    @Override
    public void onEvent(@NotNull EventSource eventSource, String id, String type, String data) {
        if (data.equals(Dic.GPT_MSG_END)) {
            httpSend.sendToClient(Dic.SEND_END);
            countDownLatch.countDown();
            return;
        }
        if (data.contains("error") || errorMessage.length() > 0) {
            // 发生错误，不输出
            errorMessage.append(data);
        }
        if (data.contains("content")) {
            ChatCompletionResponse respMessage = JSONUtil.toBean(data, ChatCompletionResponse.class);
            httpSend.sendToClient(respMessage.getChoices().get(0).getDelta().getContent());
        }
    }

    @SneakyThrows
    @Override
    public void onFailure(@NotNull EventSource eventSource, Throwable t, Response response) {
        if (Objects.isNull(response)) {
            log.error("OpenAI sse连接异常:{}", t.getMessage(), t);
            eventSource.cancel();
            httpSend.sendToClient("GPT 通信出现异常!");
            httpSend.sendToClient(Dic.SEND_END);
            return;
        }
        ResponseBody body = response.body();
        if (Objects.nonNull(body)) {
            log.error("OpenAI sse连接异常 Data：{}，异常：{}", body.string(), t.getMessage());
            httpSend.sendToClient("GPT 通信出现异常! \n" + body);
        } else {
            log.error("OpenAI  sse连接异常 Data：{}，异常：{}", response, t.getMessage());
            httpSend.sendToClient("GPT 通信出现异常! \n" + t.getMessage());
        }
        httpSend.sendToClient(Dic.SEND_END);
        eventSource.cancel();
    }

    @Override
    public void onClosed(@NotNull EventSource eventSource) {
        if (errorMessage.length() > 0) {
            // 有错误消息
            httpSend.sendToClient(MessageUtils.handErrorMsg(errorMessage.toString()));
        }
        httpSend.sendToClient(Dic.SEND_END);
        eventSource.cancel();
    }
}

