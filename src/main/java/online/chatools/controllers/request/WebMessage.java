package online.chatools.controllers.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author louye
 */
@Setter
@Getter
public class WebMessage {
    private String apiKey;
    private List<String> message;
    private int version;
    private MessageType type;


    public enum MessageType {
        TEXT, IMAGE
    }
}
