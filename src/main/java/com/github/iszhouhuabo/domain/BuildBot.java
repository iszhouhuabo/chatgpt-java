package com.github.iszhouhuabo.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * 构建一个新的机器人
 *
 * @author louye
 */
@Data
@TableName("tl_bot_info")
public class BuildBot {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    @NotBlank(message = "机器人名称不能数为空")
    private String name;
    @NotBlank(message = "机器人类别不能数为空")
    private String type;
    @TableField(value = "`describe`")
    private String describe;
    private String trainFile;
    private int isDigitized;
    private int isTrain;
    // 生成的微调文件地址
    private String fineTuningFile;
    private LocalDateTime trainTime;

    private LocalDateTime createTime;

    public BuildBot() {
        this.createTime = LocalDateTime.now();
    }
}
