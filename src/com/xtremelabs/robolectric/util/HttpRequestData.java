package com.xtremelabs.robolectric.util;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.impl.client.DefaultRequestDirector;
import org.apache.http.protocol.HttpContext;

public class HttpRequestData {
    HttpRequest httpRequest;
    HttpHost httpHost;
    HttpContext httpContext;
    DefaultRequestDirector requestDirector;

    public HttpRequestData(HttpRequest httpRequest, HttpHost httpHost, HttpContext httpContext, DefaultRequestDirector requestDirector) {
        this.httpRequest = httpRequest;
        this.httpHost = httpHost;
        this.httpContext = httpContext;
        this.requestDirector = requestDirector;
    }

    public HttpRequest getHttpRequest() {
        return httpRequest;
    }

    public HttpHost getHttpHost() {
        return httpHost;
    }

    public HttpContext getHttpContext() {
        return httpContext;
    }

    public DefaultRequestDirector getRequestDirector() {
        return requestDirector;
    }
}
