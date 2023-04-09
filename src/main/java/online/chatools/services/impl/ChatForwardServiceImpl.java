package online.chatools.services.impl;

import cn.hutool.core.text.UnicodeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import online.chatools.controllers.request.WebMessage;
import online.chatools.domain.SystemConfig;
import online.chatools.function.HttpSend;
import online.chatools.services.ChatService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

/**
 * @author louye
 */
@Service("chatForwardServiceImpl")
@Slf4j
public class ChatForwardServiceImpl implements ChatService {
    @Resource
    private SystemConfig systemConfig;

    @Override
    public void sendResponse(WebMessage webMessage, HttpSend<String> send) throws IOException {
        SystemConfig.ForwardInfo proxyInfo = systemConfig.getForward();
        if (StringUtils.isBlank(proxyInfo.getApiKey())) {
            send.sendToClient("未配置代理 ApiKey, 请联系管理员!");
            return;
        }
        if (Objects.equals(webMessage.getType(), WebMessage.MessageType.IMAGE)) {
            send.sendToClient("代理模式暂时不支持图片问答模式! 敬请期待~");
            return;
        }
        webMessage.setApiKey(proxyInfo.getApiKey());
        // 调用接口
        HttpResponse result;
        try {
            result = HttpRequest.post(proxyInfo.getHttp())
                    .header(Header.CONTENT_TYPE, "application/json")
                    .body(JSONUtil.toJsonStr(webMessage))
                    .executeAsync();
        } catch (Exception e) {
            send.sendToClient(String.join("", "发生不可预计的错误", e.getMessage()));
            send.sendToClient("END");
            return;
        }

        String line;
        send.sendToClient("[系统消息]请稍后，消息正在读取中...\n\n");
        assert result != null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(result.bodyStream()));

        while ((line = reader.readLine()) != null) {
            String msgResult = UnicodeUtil.toString(line);
            send.sendToClient(msgResult + "\n");
        }
        // 关闭流
        reader.close();
        send.sendToClient("END");
    }

    @Override
    public void sendResponseBySse(WebMessage webMessage, HttpSend<String> send) {

        if (StrUtil.isNotBlank(webMessage.getApiKey())) {
            // 自定义 key

        }
    }
}
