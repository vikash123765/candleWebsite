package com.vikash.mobileCaseBackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
public class CorsConfig implements WebMvcConfigurer {@Override
public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
            .allowedOrigins("https://api.vtscases.com")
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowedHeaders("*"); // You can specify specific headers if needed
}
}

