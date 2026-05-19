package com.houyu.common.log.desensitizer;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DesensitizerManager {

    private final Map<DesensitizerType, Desensitizer> desensitizerMap = new ConcurrentHashMap<>();

    public DesensitizerManager(List<Desensitizer> desensitizers) {
        for (Desensitizer desensitizer : desensitizers) {
            for (DesensitizerType type : DesensitizerType.values()) {
                if (desensitizer.support(type)) {
                    desensitizerMap.put(type, desensitizer);
                }
            }
        }
    }

    public String desensitize(String input) {
        if (input == null) {
            return null;
        }
        String result = input;
        for (DesensitizerType type : DesensitizerType.values()) {
            Desensitizer desensitizer = desensitizerMap.get(type);
            if (desensitizer != null) {
                result = desensitizer.desensitize(result);
            }
        }
        return result;
    }

    public String desensitize(String input, DesensitizerType type) {
        if (input == null) {
            return null;
        }
        Desensitizer desensitizer = desensitizerMap.get(type);
        if (desensitizer != null) {
            return desensitizer.desensitize(input);
        }
        return input;
    }
}