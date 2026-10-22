package cn.wildfirechat.app.shiro;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.PostConstruct;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Component
public class CorsFilter extends OncePerRequestFilter {
    // 允许跨域访问的来源，逗号分隔。为空时不允许任何跨域请求（管理后台和工作台与 API 同源部署，不需要跨域）。
    // 不能回显任意 Origin：带 Allow-Credentials 时，任意网站都能带着用户的 Cookie 调接口并读到响应。
    @Value("${wfc.cors.allowed_origins:}")
    private String allowedOrigins;

    private Set<String> allowedOriginSet;

    @PostConstruct
    private void init() {
        allowedOriginSet = new HashSet<>();
        for (String origin : allowedOrigins.split(",")) {
            origin = origin.trim();
            if (!origin.isEmpty()) {
                allowedOriginSet.add(origin);
            }
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String origin = request.getHeader("Origin");
        if (origin != null && allowedOriginSet.contains(origin)) {
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Vary", "Origin");
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Allow-Headers", "Content-Type, Access-Control-Allow-Headers, Cookie, X-Requested-With, authToken");
            response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, OPTIONS, PATCH");
            // refer to https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Access-Control-Expose-Headers
            response.setHeader("Access-Control-Expose-Headers", "authToken");
        }

        filterChain.doFilter(request, response);
    }
}
