package com.houyu.common.log.model;

import lombok.Data;

import java.util.Map;

@Data
public class HttpRequestInfo {
    private String method;
    private String uri;
    private String url;
    private String queryString;
    private Map<String, String> headers;
    private Map<String, String> cookies;
    private String requestBody;
    private Map<String, String[]> parameters;
    private String contentType;
    private String userAgent;
    private String referer;
    private String protocol;
    private String clientIp;
}