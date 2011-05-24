package com.xtremelabs.robolectric.tester.org.apache.http;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.RequestDirector;
import org.apache.http.protocol.HttpContext;

import javax.xml.ws.http.HTTPException;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

public class FakeHttpLayer {
    List<HttpResponse> pendingHttpResponses = new ArrayList<HttpResponse>();
    List<HttpRequestInfo> httpRequestInfos = new ArrayList<HttpRequestInfo>();
    List<HttpEntityStub.ResponseRule> httpResponseRules = new ArrayList<HttpEntityStub.ResponseRule>();
    HttpResponse defaultHttpResponse;
    private HttpResponse defaultResponse;

    public void addPendingHttpResponse(int statusCode, String responseBody) {
        addPendingHttpResponse(new TestHttpResponse(statusCode, responseBody));
    }

    public void addPendingHttpResponseWithContentType(int statusCode, String responseBody, Header contentType) {
        addPendingHttpResponse(new TestHttpResponse(statusCode, responseBody, contentType));
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

    public void addHttpResponseRule(HttpEntityStub.ResponseRule responseRule) {
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

        for (HttpEntityStub.ResponseRule httpResponseRule : httpResponseRules) {
            if (httpResponseRule.matches(httpRequest)) {
                return httpResponseRule.getResponse();
            }
        }

        return defaultHttpResponse;
    }

    public HttpResponse emulateRequest(HttpHost httpHost, HttpRequest httpRequest, HttpContext httpContext, RequestDirector requestDirector) throws HttpException, IOException {
        HttpResponse httpResponse = findResponse(httpRequest);

        if (httpResponse == null) {
            throw new RuntimeException("Unexpected call to execute, no pending responses are available. See Robolectric.addPendingResponse().");
        }

        httpRequestInfos.add(new HttpRequestInfo(httpRequest, httpHost, httpContext, requestDirector));

        return httpResponse;
    }
    public boolean hasPendingResponses() {
        return !pendingHttpResponses.isEmpty();
    }

    public boolean hasRequestInfos() {
        return !httpRequestInfos.isEmpty();
    }

    public boolean hasResponseRules() {
        return !httpResponseRules.isEmpty();
    }

    public HttpResponse getDefaultResponse() {
        return defaultResponse;
    }

    public HttpRequestInfo getSentHttpRequestInfo(int index) {
        return httpRequestInfos.get(index);
    }

    public List<HttpRequestInfo> getSentHttpRequestInfos() {
        return new ArrayList<HttpRequestInfo>(httpRequestInfos);
    }

    public void clearHttpResponseRules() {
        httpResponseRules.clear();
    }

    public static class RequestMatcherResponseRule implements HttpEntityStub.ResponseRule {
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

    public static class RequestMatcherBuilder implements RequestMatcher {
        private String method;
        private String hostname;
        private String path;
        private boolean noParams;
        private Map<String, String> params = new HashMap<String, String>();

        public RequestMatcherBuilder method(String method) {
            this.method = method;
            return this;
        }

        public RequestMatcherBuilder host(String hostname) {
            this.hostname = hostname;
            return this;
        }

        public RequestMatcherBuilder path(String path) {
            this.path = "/" + path;
            return this;
        }

        public RequestMatcherBuilder param(String name, String value) {
            params.put(name, value);
            return this;
        }

        public RequestMatcherBuilder noParams() {
            noParams = true;
            return this;
        }

        @Override public boolean matches(HttpRequest request) {
            URI uri = URI.create(request.getRequestLine().getUri());
            if (method != null && !method.equals(request.getRequestLine().getMethod())) return false;
            if (hostname != null && !hostname.equals(uri.getHost())) return false;
            if (path != null && !path.equals(uri.getRawPath())) return false;
            if (noParams && !uri.getRawQuery().equals(null)) return false;
            if (params.size() > 0) {
                StringTokenizer tok = new StringTokenizer(uri.getRawQuery(), "&", false);
                if (tok.countTokens() != params.size()) return false;
                while (tok.hasMoreTokens()) {
                    String name, value;
                    String nextParam = tok.nextToken();
                    if (nextParam.contains("=")) {
                        String[] nameAndValue = nextParam.split("=");
                        name = nameAndValue[0];
                        value = nameAndValue[1];
                    } else {
                        name = nextParam;
                        value = "";
                    }
                    if (!params.get(name).equals(value)) return false;
                }
            }

            return true;
        }
    }

    // TODO: test this
    public static class UriRegexMatcher implements RequestMatcher {
        private String method;
        private final Pattern uriRegex;

        public UriRegexMatcher(String method, String uriRegex) {
            this.method = method;
            this.uriRegex = Pattern.compile(uriRegex);
        }

        @Override public boolean matches(HttpRequest request) {
            return request.getRequestLine().getMethod().equals(method) &&
                    uriRegex.matcher(request.getRequestLine().getUri()).matches();
        }
    }
}
