package com.github.iszhouhuabo.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author louye
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("tl_train_model_info")
public class FineTuneData {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String modelId;
    private String baseModel;
    private String organizationId;
    private String status;
    private Date updatedAt;
    private Date createdAt;
    private String fineTunedModel;
    private String trainingId;
}
