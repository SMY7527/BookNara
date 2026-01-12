package com.booknara.booknaraPrj.admin.inquiry;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 브라우저에서 /uploads/** 로 접근하면 실제 C:/data/uploads/ 폴더를 보여줌
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:/C:/data/uploads/"); // 실제 파일이 저장된 절대 경로 (끝에 / 필수)
    }
}
