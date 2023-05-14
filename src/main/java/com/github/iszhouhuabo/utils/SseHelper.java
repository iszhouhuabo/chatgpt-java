package com.github.iszhouhuabo.utils;

import lombok.experimental.UtilityClass;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * @author louye cp github
 */
@UtilityClass
public class SseHelper {
    public void complete(SseEmitter sseEmitter) {
        try {
            sseEmitter.complete();
        } catch (Exception ignored) {
        }
    }

    public void send(SseEmitter sseEmitter, Object data) {
        try {
            sseEmitter.send(data);
        } catch (Exception ignored) {
        }
    }
}
