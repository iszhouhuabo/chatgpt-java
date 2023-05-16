package com.github.iszhouhuabo.web;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.github.iszhouhuabo.domain.BuildBot;
import com.github.iszhouhuabo.domain.ChatGptConfig;
import com.github.iszhouhuabo.services.BuildAIService;
import com.github.iszhouhuabo.web.response.Resp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import java.io.File;
import java.io.IOException;

/**
 * 构建模型
 *
 * @author louye
 */
@RestController
@RequestMapping("api/build")
@RequiredArgsConstructor
@Slf4j
public class BuildAIController {

    public final BuildAIService buildAIService;
    public final ChatGptConfig chatGptConfig;

    /**
     * 上传文件
     */
    @CrossOrigin
    @PostMapping("/upload")
    public Resp uploadFile(@RequestBody MultipartFile file) {
        if (file.isEmpty()) {
            return Resp.fail().msg("请选择要上传的文件");
        }
        // 检查文件类型
        String fileExtension = this.getFileExtension(file);
        if (!"xlsx".equals(fileExtension)) {
            return Resp.fail().msg("只允许上传Excel[xlsx]文件");
        }
        try {
            // 获取文件名
            String fileName = StrUtil.subBefore(file.getOriginalFilename(), ".", true)
                    + UUID.fastUUID() + ".xlsx";
            // 指定文件存储路径
//            String filePath = "/Users/zhouhuabo/tmp/uploads/";
            String filePath = this.chatGptConfig.getTrainFile();
            if (!FileUtil.isDirectory(new File(filePath))) {
                FileUtil.mkParentDirs(filePath);
                FileUtil.mkdir(filePath);
            }
            // 创建文件对象
            File dest = new File(filePath + fileName);
            // 执行文件上传
            file.transferTo(dest);
            return Resp.ok().msg("文件上传成功").data(fileName);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Resp.ok().msg("文件上传失败").data(e.getMessage());
        }
    }

    private String getFileExtension(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        if (originalFileName != null) {
            return originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
        }
        throw new RuntimeException("文件名不能数为空");
    }

    @GetMapping("list")
    @CrossOrigin
    public Resp list(String name) {
        return Resp.ok().data(
                this.buildAIService.lambdaQuery().like(StrUtil.isNotBlank(name), BuildBot::getName, name).list());
    }

    @PostMapping(value = {"add", "update"})
    @CrossOrigin
    public Resp add(@RequestBody @Validated BuildBot bot) {
        return Resp.ok().data(this.buildAIService.saveOrUpdate(bot));
    }

    @DeleteMapping("del/{id}")
    @CrossOrigin
    public Resp delete(@PathVariable String id) {
        return Resp.ok().data(this.buildAIService.del(id));
    }

    @PostMapping("train")
    @CrossOrigin
    public Resp train(@RequestParam @Validated @NotBlank(message = "id不能数为空") String id) {
        return Resp.ok().data(this.buildAIService.train(id));
    }

    @GetMapping("progress")
    @CrossOrigin
    public Resp showTrainProgress(@RequestParam @Validated @NotBlank(message = "ID不能为空!") String id) {
        return this.buildAIService.showTrainProgress(id);
    }

    @GetMapping("bot/type")
    @CrossOrigin
    public Resp botType() {
        return Resp.ok().data(this.buildAIService.lambdaQuery()
                .select(BuildBot::getType).list());
    }

}
