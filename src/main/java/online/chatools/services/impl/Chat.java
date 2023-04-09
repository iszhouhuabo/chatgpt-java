package online.chatools.services.impl;

import online.chatools.controllers.request.WebMessage;
import online.chatools.domain.SystemConfig;
import online.chatools.function.HttpSend;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @author louye
 */
@Service
public class Chat {

    @Resource
    private ChatGPTServiceImpl chatGptService;
    @Resource
    private ChatForwardServiceImpl chatProxyService;
    @Resource
    private SystemConfig systemConfig;

    public void sendResponse(WebMessage webMessage, HttpSend<String> send) throws IOException {
        if (systemConfig.isUseForward()) {
            chatProxyService.sendResponse(webMessage, send);
        } else {
            chatGptService.sendResponseBySse(webMessage, send);
        }
    }
}
