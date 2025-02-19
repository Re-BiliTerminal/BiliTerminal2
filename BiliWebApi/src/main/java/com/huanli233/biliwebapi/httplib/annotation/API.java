package com.huanli233.biliwebapi.httplib.annotation;

import com.huanli233.biliwebapi.httplib.Domains;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface API {
    String value() default Domains.BASE_API_URL;
}
