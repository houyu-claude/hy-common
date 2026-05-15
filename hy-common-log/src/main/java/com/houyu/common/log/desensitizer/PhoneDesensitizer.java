package com.houyu.common.log.desensitizer;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class PhoneDesensitizer implements Desensitizer {

    private static final Pattern PHONE_PATTERN = Pattern.compile("(1[3-9]\\d)\\d{4}(\\d{4})");

    @Override
    public String desensitize(String input) {
        if (input == null) {
            return null;
        }
        return PHONE_PATTERN.matcher(input).replaceAll("$1****$2");
    }
}