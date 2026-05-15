package com.houyu.common.log.desensitizer;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DesensitizerManager {

    private final List<Desensitizer> desensitizers;

    public DesensitizerManager(List<Desensitizer> desensitizers) {
        this.desensitizers = desensitizers;
    }

    public String desensitize(String input) {
        if (input == null) {
            return null;
        }
        String result = input;
        for (Desensitizer desensitizer : desensitizers) {
            result = desensitizer.desensitize(result);
        }
        return result;
    }
}