package com.houyu.common.app.interceptor;

import com.houyu.common.app.context.RequestContextHolder;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestContextFilter implements Filter {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_NAME_HEADER = "X-User-Name";
    private static final String RETRY_FLAG_HEADER = "X-Retry-Flag";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String traceId = httpRequest.getHeader(TRACE_ID_HEADER);
        if (traceId == null || traceId.isEmpty()) {
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpResponse.getWriter().write("X-Trace-Id is required");
            return;
        }

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(httpRequest);

        try {
            RequestContextHolder.setTraceId(traceId);
            RequestContextHolder.setRequestId(httpRequest.getHeader(REQUEST_ID_HEADER));
            RequestContextHolder.setUserId(httpRequest.getHeader(USER_ID_HEADER));
            RequestContextHolder.setUserName(httpRequest.getHeader(USER_NAME_HEADER));
            RequestContextHolder.setRetryFlag(Boolean.parseBoolean(
                    httpRequest.getHeader(RETRY_FLAG_HEADER)));

            Map<String, String> headers = new HashMap<>();
            httpRequest.getHeaderNames().asIterator().forEachRemaining(name -> {
                headers.put(name, httpRequest.getHeader(name));
            });
            RequestContextHolder.setRequestHeaders(headers);

            chain.doFilter(wrappedRequest, response);
        } finally {
            RequestContextHolder.clear();
        }
    }
}