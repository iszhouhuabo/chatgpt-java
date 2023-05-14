package com.github.iszhouhuabo.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author louye
 */
@Data
public class TrainData implements Serializable {
    @Serial
    private static final long serialVersionUID = -1629163588528001802L;
    private String prompt;
    private String completion;

    //重写 toString()
    @Override
    public String toString() {
        return "{\"prompt\":\"" + this.prompt + " ->\", \"completion\":\"" + this.completion + "\\n\"}";
    }
}
