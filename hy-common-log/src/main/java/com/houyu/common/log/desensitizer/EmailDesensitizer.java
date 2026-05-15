package com.houyu.common.log.desensitizer;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class EmailDesensitizer implements Desensitizer {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("(\\w{1,2})\\w*@(\\w+\\.\\w+)");

    @Override
    public String desensitize(String input) {
        if (input == null) {
            return null;
        }
        return EMAIL_PATTERN.matcher(input).replaceAll("$1**@$2");
    }
}