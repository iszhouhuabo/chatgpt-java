package cn.online.chatools.services.impl;

import cn.hutool.core.text.UnicodeUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import cn.online.chatools.controllers.request.Message;
import cn.online.chatools.domain.SystemConfig;
import cn.online.chatools.services.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author louye
 */
@Service("chatProxyServiceImpl")
@Slf4j
public class ChatProxyServiceImpl implements ChatService {
    @Resource
    private SystemConfig systemConfig;

    @Override
    public void sendResponse(Message message, Consumer<String> send) throws IOException {
        SystemConfig.ProxyInfo proxyInfo = systemConfig.getProxy();
        if (StringUtils.isBlank(proxyInfo.getApiKey())) {
            send.accept("未配置代理 ApiKey, 请联系管理员!");
            return;
        }
        if (Objects.equals(message.getType(), Message.MessageType.IMAGE)) {
            send.accept("代理模式暂时不支持图片问答模式! 敬请期待~");
            return;
        }

        message.setApiKey(proxyInfo.getApiKey());

        // 调用接口
        HttpResponse result;
        try {
            result = HttpRequest.post(proxyInfo.getHttp())
                    .header(Header.CONTENT_TYPE, "application/json")
                    .body(JSONUtil.toJsonStr(message))
                    .executeAsync();
        } catch (Exception e) {
            send.accept(String.join("", "发生不可预计的错误", e.getMessage()));
            send.accept("END");
            return;
        }

        String line;
        send.accept("[系统消息]请稍后，消息正在读取中...\n\n");
        assert result != null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(result.bodyStream()));

        while ((line = reader.readLine()) != null) {
            String msgResult = UnicodeUtil.toString(line);
            send.accept(msgResult + "\n");
        }
        // 关闭流
        reader.close();
        send.accept("END");
    }
}
