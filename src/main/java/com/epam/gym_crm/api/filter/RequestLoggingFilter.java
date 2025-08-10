
package com.epam.gym_crm.api.filter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final String TRANSACTION_ID_KEY = "transactionId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        String transactionId = UUID.randomUUID().toString().substring(0, 8); 
        MDC.put(TRANSACTION_ID_KEY, transactionId);

        long startTime = System.currentTimeMillis();
        
        // Gelen isteği logla
        logRequest(requestWrapper);

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            
            // Giden yanıtı logla
            logResponse(responseWrapper, duration);
            
            responseWrapper.copyBodyToResponse();
            
            MDC.remove(TRANSACTION_ID_KEY);
        }
    }

    private void logRequest(ContentCachingRequestWrapper request) {
    	
        String requestBody = getBody(request.getContentAsByteArray(), request.getCharacterEncoding());
        logger.info("--> {} {} | Body: {}",
                request.getMethod(),
                request.getRequestURI(),
                requestBody.replaceAll("[\r\n\t]+", ""));
    }

    private void logResponse(ContentCachingResponseWrapper response, long duration) {
        String responseBody = getBody(response.getContentAsByteArray(), response.getCharacterEncoding());
        logger.info("<-- {} | Took: {}ms | Body: {}",
                response.getStatus(),
                duration,
                responseBody.replaceAll("[\r\n\t]+", ""));
    }

    private String getBody(byte[] content, String encoding) {
        if (content.length == 0) {
            return "[No Body]";
        }
        try {
            return new String(content, encoding);
        } catch (UnsupportedEncodingException e) {
            return "[Unsupported Encoding]";
        }
    }
}