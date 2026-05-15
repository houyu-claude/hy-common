package com.houyu.common.log.desensitizer;

public interface Desensitizer {
    String desensitize(String input);
    boolean support(DesensitizerType type);
}