package online.chatools.domain;

import cn.hutool.core.util.RandomUtil;
import com.unfbx.chatgpt.function.KeyStrategyFunction;
import lombok.AllArgsConstructor;
import online.chatools.domain.common.Dic;
import online.chatools.function.HttpSend;

import java.util.List;

/**
 * @author louye
 */
@AllArgsConstructor
public class KeyRandom implements KeyStrategyFunction<List<String>, String> {
    private final HttpSend<String> send;

    @Override
    public String apply(List<String> apiKeys) {
        if (apiKeys.isEmpty()) {
            send.sendToClient("oh ~ 无效的 APIKEY !");
            send.sendToClient(Dic.SEND_END);
            throw new NullPointerException("Key 是空的！");
        }
        return RandomUtil.randomEle(apiKeys);
    }
}