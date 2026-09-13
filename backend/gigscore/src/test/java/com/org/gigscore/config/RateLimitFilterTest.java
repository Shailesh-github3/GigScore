package com.org.gigscore.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class RateLimitFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    @Test
    void rejectsRequestsBeyondLimitWith429AndRetryAfter() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(Map.of("/api/v1/users/login", new RateLimitFilter.Limit(2, 3600)));

        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/v1/users/login");
        when(request.getRemoteAddr()).thenReturn("10.0.0.1");

        StringWriter body = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(body));

        filter.doFilter(request, response, chain);
        filter.doFilter(request, response, chain);
        verify(chain, times(2)).doFilter(request, response);

        filter.doFilter(request, response, chain);
        verify(chain, times(2)).doFilter(request, response);
        verify(response).setStatus(429);
        verify(response).setHeader(eq("Retry-After"), anyString());
        assertThat(body.toString()).contains("Too many requests");
    }

    @Test
    void passesThroughUnlimitedPaths() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(Map.of("/api/v1/users/login", new RateLimitFilter.Limit(1, 3600)));

        when(request.getMethod()).thenReturn("GET");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).setStatus(429);
    }
}