package com.github.iszhouhuabo.web;

import com.github.iszhouhuabo.services.ChatSseService;
import com.github.iszhouhuabo.web.request.ChatRequest;
import com.github.iszhouhuabo.web.response.Resp;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.validation.constraints.NotBlank;

/**
 * @author louye
 */
@RestController
@RequestMapping("api/gpt")
@ResponseBody
@RequiredArgsConstructor
public class ChatGptController {
    private final ChatSseService sseService;

    @CrossOrigin
    @GetMapping("/sse_create")
    public SseEmitter createConnect(@RequestHeader @NotBlank String uid) {
        return sseService.createSse(uid);
    }

    @CrossOrigin
    @GetMapping("/sse_close")
    public Resp closeConnect(@RequestHeader @NotBlank String uid) {
        return sseService.closeSse(uid);
    }

    @CrossOrigin
    @PostMapping("/sse_chat")
    @ResponseBody
    public Resp sseChat(@RequestBody @Validated ChatRequest chatRequest, @NotBlank String uid) {
        return sseService.sseChat(uid, chatRequest);
    }


}
