package com.github.iszhouhuabo.services;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.iszhouhuabo.domain.BuildBot;
import com.github.iszhouhuabo.domain.ChatGptConfig;
import com.github.iszhouhuabo.domain.TrainData;
import com.github.iszhouhuabo.mapper.BuildBotMapper;
import com.github.iszhouhuabo.web.response.Resp;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author louye
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BuildAIService extends ServiceImpl<BuildBotMapper, BuildBot> {

    private final ChatGptConfig chatGptConfig;

    @SneakyThrows
    public static void main(String[] args) {
        // openai api fine_tunes.follow -i ft-zMZcY5YHTMRY5lxQ64JIhlJI
        // openai api fine_tunes.follow -i ft-pBNDDU4PR90DS8LOsyJu10x0
        ChatGptConfig chatGptConfig = new ChatGptConfig();
        BuildBot bot = new BuildBot();
        bot.setTrainFile("train.xlsx");
        chatGptConfig.setTrainFile("/Users/zhouhuabo/tmp/uploads/");
        CompletableFuture.supplyAsync(() -> {
            String fineTuningFile = chatGptConfig.getTrainFile() + "fineTuningFile/";
            if (!FileUtil.isDirectory(new File(fineTuningFile))) {
                FileUtil.mkdir(fineTuningFile);
            }
            List<TrainData> train = ExcelUtil.getReader(chatGptConfig.getTrainFile() + bot.getTrainFile()).readAll(TrainData.class);

            FileUtil.writeUtf8Lines(train,
                    fineTuningFile + StrUtil.subBefore(bot.getTrainFile(), ".", false) + ".jsonl");
            return StrUtil.subBefore(bot.getTrainFile(), ".", false) + ".jsonl";
        }).thenAcceptAsync((rep) -> {
        }).exceptionally((throwable) -> {
            throwable.printStackTrace();
            return null;
        }).get();
    }

    public Resp train(String id) {
        BuildBot bot = this.getById(id);
        if (bot == null) {
            return Resp.fail().msg("机器人不存在!");
        }
        CompletableFuture.supplyAsync(() -> {
            String fineTuningFile = this.chatGptConfig.getTrainFile() + "fineTuningFile/";
            if (!FileUtil.isDirectory(new File(fineTuningFile))) {
                FileUtil.mkdir(fineTuningFile);
            }
            List<TrainData> train = ExcelUtil.getReader(this.chatGptConfig.getTrainFile() + bot.getTrainFile()).readAll(TrainData.class);
            FileUtil.writeUtf8Lines(train,
                    fineTuningFile + StrUtil.subBefore(bot.getTrainFile(), ".", false) + ".jsonl");
            return StrUtil.subBefore(bot.getTrainFile(), ".", false) + ".jsonl";
        }).thenAcceptAsync((rep) -> {
            if (StrUtil.isBlank(rep)) {
                throw new RuntimeException("训练失败! 生成的文件为空!");
            }
            this.lambdaUpdate().set(BuildBot::getFineTuningFile, rep).set(BuildBot::getIsTrain, 1).eq(BuildBot::getId, id).update();
        }).exceptionally((throwable) -> {
            log.error("训练失败! ", throwable);
            return null;
        });
        return Resp.ok().msg("机器人已在后台训练中...");

    }
}
