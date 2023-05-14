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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author louye
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BuildAIService extends ServiceImpl<BuildBotMapper, BuildBot> {
    private final ChatGptConfig chatGptConfig;

    public Resp train(String id) {
        // openai api fine_tunes.follow -i ft-zMZcY5YHTMRY5lxQ64JIhlJI
        // openai api fine_tunes.follow -i ft-pBNDDU4PR90DS8LOsyJu10x0

        BuildBot bot = this.getById(id);
        if (bot == null) {
            return Resp.fail().msg("机器人不存在!");
        }
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            String fineTuningFile = this.chatGptConfig.getTrainFile() + "fineTuningFile/";
            if (!FileUtil.isDirectory(new File(fineTuningFile))) {
                FileUtil.mkdir(fineTuningFile);
            }
            List<TrainData> train = ExcelUtil.getReader(this.chatGptConfig.getTrainFile() + bot.getTrainFile()).readAll(TrainData.class);
            FileUtil.writeUtf8Lines(train,
                    fineTuningFile + StrUtil.subBefore(bot.getTrainFile(), ".", false) + ".jsonl");
            return StrUtil.subBefore(bot.getTrainFile(), ".", false) + ".jsonl";
        }).thenApplyAsync((rep) -> {
            if (StrUtil.isBlank(rep)) {
                throw new RuntimeException("训练失败! 生成的文件为空!");
            }
            this.lambdaUpdate().set(BuildBot::getFineTuningFile, rep).set(BuildBot::getIsTrain, 1).eq(BuildBot::getId, id).update();
            return rep;
        }).thenApplyAsync(rep -> {
            bot.setIsTrain(1);
            bot.setFineTuningFile(rep);
            return rep;
        }).exceptionally((throwable) -> {
            log.error("训练失败! ", throwable);
            return throwable.getMessage();
        });

        try {
            // 同步等,实际上没必要要异步,但是想玩玩
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("训练文件转换失败!", e);
            return Resp.ok().msg("生成训练文件失败! 错误消息: {}", e);
        }

        if (future.isDone() && !future.isCompletedExceptionally()) {

        }

        return Resp.ok().msg("机器人已在后台训练中...");

    }

    public int del(String id) {
        BuildBot delInfo = this.getById(id);
        if (delInfo == null) {
            return 0;
        }
        this.removeById(id);
        if (StrUtil.isNotBlank(delInfo.getFineTuningFile())) {
            FileUtil.del(delInfo.getTrainFile());
        }
        if (StrUtil.isNotBlank(delInfo.getFineTuningFile())) {
            FileUtil.del(delInfo.getFineTuningFile());
        }
        return 1;
    }
}
