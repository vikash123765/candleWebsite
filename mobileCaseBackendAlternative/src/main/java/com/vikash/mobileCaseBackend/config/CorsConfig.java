package com.vikash.mobileCaseBackend.config;

import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class CorsConfig implements WebMvcConfigurer {@Override
public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
            .allowedOrigins("https://vtscases.netlify.app/")
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowedHeaders("*"); // You can specify specific headers if needed
}
}
