package com.github.iszhouhuabo.services;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.iszhouhuabo.domain.BuildBot;
import com.github.iszhouhuabo.mapper.BuildBotMapper;
import com.github.iszhouhuabo.web.response.Resp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author louye
 */
@Service
@Slf4j
public class BuildAIService extends ServiceImpl<BuildBotMapper, BuildBot> {

    public Resp train(String id) {
        BuildBot bot = this.getById(id);
        if (bot == null) {
            return Resp.fail().msg("机器人不存在!");
        }
        
        return Resp.ok().msg("机器人已在后台训练中...");
    }
}
