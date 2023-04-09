package online.chatools.domain;

import cn.hutool.core.util.StrUtil;
import com.unfbx.chatgpt.function.KeyStrategyFunction;
import lombok.RequiredArgsConstructor;
import online.chatools.function.HttpSend;

import java.util.List;

/**
 * @author louye
 */
@RequiredArgsConstructor
public class KeyDynamicStrategy implements KeyStrategyFunction<List<String>, String> {

    private final SystemConfig systemConfig;
    private final HttpSend<String> httpSend;

    @Override
    public String apply(List<String> list) {
        if (StrUtil.isNotBlank(systemConfig.getFreeApiKey())) {
            return systemConfig.getFreeApiKey();
        }
        if (list.isEmpty()) {
            // 去文件中拿一次

            // 如果是空的
            httpSend.sendToClient("哦豁, 系统没有密钥了, 请联系管理员！[jayszxs@outlook.com]");
        }
        return null;
    }
}
