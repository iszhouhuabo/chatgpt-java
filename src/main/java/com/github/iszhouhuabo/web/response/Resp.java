package com.github.iszhouhuabo.web.response;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author louye
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Resp implements Serializable {

    @Serial
    private static final long serialVersionUID = -3264746113146570349L;
    private Integer code;
    private String msg;
    private Object data;

    public static Resp ok() {
        return Resp.builder().code(0).msg("处理成功!").build();
    }
    public static Resp fail() {
        return Resp.builder().code(1).msg("处理失败!").build();
    }
    public Resp code(Integer code) {
        this.code = code;
        return this;
    }

    public Resp msg(String msg, Object... o) {
        this.msg = StrUtil.format(msg, o);
        return this;
    }
    public Resp data(Object data) {
        this.data = data;
        return this;
    }


}
