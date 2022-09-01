package com.dope.breaking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.CacheControl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

@EnableJpaAuditing
@SpringBootApplication
public class BreakingApplication implements WebMvcConfigurer {

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {

		registry.addResourceHandler("/static/compressedProfileImg/**").addResourceLocations("/WEB-INF/static/compressedProfileImg/")
				.setCacheControl(CacheControl.maxAge(2, TimeUnit.HOURS).cachePublic());


		registry.addResourceHandler("/static/originalPostMedia/**").addResourceLocations("/WEB-INF/static/originalPostMedia/")
				.setCacheControl(CacheControl.maxAge(2, TimeUnit.HOURS).cachePublic());


		registry.addResourceHandler("/static/originalProfileImg/**").addResourceLocations("/WEB-INF/static/originalProfileImg/")
				.setCacheControl(CacheControl.maxAge(2, TimeUnit.HOURS).cachePublic());


		registry.addResourceHandler("/static/thumbnailPostMedia/**").addResourceLocations("/WEB-INF/static/thumbnailPostMedia/")
				.setCacheControl(CacheControl.maxAge(2, TimeUnit.HOURS).cachePublic());
	}

	@PostConstruct
	public void started() {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
	}

	public static void main(String[] args) {
		SpringApplication.run(BreakingApplication.class, args);
	}



}
