package com.github.iszhouhuabo.web.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

/**
 * @author louye
 */
@Setter
@Getter
public class ChatRequest {
    @JsonProperty("your_api_key")
    private String apiKey;
    @NotBlank(message = "聊天消息不能空")
    private String message;
}
