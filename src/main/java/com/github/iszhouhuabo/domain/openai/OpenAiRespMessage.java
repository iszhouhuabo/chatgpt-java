package com.github.iszhouhuabo.domain.openai;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.unfbx.chatgpt.entity.common.Usage;
import lombok.Data;

import java.util.List;

/**
 * @author louye
 */
@Data
public class OpenAiRespMessage {
    private String id;
    private String object;
    private long created;
    /**
     * 消息列表
     */
    private List<Choices> choices;
    private String model;
    private Usage usage;


    @Data
    public static class Choices {
        /**
         * 消息内容
         */
        private String text;
        private String content;
        private int index;
        private String logprobs;
        @JsonProperty("finish_reason")
        private String finishReason;
    }
}
