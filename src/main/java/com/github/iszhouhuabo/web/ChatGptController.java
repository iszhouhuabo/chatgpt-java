package com.github.iszhouhuabo.web;

import com.github.iszhouhuabo.services.ChatHttpService;
import com.github.iszhouhuabo.services.ChatSseService;
import com.github.iszhouhuabo.web.request.ChatRequest;
import com.github.iszhouhuabo.web.response.Resp;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author louye
 */
@RestController
@RequestMapping("api/")
@ResponseBody
@RequiredArgsConstructor
public class ChatGptController {
    private final ChatSseService sseService;
    private final ChatHttpService httpService;

    @CrossOrigin
    @GetMapping("sse/create")
    public SseEmitter createConnect(@RequestHeader @Validated @NotBlank(message = "uid不能数为空") String uid) {
        return this.sseService.createSse(uid);
    }

    @CrossOrigin
    @GetMapping("sse/close")
    public Resp closeConnect(@RequestHeader @Validated @NotBlank(message = "uid不能数为空") String uid) {
        return this.sseService.closeSse(uid);
    }

    @CrossOrigin
    @PostMapping("sse/chat")
    public Resp sseChat(@RequestBody @Validated ChatRequest chatRequest,
                        @RequestHeader @Validated @NotBlank(message = "uid不能数为空") String uid) {
        return this.sseService.sseChat(uid, chatRequest);
    }

    @CrossOrigin
    @PostMapping("/chat/sse")
    public SseEmitter sseEmitter(@RequestBody @Validated ChatRequest chatRequest) {
        return this.sseService.sendToSse(chatRequest);
    }

    @CrossOrigin
    @PostMapping("http/chat")
    public void httpChat(@RequestBody @Validated ChatRequest chatRequest, HttpServletResponse response) throws IOException {
        OutputStream outputStream = response.getOutputStream();
        response.setContentType("application/octet-stream");
        this.httpService.sendResponse(chatRequest, (str) -> {
            try {
                if (!"END".equals(str)) {
                    outputStream.write(str.getBytes());
                    outputStream.flush();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        outputStream.close();
    }
}
