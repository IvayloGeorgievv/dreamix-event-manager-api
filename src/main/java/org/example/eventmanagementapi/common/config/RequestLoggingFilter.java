package org.example.eventmanagementapi.common.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final String LOG_PREFIX = "<--- {}";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request, 1024 * 1024); // 1MB cache limit
        String correlationId = UUID.randomUUID().toString().substring(0, 8);
        long startTime = System.currentTimeMillis();

        String uri = request.getRequestURI();
        String queryString = request.getQueryString() != null ? "?" + request.getQueryString() : "";
        String method = request.getMethod();

        try {
            filterChain.doFilter(wrappedRequest, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus();

            // Извлича входните параметри от JSON Request Body
            String requestBody = getRequestBody(wrappedRequest);

            String logMessage = String.format(
                    "[Trace: %s] HTTP %s %s%s | Input Body: %s | Status: %d | Time: %dms",
                    correlationId,
                    method,
                    uri,
                    queryString,
                    requestBody.isEmpty() ? "NONE" : requestBody,
                    status,
                    duration
            );

            if (status >= 500) {
                log.error(LOG_PREFIX, logMessage);
            } else if (status >= 400) {
                log.warn(LOG_PREFIX, logMessage);
            } else {
                log.info(LOG_PREFIX, logMessage);
            }
        }
    }

    private String getRequestBody(ContentCachingRequestWrapper request) {
        byte[] buf = request.getContentAsByteArray();
        if (buf.length > 0) {
            return new String(buf, 0, Math.min(buf.length, 1000), StandardCharsets.UTF_8)
                    .replaceAll("[\\r\\n]+", "")
                    .replaceAll("\\s{2,}", " ");
        }
        return "";
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.contains("/swagger-ui") || path.contains("/v3/api-docs") || path.contains("/favicon.ico");
    }
}
