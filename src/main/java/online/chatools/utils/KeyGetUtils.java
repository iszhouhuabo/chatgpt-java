package online.chatools.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.chatools.domain.SystemConfig;
import org.apache.commons.lang3.NotImplementedException;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * @author louye
 */
@AllArgsConstructor
@Slf4j
public class KeyGetUtils {
    private final SystemConfig systemConfig;
    private final String userApiKey;

    public List<String> doFile() {
        if (StrUtil.isNotBlank(userApiKey)) {
            return Collections.singletonList(userApiKey);
        }
        if (!systemConfig.isFreeEnable()) {
            return List.of();
        }
        if (StrUtil.isNotBlank(systemConfig.getFreeApiKey())) {
            return Collections.singletonList(systemConfig.getFreeApiKey());
        }
        String file = systemConfig.getKeyFile();
        if (FileUtil.exist(file) && FileUtil.isNotEmpty(new File(file))) {
            log.info("从本地文件中获取Key...");
            return FileUtil.readUtf8Lines(systemConfig.getKeyFile());
        }
        return List.of();
    }

    public List<String> doDb() {
        throw new NotImplementedException("还没有实现");
    }

    public List<String> doCache() {
        throw new NotImplementedException("还没有实现");
    }
}
