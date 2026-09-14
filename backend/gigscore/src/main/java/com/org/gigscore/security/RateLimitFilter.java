package com.org.gigscore.security;

import java.io.IOException;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    static final String LOGIN_PATH = "/api/v1/users/login";
    static final String CHAT_PATH = "/api/v1/chat/ask";
    static final long WINDOW_MILLIS = 60_000L;

    private final ErrorResponseWriter errorResponseWriter;

    @Value("${rate.limit.login-per-minute:10}")
    private int loginPerMinute;

    @Value("${rate.limit.chat-per-minute:20}")
    private int chatPerMinute;

    private final Map<String, Deque<Long>> hits = new ConcurrentHashMap<>();

    public RateLimitFilter(ErrorResponseWriter errorResponseWriter) {
        this.errorResponseWriter = errorResponseWriter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        int limit = -1;
        if (LOGIN_PATH.equals(path)) {
            limit = loginPerMinute;
        } else if (CHAT_PATH.equals(path)) {
            limit = chatPerMinute;
        }

        if (limit <= 0) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = request.getRemoteAddr() + "|" + path;
        long now = System.currentTimeMillis();
        Deque<Long> timestamps = hits.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());

        synchronized (timestamps) {
            while (!timestamps.isEmpty() && now - timestamps.peekFirst() > WINDOW_MILLIS) {
                timestamps.pollFirst();
            }
            if (timestamps.size() >= limit) {
                response.setHeader("Retry-After", "60");
                errorResponseWriter.write(response, 429, "Too many requests. Please try again later.");
                return;
            }
            timestamps.addLast(now);
        }

        filterChain.doFilter(request, response);
    }
}