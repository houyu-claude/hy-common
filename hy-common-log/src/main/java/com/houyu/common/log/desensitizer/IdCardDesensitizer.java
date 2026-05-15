package com.houyu.common.log.desensitizer;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class IdCardDesensitizer implements Desensitizer {

    private static final Pattern ID_CARD_PATTERN = Pattern.compile("(\\d{3})\\d{10}(\\d{4})");

    @Override
    public String desensitize(String input) {
        if (input == null) {
            return null;
        }
        return ID_CARD_PATTERN.matcher(input).replaceAll("$1**********$2");
    }
}