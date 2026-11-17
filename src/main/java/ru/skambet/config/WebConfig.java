package ru.skambet.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ru.skambet.web.BlockedUserInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final BlockedUserInterceptor blockedUserInterceptor;

    public WebConfig(BlockedUserInterceptor blockedUserInterceptor) {
        this.blockedUserInterceptor = blockedUserInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // feature 20
        registry.addInterceptor(blockedUserInterceptor)
                .excludePathPatterns("/login", "/register", "/error", "/css/**", "/js/**", "/favicon.ico");
    }
}
