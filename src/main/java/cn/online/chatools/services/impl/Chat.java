package cn.online.chatools.services.impl;

import cn.online.chatools.controllers.request.Message;
import cn.online.chatools.domain.SystemConfig;
import cn.online.chatools.services.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * @author louye
 */
@Service
public class Chat implements ChatService {

    @Resource
    private ChatGPTServiceImpl chatGPTService;
    @Resource
    private ChatProxyServiceImpl chatProxyService;
    @Resource
    private SystemConfig systemConfig;

    @Override
    public void sendResponse(Message message, Consumer<String> send) throws IOException {
        if (systemConfig.isUseProxy()) {
            chatProxyService.sendResponse(message, send);
        } else {
            chatGPTService.sendResponse(message, send);
        }
    }
}
