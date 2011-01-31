package com.xtremelabs.robolectric.shadows;

import com.xtremelabs.robolectric.util.HttpRequestInfo;
import com.xtremelabs.robolectric.util.RequestMatcher;
import com.xtremelabs.robolectric.util.ResponseRule;
import com.xtremelabs.robolectric.util.TestHttpResponse;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.RequestDirector;
import org.apache.http.protocol.HttpContext;

import javax.xml.ws.http.HTTPException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FakeHttpLayer {
    List<HttpResponse> pendingHttpResponses = new ArrayList<HttpResponse>();
    List<HttpRequestInfo> httpRequestInfos = new ArrayList<HttpRequestInfo>();
    List<ResponseRule> httpResponseRules = new ArrayList<ResponseRule>();
    HttpResponse defaultHttpResponse;

    public void addPendingHttpResponse(int statusCode, String responseBody) {
        addPendingHttpResponse(new TestHttpResponse(statusCode, responseBody));
    }

    public void addPendingHttpResponse(HttpResponse httpResponse) {
        pendingHttpResponses.add(httpResponse);
    }

    public void addHttpResponseRule(String method, String uri, HttpResponse response) {
        addHttpResponseRule(new DefaultRequestMatcher(method, uri), response);
    }

    public void addHttpResponseRule(String uri, HttpResponse response) {
        addHttpResponseRule(new UriRequestMatcher(uri), response);
    }

    public void addHttpResponseRule(String uri, String response) {
        addHttpResponseRule(new UriRequestMatcher(uri), new TestHttpResponse(200, response));
    }

    public void addHttpResponseRule(RequestMatcher requestMatcher, HttpResponse response) {
        addHttpResponseRule(new RequestMatcherResponseRule(requestMatcher, response));
    }

    public void addHttpResponseRule(ResponseRule responseRule) {
        httpResponseRules.add(responseRule);
    }

    public void setDefaultHttpResponse(HttpResponse defaultHttpResponse) {
        this.defaultHttpResponse = defaultHttpResponse;
    }

    public void setDefaultHttpResponse(int statusCode, String responseBody) {
        setDefaultHttpResponse(new TestHttpResponse(statusCode, responseBody));
    }

    private HttpResponse findResponse(HttpRequest httpRequest) throws HttpException, IOException {
        if (!pendingHttpResponses.isEmpty()) {
            return pendingHttpResponses.remove(0);
        }

        for (ResponseRule httpResponseRule : httpResponseRules) {
            if (httpResponseRule.matches(httpRequest)) {
                return httpResponseRule.getResponse();
            }
        }

        return defaultHttpResponse;
    }

    HttpResponse emulateRequest(HttpHost httpHost, HttpRequest httpRequest, HttpContext httpContext, RequestDirector requestDirector) throws HttpException, IOException {
        HttpResponse httpResponse = findResponse(httpRequest);

        if (httpResponse == null) {
            throw new RuntimeException("Unexpected call to execute, no pending responses are available. See Robolectric.addPendingResponse().");
        }

        httpRequestInfos.add(new HttpRequestInfo(httpRequest, httpHost, httpContext, requestDirector));

        return httpResponse;
    }

    public static class RequestMatcherResponseRule implements ResponseRule {
        private RequestMatcher requestMatcher;
        private HttpResponse responseToGive;
        private IOException ioException;
        private HTTPException httpException;

        public RequestMatcherResponseRule(RequestMatcher requestMatcher, HttpResponse responseToGive) {
            this.requestMatcher = requestMatcher;
            this.responseToGive = responseToGive;
        }

        public RequestMatcherResponseRule(RequestMatcher requestMatcher, IOException ioException) {
            this.requestMatcher = requestMatcher;
            this.ioException = ioException;
        }

        public RequestMatcherResponseRule(RequestMatcher requestMatcher, HTTPException httpException) {
            this.requestMatcher = requestMatcher;
            this.httpException = httpException;
        }

        @Override public boolean matches(HttpRequest request) {
            return requestMatcher.matches(request);
        }

        @Override public HttpResponse getResponse() throws HttpException, IOException {
            if (httpException != null) throw httpException;
            if (ioException != null) throw ioException;
            return responseToGive;
        }
    }

    public static class DefaultRequestMatcher implements RequestMatcher {
        private String method;
        private String uri;

        public DefaultRequestMatcher(String method, String uri) {
            this.method = method;
            this.uri = uri;
        }

        @Override public boolean matches(HttpRequest request) {
            return request.getRequestLine().getMethod().equals(method) &&
                    request.getRequestLine().getUri().equals(uri);
        }
    }

    public static class UriRequestMatcher implements RequestMatcher {
        private String uri;

        public UriRequestMatcher(String uri) {
            this.uri = uri;
        }

        @Override public boolean matches(HttpRequest request) {
            return request.getRequestLine().getUri().equals(uri);
        }
    }
}
