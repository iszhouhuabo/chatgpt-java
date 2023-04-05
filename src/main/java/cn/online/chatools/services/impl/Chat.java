package cn.online.chatools.services.impl;

import cn.hutool.core.util.StrUtil;
import cn.online.chatools.controllers.request.Message;
import cn.online.chatools.domain.SystemConfig;
import cn.online.chatools.domain.common.Dic;
import cn.online.chatools.services.ChatService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
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
        if (handSystemMessage(message.getMessage(), send)) {
            send.accept(Dic.SEND_END);
            return;
        }

        if (systemConfig.isUseProxy()) {
            chatProxyService.sendResponse(message, send);
        } else {
            chatGPTService.sendResponse(message, send);
        }
    }

    private boolean handSystemMessage(List<String> message, Consumer<String> send) {
        String str = message.get(0);
        if (StrUtil.startWith(str, "#")) {
            if (checkAuth(StrUtil.subBetween(str, "#", "#"))) {
                String op = StrUtil.subAfter(str, "#", true);
                switch (op) {
                    case Dic.SysOp.CLOSE_PROXY:
                        systemConfig.setUseProxy(false);
                        send.accept("关闭代理成功!");
                        return true;
                    case Dic.SysOp.OPEN_PROXY:
                        systemConfig.setUseProxy(true);
                        send.accept("打开代理成功!");
                        return true;
                    case Dic.SysOp.CLOSE_FREE_API_KEY:
                        systemConfig.setFreeApiKey(null);
                        send.accept("关闭内置APIKEY成功!");
                        return true;
                    default:
                        if (StrUtil.contains(op, "=")) {
                            if (Dic.SysOp.SET_FREE_API_KEY.equals(
                                    StrUtil.subBefore(op, "=", false)
                            )) {
                                String key = StrUtil.subAfter(op, "=", false);
                                if (StrUtil.isNotBlank(key)) {
                                    systemConfig.setFreeApiKey(key);
                                    send.accept("内置APIKEY，设置成功!");
                                    return true;
                                }
                            }
                        }
                }
            }
        }
        return false;
    }


    private boolean checkAuth(String str) {
        return systemConfig.getAuth().equals(str);
    }
}
