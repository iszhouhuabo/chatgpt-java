package com.github.iszhouhuabo.web.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.unfbx.chatgpt.entity.chat.Message;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author louye
 */
@Setter
@Getter
public class ChatRequest {
    @JsonProperty("your_api_key")
    private String apiKey;
    @NotBlank(message = "聊天消息不能空")
    private List<Message> message;
    @NotBlank(message = "聊天模型不能为空!")
    private String model;
    @JsonProperty("presence_penalty")
    private double presencePenalty = 0;
    private double temperature = 0.2;
}
