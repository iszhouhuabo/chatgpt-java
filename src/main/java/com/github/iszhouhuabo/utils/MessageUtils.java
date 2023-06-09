package com.github.iszhouhuabo.utils;

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
        if (StrUtil.contains(msgResult, "API KEYS 不能为空")) {
            // 没有 kye
            return "[404] 网站未开启内置共享KEY模式, 请在设置中添加您自己的 API KEY, 服务器不会记录该信息!";
        }
        return "GPT通讯出现不可预料错误, 请刷新重试! \n\r " + msgResult;
    }
}
