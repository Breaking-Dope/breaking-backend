package com.dope.breaking.security.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {


    @Override
    public void addCorsMappings(CorsRegistry registry) { //CORS 정책 추가.
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000", "http://team-dope.link:3000", "https://team-dope.link")
                .allowedHeaders("authorization", "authorization-refresh", "User-Agent", "Cache-Control", "Content-Type")
                .exposedHeaders("authorization", "authorization-refresh", "User-Agent", "Cache-Control", "Content-Type")
                .allowedMethods("*")
                .allowCredentials(true);
    }
}
