package online.chatools.domain;


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
    private String content = "Louye ChatGPT";
    private String auth = "louye";
    private String freeApiKey;

    private boolean useForward;
    /**
     * 转发
     */
    private ForwardInfo forward;
    /**
     * 存放Key的文件
     */
    private String keyFile;
    private boolean freeEnable;

    @Data
    public static class ForwardInfo {
        private String http;
        private String apiKey;
    }
}
