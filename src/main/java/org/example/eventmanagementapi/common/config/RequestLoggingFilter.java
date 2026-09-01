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
    protected void doFilterInternal(@NonNull final HttpServletRequest request, @NonNull final HttpServletResponse response, final FilterChain filterChain)
            throws ServletException, IOException {

        final ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request, 1024 * 1024); // 1MB cache limit
        final String correlationId = UUID.randomUUID().toString().substring(0, 8);
        final long startTime = System.currentTimeMillis();

        final String uri = request.getRequestURI();
        final String queryString = request.getQueryString() != null ? "?" + request.getQueryString() : "";
        final String method = request.getMethod();

        try {
            filterChain.doFilter(wrappedRequest, response);
        } finally {
            final long duration = System.currentTimeMillis() - startTime;
            final int status = response.getStatus();

            // Gets the Input params from JSON Request Body
            final String requestBody = getRequestBody(wrappedRequest);

            final String logMessage = String.format(
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

    private String getRequestBody(final ContentCachingRequestWrapper request) {
        final byte[] buf = request.getContentAsByteArray();
        if (buf.length > 0) {
            return new String(buf, 0, Math.min(buf.length, 1000), StandardCharsets.UTF_8)
                    .replaceAll("[\\r\\n]+", "")
                    .replaceAll("\\s{2,}", " ");
        }
        return "";
    }

    @Override
    protected boolean shouldNotFilter(final HttpServletRequest request) {
        final String path = request.getRequestURI();
        return path.contains("/swagger-ui") || path.contains("/v3/api-docs") || path.contains("/favicon.ico");
    }
}
