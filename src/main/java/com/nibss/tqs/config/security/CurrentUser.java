package com.nibss.tqs.config.security;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.*;

/**
 * Created by eoriarewo on 4/6/2017.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
@AuthenticationPrincipal
@Documented
public @interface CurrentUser {
}
