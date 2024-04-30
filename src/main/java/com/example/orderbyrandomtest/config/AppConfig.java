package com.example.orderbyrandomtest.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry(order = Ordered.LOWEST_PRECEDENCE - 4)
@Configuration
public class AppConfig {
}
