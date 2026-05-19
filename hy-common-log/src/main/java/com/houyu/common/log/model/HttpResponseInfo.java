package com.houyu.common.log.model;

import lombok.Data;

import java.util.Map;

@Data
public class HttpResponseInfo {
    private Integer statusCode;
    private Map<String, String> headers;
    private String responseBody;
    private String contentType;
    private Long contentLength;
    private String errorCode;
    private String errorMessage;
}