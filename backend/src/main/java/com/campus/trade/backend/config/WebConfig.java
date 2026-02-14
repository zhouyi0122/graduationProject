package com.campus.trade.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取项目根目录下的 uploads 文件夹绝对路径
        String uploadPath = System.getProperty("user.dir") + File.separator + "uploads" + File.separator;
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // 将 /api/uploads/** 映射到本地文件系统
        registry.addResourceHandler("/api/uploads/**")
                .addResourceLocations("file:" + uploadPath);
    }
}
