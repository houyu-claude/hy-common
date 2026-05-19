package com.houyu.common.log.desensitizer;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class PasswordDesensitizer implements Desensitizer {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("(?i)(password|pwd|passwd)\\s*[=:]\\s*[^\\s,;}]+");

    @Override
    public String desensitize(String input) {
        if (input == null) {
            return null;
        }
        return PASSWORD_PATTERN.matcher(input).replaceAll("$1=******");
    }

    @Override
    public boolean support(DesensitizerType type) {
        return type == DesensitizerType.PASSWORD;
    }
}