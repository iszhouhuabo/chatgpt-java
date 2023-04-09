package online.chatools.controllers;

import online.chatools.controllers.request.WebMessage;
import online.chatools.services.impl.Chat;
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

    @PostMapping("send")
    public void stream(@RequestBody WebMessage webMessage,
                       HttpServletResponse response) throws IOException {

        OutputStream outputStream = response.getOutputStream();
        response.setContentType("application/octet-stream");
        chat.sendResponse(webMessage, (str) -> {
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
