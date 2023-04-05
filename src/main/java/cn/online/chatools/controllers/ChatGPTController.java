package cn.online.chatools.controllers;

import cn.online.chatools.controllers.request.Message;
import cn.online.chatools.controllers.response.Resp;
import cn.online.chatools.services.impl.Chat;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author louye
 */
@RestController
@RequestMapping("api/gpt")
@ResponseBody
public class ChatGPTController {
    @Resource
    private Chat chat;

    @GetMapping("/balances")
    public Resp queryBalanceResponse(@RequestParam(required = false) String apiKey) {
        return chat.creditQuery(apiKey);
    }

    @PostMapping("send")
    public void stream(@RequestBody Message message,
                       HttpServletResponse response) throws IOException {

        OutputStream outputStream = response.getOutputStream();
        response.setContentType("application/octet-stream");
        chat.sendResponse(message, (str) -> {
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
