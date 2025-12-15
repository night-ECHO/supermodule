package com.adflex.profile.security;



import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SpamGuard {
    int maxRequests() default 5;      // Tối đa 5 lần
    int windowSeconds() default 60;   // Trong vòng 60 giây
    boolean checkPhone() default false;
}
