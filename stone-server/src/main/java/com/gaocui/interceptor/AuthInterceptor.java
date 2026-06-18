package com.gaocui.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaocui.common.Result;
import com.gaocui.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public AuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();
        if ("GET".equalsIgnoreCase(request.getMethod()) && path.matches(".*/api/products/\\d+$")) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            send401(response);
            return false;
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validate(token)) {
            send401(response);
            return false;
        }

        Claims claims = jwtUtil.parse(token);
        request.setAttribute("merchantId", claims.get("merchantId", Long.class));
        request.setAttribute("isVip", claims.get("isVip", Boolean.class));
        return true;
    }

    private void send401(HttpServletResponse response) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(401);
        response.getWriter().write(new ObjectMapper().writeValueAsString(Result.fail(401, "未登录或token已过期")));
    }
}
