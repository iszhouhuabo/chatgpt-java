package cn.online.chatools.services.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.UnicodeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.online.chatools.controllers.request.Message;
import cn.online.chatools.controllers.response.Resp;
import cn.online.chatools.domain.OpenAiConfig;
import cn.online.chatools.domain.SystemConfig;
import cn.online.chatools.services.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author louye
 */
@Service("chatGPTServiceImpl")
@Slf4j
public class ChatGPTServiceImpl implements ChatService {

    @Resource
    private OpenAiConfig openAiConfig;
    @Resource
    private SystemConfig systemConfig;

    @Override
    public Resp creditQuery(String key) {

        String apikey = openAiConfig.getApiKey();
        if (StrUtil.isNotBlank(key)) {
            apikey = key;
        }
        String result = HttpRequest.get(openAiConfig.getCreditApi())
                .header(Header.CONTENT_TYPE, "application/json")
                .header(Header.AUTHORIZATION, "Bearer " + apikey)
                .execute().body();
        if (result.contains("server_error")) {
            throw new RuntimeException("请求ChatGPT官方服务器出错");
        }
        JSONObject jsonObject = JSONUtil.parseObj(result);
        // 返回结果
        return Resp.ok().data(jsonObject.getStr("total_available"));
    }

    private void genImage(Message message, String key, Consumer<String> send) {
        // 请求参数
        Map<String, String> userMessage = MapUtil.of(
                "size", "512x512"
        );
        userMessage.put("prompt", message.getMessage().get(0));

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
            send.accept(m.group(1));
        } else {
            send.accept("图片生成失败！");
        }
    }


    @Override
    public void sendResponse(Message message, Consumer<String> send) throws IOException {
        String key = systemConfig.getFreeApiKey();
        if (StringUtils.isNoneBlank(message.getApiKey())) {
            key = message.getApiKey();
        }
        if (StringUtils.isBlank(key)) {
            send.accept(systemConfig.getNotAuthContent());
            return;
        }
        if (Objects.equals(message.getType(), Message.MessageType.IMAGE)) {
            genImage(message, key, send);
            return;
        }

        // 构建对话参数
        List<Map<String, String>> messages = message.getMessage().stream().map(msg -> {
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
            send.accept(String.join("", "发生不可预计的错误", e.getMessage()));
            send.accept("END");
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
                send.accept(data);
            }
        }
        // 关闭流
        reader.close();
        // 如果出错，抛出异常
        if (printErrorMsg) {
            send.accept(errMsg.toString());
            send.accept("END");
        }
        send.accept("END");
    }
}
