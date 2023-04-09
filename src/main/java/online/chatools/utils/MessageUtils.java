package online.chatools.utils;

import cn.hutool.core.util.StrUtil;

/**
 * @author louye
 */
public class MessageUtils {

    public static String handErrorMsg(String msgResult) {
        if (StrUtil.contains(msgResult, "Rate limit reached for")) {
            return "[500] GPT官网网络不稳定, 请刷新后重试, 或者重发消息!";
        }
        if (StrUtil.contains(msgResult, "insufficient_quota")) {
            // 余额不足
            return "[501] GPT官网网络不稳定, 请刷新后重试, 或者重发消息!";
        }
        if (StrUtil.contains(msgResult, "Incorrect API key provided")) {
            // key被封了
            return "[502] GPT官网网络不稳定, 请刷新后重试, 或者重发消息!";
        }
        return "GPT通讯出现不可预料错误, 请刷新重试! \n\r " + msgResult;
    }
}
