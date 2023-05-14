package com.github.iszhouhuabo.listener;

import com.github.iszhouhuabo.utils.SseHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * @author louye
 */
@RequiredArgsConstructor
public class SseStreamListener extends AbstractStreamListener {
    final SseEmitter sseEmitter;

    @Override
    public void onMsg(String message) {
        SseHelper.send(this.sseEmitter, message);
    }

    @Override
    public void onError(Throwable throwable, String response) {
        SseHelper.complete(this.sseEmitter);
    }

}
