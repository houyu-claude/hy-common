package com.houyu.common.log.trace;

public class FlagValidator {
    public static String normalizeFlag(String flag) {
        if (flag != null && flag.length() == 1) {
            char c = flag.charAt(0);
            if (c >= '1' && c <= '9') {
                return flag;
            }
        }
        return "0";
    }
}