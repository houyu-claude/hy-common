package com.houyu.common.log.desensitizer;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class BankCardDesensitizer implements Desensitizer {

    private static final Pattern BANK_CARD_PATTERN = Pattern.compile("(\\d{4})\\d{8,11}(\\d{4})");

    @Override
    public String desensitize(String input) {
        if (input == null) {
            return null;
        }
        return BANK_CARD_PATTERN.matcher(input).replaceAll("$1**********$2");
    }
}