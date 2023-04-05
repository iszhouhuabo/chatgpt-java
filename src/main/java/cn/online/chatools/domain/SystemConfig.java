package cn.online.chatools.domain;


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
    private String content = "Louye-ChatGPT【chatools.online】";
    private String auth = "louyezhou";
    private String freeApiKey;
    private String notAuthContent;

    private boolean useProxy;
    private ProxyInfo proxy;

    @Data
    public static class ProxyInfo {
        private String http;
        private String apiKey;
    }
}
