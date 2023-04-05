package cn.online.chatools.domain.common;

import lombok.Getter;

/**
 * @author louye
 */
public class Dic {

    public final static String OP_START = "#(.*?)#";
    public final static String SEND_END = "END";

    public static class SysOp {
        public static final String CLOSE_PROXY = "关闭代理";
        public static final String OPEN_PROXY = "打开代理";
        public static final String UPDATE_FREE_API_KEY = "更新内置API";
        public static final String CLOSE_FREE_API_KEY = "关闭内置";
        public static final String OPEN_FREE_API_KEY = "打开内置";
        public static final String SET_FREE_API_KEY = "设置内置API";
        public static final String CHECK_IS_OK_API_KEY = "更新一个可用的KEY";

        @Getter
        private final String op;

        SysOp(String op) {
            this.op = op;
        }
    }
}
