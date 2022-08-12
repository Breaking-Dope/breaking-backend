package com.dope.breaking.security.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {


    @Override
    public void addCorsMappings(CorsRegistry registry) { //CORS 정책 추가.
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:8080", "https://team-dope.link:8443", "http://localhost:3000", "https://localhost:3000")
                .allowedMethods("*")
                .allowedHeaders("*")
                .exposedHeaders("*")
                .allowCredentials(true);
    }
}
