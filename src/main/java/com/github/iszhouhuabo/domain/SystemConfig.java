package com.github.iszhouhuabo.domain;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * 系统相关配置
 *
 * @author louye
 */
@Data
@Component
@Configuration
@ConfigurationProperties(prefix = "system")
public class SystemConfig {
    /**
     * 上下文数量
     */
    private int contextNum = 10;
    /**
     * 使用内置key
     */
    private boolean useInternalKey = false;
}
