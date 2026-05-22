package com.houyu.common.app.aop;

import com.houyu.common.app.config.AppProperties;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class AopPackageMatcher {

    private final Pattern controllerPattern;
    private final Pattern servicePattern;
    private final Pattern managerPattern;

    public AopPackageMatcher(AppProperties appProperties) {
        AppProperties.AopConfig aopConfig = appProperties.getAop();
        this.controllerPattern = Pattern.compile(convertToRegex(aopConfig.getControllerPackage()));
        this.servicePattern = Pattern.compile(convertToRegex(aopConfig.getServicePackage()));
        this.managerPattern = Pattern.compile(convertToRegex(aopConfig.getManagerPackage()));
    }

    private String convertToRegex(String packagePattern) {
        return packagePattern
                .replace(".", "\\.")
                .replace("*", ".*");
    }

    public boolean matchesController(String className) {
        return controllerPattern.matcher(className).matches();
    }

    public boolean matchesService(String className) {
        return servicePattern.matcher(className).matches();
    }

    public boolean matchesManager(String className) {
        return managerPattern.matcher(className).matches();
    }
}