package cn.online.chatools.controllers.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author louye
 */
@Setter
@Getter
@Builder
public class NotificationMessage {
    @Builder.Default
    private String msgtype = "text";
    private Content text;

    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Content {
        private String content;
    }
}
