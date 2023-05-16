package com.github.iszhouhuabo.services;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.poi.excel.ExcelUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.iszhouhuabo.domain.BuildBot;
import com.github.iszhouhuabo.domain.ChatGptConfig;
import com.github.iszhouhuabo.domain.FineTuneData;
import com.github.iszhouhuabo.domain.TrainData;
import com.github.iszhouhuabo.mapper.BuildBotMapper;
import com.github.iszhouhuabo.mapper.FineTuneMapper;
import com.github.iszhouhuabo.web.response.Resp;
import com.unfbx.chatgpt.OpenAiClient;
import com.unfbx.chatgpt.entity.files.UploadFileResponse;
import com.unfbx.chatgpt.entity.fineTune.FineTune;
import com.unfbx.chatgpt.entity.fineTune.FineTuneResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.concurrent.*;

/**
 * 构建机器人
 *
 * @author louye
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BuildAIService extends ServiceImpl<BuildBotMapper, BuildBot> {
    private final ChatGptConfig chatGptConfig;
    private final OpenAiClient openAiClient;
    private final FineTuneMapper fineTuneMapper;

    /**
     * 开始微调模型,收费 api key 才能使用!!!
     *
     * @param id 生成的机器人id
     * @return 结果, 异步
     */
    public Resp train(final String id) {
        // openai api fine_tunes.follow -i ft-zMZcY5YHTMRY5lxQ64JIhlJI
        // openai api fine_tunes.follow -i ft-pBNDDU4PR90DS8LOsyJu10x0
        final BuildBot bot = this.getById(id);
        if (bot == null) {
            return Resp.fail().msg("机器人不存在!");
        }
        final CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            final String fineTuningFile = this.chatGptConfig.getTrainFile() + "fineTuningFile/";
            if (!FileUtil.isDirectory(new File(fineTuningFile))) {
                FileUtil.mkdir(fineTuningFile);
            }
            final List<TrainData> train = ExcelUtil.getReader(this.chatGptConfig.getTrainFile() + bot.getTrainFile()).readAll(TrainData.class);
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
        } catch (final InterruptedException | ExecutionException e) {
            log.error("训练文件转换失败!", e);
            return Resp.ok().msg("生成训练文件失败! 错误消息: {}", e);
        }

        if (future.isDone() && !future.isCompletedExceptionally()) {
            final UploadFileResponse uploadFileResponse = this.openAiClient.uploadFile(new File(this.chatGptConfig.getTrainFile() + "fineTuningFile/" + bot.getFineTuningFile()));
            log.info(JSONUtil.toJsonStr(uploadFileResponse));
            final long tid = IdUtil.getSnowflakeNextId();
            this.fineTuneMapper.insert(
                    FineTuneData.builder()
                            .id(tid)
                            .trainingId(uploadFileResponse.getId())
                            .createdAt(DateUtil.date(uploadFileResponse.getCreated_at()))
                            .build());
            this.lambdaUpdate().set(BuildBot::getTrainModelId, tid).eq(BuildBot::getId, id).update();

            // 开始训练
            FineTune fineTune = FineTune.builder()
                    .trainingFile(uploadFileResponse.getId())
                    .suffix("tl-ds")
                    .model(FineTune.Model.ADA.getName())
                    .build();
            FineTuneResponse fineTuneResponse = this.openAiClient.fineTune(fineTune);
            log.info(JSONUtil.toJsonStr(fineTuneResponse));
            this.fineTuneMapper.updateById(FineTuneData.builder()
                    .id(tid)
                    .status(fineTuneResponse.getStatus())
                    .fineTunedModel(fineTuneResponse.getFineTunedModel())
                    .organizationId(fineTuneResponse.getOrganizationId())
                    .baseModel(fineTuneResponse.getModel())
                    .modelId(fineTuneResponse.getId())
                    .updatedAt(DateUtil.date())
                    .build());

            // 开启定时查询
            ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
            Runnable task = () -> {
                FineTuneResponse checkSts = this.openAiClient.retrieveFineTune(fineTuneResponse.getId());
                log.info("定时获取训练状态--> {}", JSONUtil.toJsonStr(checkSts));
                if ("succeeded".equalsIgnoreCase(checkSts.getStatus())) {
                    this.fineTuneMapper.updateById(FineTuneData.builder()
                            .id(tid)
                            .status(checkSts.getStatus())
                            .fineTunedModel(checkSts.getFineTunedModel())
                            .build());
                    executor.shutdown();
                }
            };
            // 延迟 1 分钟执行,每次间隔5分钟...
            executor.scheduleAtFixedRate(task, 1, 5, TimeUnit.MINUTES);
            executor.schedule(executor::shutdown, 30, TimeUnit.MINUTES);
        }
        return Resp.ok().msg("机器人已在后台训练中...");

    }

    /**
     * 删除机器人,包含删除文件、格式化之后的微调文件
     *
     * @param id 机器人id
     */
    public int del(final String id) {
        final BuildBot delInfo = this.getById(id);
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

    /**
     * 查看进度
     *
     * @param id 机器人id
     */
    public Resp showTrainProgress(String id) {
        String fineTuneId = this.getBaseMapper().getFineTuneId(id);
        if (StrUtil.isBlank(fineTuneId)) {
            return Resp.fail().msg("机器人对应的:fine-tune是空的,检查是否开启训练?");
        }
        try {
            return Resp.ok().msg("获取成功").data(this.openAiClient.fineTuneEvents(fineTuneId));
        } catch (Exception e) {
            return Resp.fail().data(e.getMessage());
        }
    }
}
