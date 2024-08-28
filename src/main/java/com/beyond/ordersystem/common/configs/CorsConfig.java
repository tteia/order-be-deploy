package com.beyond.ordersystem.common.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry corsRegistry){
        corsRegistry.addMapping("/**")
                .allowedOrigins("https://www.tteia.xyz") // 허용 url 명시 (우리 서버에 들어올 수 있는 url 명시)
                .allowedMethods("*") // GET, POST, PUT, DELETE 등 요청
                .allowedHeaders("*")
                .allowCredentials(true);
    }

}
