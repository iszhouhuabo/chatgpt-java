package com.github.iszhouhuabo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.iszhouhuabo.domain.BuildBot;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author louye
 */
public interface BuildBotMapper extends BaseMapper<BuildBot> {

    /**
     * 通过机器人获取对于的模型ID(openai 生成的 ft-******)
     *
     * @param id 机器人ID
     */
    @Select(" select b.model_id from tl_bot_info a left join tl_train_model_info b on a.train_model_id = b.id" +
            " where a.is_train = 1")
    String getFineTuneId(@Param("id") String id);
}
