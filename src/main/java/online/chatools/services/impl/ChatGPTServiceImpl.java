package online.chatools.services.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.UnicodeUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.unfbx.chatgpt.OpenAiStreamClient;
import com.unfbx.chatgpt.entity.chat.Message;
import com.unfbx.chatgpt.function.KeyRandomStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import online.chatools.controllers.request.WebMessage;
import online.chatools.domain.OpenAiConfig;
import online.chatools.domain.SystemConfig;
import online.chatools.domain.common.Dic;
import online.chatools.function.HttpSend;
import online.chatools.listener.ChatStreamSseListener;
import online.chatools.services.ChatService;
import online.chatools.utils.KeyGetUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author louye
 */
@Service("chatGPTServiceImpl")
@Slf4j
@RequiredArgsConstructor
public class ChatGPTServiceImpl implements ChatService {
    private final OpenAiConfig openAiConfig;
    private final SystemConfig systemConfig;
    private final OkHttpClient okHttpClient;

    private void genImage(WebMessage webMessage, String key, HttpSend<String> send) {
        // 请求参数
        Map<String, String> userMessage = MapUtil.of(
                "size", "512x512"
        );
        userMessage.put("prompt", webMessage.getMessage().get(0));

        // 调用接口
        String result = HttpRequest.post(openAiConfig.getImageApi())
                .header(Header.CONTENT_TYPE, "application/json")
                .header(Header.AUTHORIZATION, "Bearer " + key)
                .body(JSONUtil.toJsonStr(userMessage))
                .execute().body();
        // 正则匹配出结果
        Pattern p = Pattern.compile("\"url\": \"(.*?)\"");
        Matcher m = p.matcher(result);
        if (m.find()) {
            send.sendToClient(m.group(1));
        } else {
            send.sendToClient("图片生成失败！");
        }
    }


    /**
     * 建议使用 {@link #sendResponseBySse(WebMessage, HttpSend)}
     */
    @Deprecated
    @Override
    public void sendResponse(WebMessage webMessage, HttpSend<String> send) throws IOException {
        String key = systemConfig.getFreeApiKey();
        if (StringUtils.isNoneBlank(webMessage.getApiKey())) {
            key = webMessage.getApiKey();
        }
        if (StringUtils.isBlank(key)) {
            send.sendToClient("请配置有效的 APIKEY, 谢谢~");
            return;
        }
        if (Objects.equals(webMessage.getType(), WebMessage.MessageType.IMAGE)) {
            genImage(webMessage, key, send);
            return;
        }

        // 构建对话参数
        List<Map<String, String>> messages = webMessage.getMessage().stream().map(msg -> {
            Map<String, String> userMessage = MapUtil.of(
                    "role", "user"
            );
            userMessage.put("content", msg);
            return userMessage;
        }).collect(Collectors.toList());


        // 构建请求参数
        HashMap<Object, Object> params = new HashMap<>();
        params.put("stream", true);
        params.put("model", openAiConfig.getModel());
        params.put("messages", messages);

        // 调用接口
        HttpResponse result;
        try {
            result = HttpRequest.post(openAiConfig.getOpenaiApi())
                    .header(Header.CONTENT_TYPE, "application/json")
                    .header(Header.AUTHORIZATION, "Bearer " + key)
                    .body(JSONUtil.toJsonStr(params))
                    .executeAsync();
        } catch (Exception e) {
            send.sendToClient(String.join("", "发生不可预计的错误", e.getMessage()));
            send.sendToClient("END");
            return;
        }
        // 处理数据
        String line;
        assert result != null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(result.bodyStream()));
        boolean printErrorMsg = false;
        StringBuilder errMsg = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            String msgResult = UnicodeUtil.toString(line);

            // 正则匹配错误信息
            if (msgResult.contains("\"error\":")) {
                printErrorMsg = true;
            }
            // 如果出错，打印错误信息
            if (printErrorMsg) {
                errMsg.append(msgResult);
            } else if (msgResult.contains("content")) {
                String data = JSONUtil.parseObj(line.substring(5)).getByPath("choices[0].delta.content").toString();
                send.sendToClient(data);
            }
        }
        // 关闭流
        reader.close();
        // 如果出错，抛出异常
        if (printErrorMsg) {
            send.sendToClient(errMsg.toString());
            send.sendToClient("END");
        }
        send.sendToClient("END");
    }

    @Override
    public void sendResponseBySse(WebMessage webMessage, HttpSend<String> send) {
        OpenAiStreamClient client = OpenAiStreamClient.builder()
                .apiKey(new KeyGetUtils(systemConfig, webMessage.getApiKey()).doFile())
                .keyStrategy(new KeyRandomStrategy())
                .okHttpClient(okHttpClient)
                .build();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.streamChatCompletion(
                webMessage.getMessage().stream().map(e ->
                        Message.builder()
                                .role(Message.Role.USER)
                                .content(e)
                                .build()).collect(Collectors.toList()), new ChatStreamSseListener(send, countDownLatch));
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            log.error("等待AI数据回写失败!", e);
            send.sendToClient(Dic.SEND_END);
        }
    }
}
