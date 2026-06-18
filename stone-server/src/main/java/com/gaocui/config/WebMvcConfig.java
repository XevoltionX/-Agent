package com.gaocui.config;

import com.gaocui.interceptor.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final com.gaocui.util.JwtUtil jwtUtil;

    public WebMvcConfig(com.gaocui.util.JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor(jwtUtil))
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/send-code",
                        "/api/login",
                        "/api/search",
                        "/api/ai/match",
                        "/api/inquiry",         // 买家留资公开
                        "/api/chat",            // AI对话公开
                        "/api/chat/stream",     // SSE流式对话公开
                        "/api/upload",          // 图片上传
                        "/api/ai/recognize",    // AI图片识别
                        "/api/alipay/notify",   // 支付宝回调
                        "/api/alipay/pay"       // 支付宝支付页面
                );
    }
}
