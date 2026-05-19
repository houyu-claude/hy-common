package com.houyu.common.log.annotation;

import com.houyu.common.log.desensitizer.DesensitizerType;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogDesensitize {
    DesensitizerType type();
}