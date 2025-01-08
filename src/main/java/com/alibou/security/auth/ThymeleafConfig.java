package com.alibou.security.auth;

import com.alibou.security.utils.DateUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class ThymeleafConfig {
    @Bean
    public DateUtils dateUtils() {
        return new DateUtils();
    }
}