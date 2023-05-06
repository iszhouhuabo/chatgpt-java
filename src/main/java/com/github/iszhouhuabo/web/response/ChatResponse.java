package com.github.iszhouhuabo.web.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 描述：
 *
 * @author https:www.unfbx.com
 * 2023-04-08
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatResponse {
    /**
     * 问题消耗tokens
     */
    @JsonProperty("question_tokens")
    @Builder.Default
    private long questionTokens = 0;
}
