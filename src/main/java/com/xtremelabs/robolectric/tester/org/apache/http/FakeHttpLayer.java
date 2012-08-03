package com.xtremelabs.robolectric.tester.org.apache.http;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.shadows.HttpResponseGenerator;
import org.apache.http.*;
import org.apache.http.client.RequestDirector;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class FakeHttpLayer {
    List<HttpResponseGenerator> pendingHttpResponses = new ArrayList<HttpResponseGenerator>();
    List<HttpRequestInfo> httpRequestInfos = new ArrayList<HttpRequestInfo>();
    List<HttpResponse> httpResponses = new ArrayList<HttpResponse>();
    List<HttpEntityStub.ResponseRule> httpResponseRules = new ArrayList<HttpEntityStub.ResponseRule>();
    HttpResponse defaultHttpResponse;
    private HttpResponse defaultResponse;
    private boolean interceptHttpRequests = true;

    public HttpRequestInfo getLastSentHttpRequestInfo() {
        List<HttpRequestInfo> requestInfos = Robolectric.getFakeHttpLayer().getSentHttpRequestInfos();
        if (requestInfos.isEmpty()) {
            return null;
        }
        return requestInfos.get(requestInfos.size() - 1);
    }

    public void addPendingHttpResponse(int statusCode, String responseBody, Header... headers) {
        addPendingHttpResponse(new TestHttpResponse(statusCode, responseBody, headers));
    }

    public void addPendingHttpResponse(final HttpResponse httpResponse) {
        addPendingHttpResponse(new HttpResponseGenerator() {
            @Override
            public HttpResponse getResponse(HttpRequest request) {
                return httpResponse;
            }
        });
    }

    public void addPendingHttpResponse(HttpResponseGenerator httpResponseGenerator) {
        pendingHttpResponses.add(httpResponseGenerator);
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

    /**
     * Add a response rule.
     *
     * @param requestMatcher Request matcher
     * @param responses      A list of responses that are returned to matching requests in order from first to last.
     */
    public void addHttpResponseRule(RequestMatcher requestMatcher, List<? extends HttpResponse> responses) {
        addHttpResponseRule(new RequestMatcherResponseRule(requestMatcher, responses));
    }

    public void addHttpResponseRule(HttpEntityStub.ResponseRule responseRule) {
        httpResponseRules.add(0, responseRule);
    }

    public void setDefaultHttpResponse(HttpResponse defaultHttpResponse) {
        this.defaultHttpResponse = defaultHttpResponse;
    }

    public void setDefaultHttpResponse(int statusCode, String responseBody) {
        setDefaultHttpResponse(new TestHttpResponse(statusCode, responseBody));
    }

    private HttpResponse findResponse(HttpRequest httpRequest) throws HttpException, IOException {
        if (!pendingHttpResponses.isEmpty()) {
            return pendingHttpResponses.remove(0).getResponse(httpRequest);
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
            throw new RuntimeException("Unexpected call to execute, no pending responses are available. See Robolectric.addPendingResponse(). Request was: " +
                    httpRequest.getRequestLine().getMethod() + " " + httpRequest.getRequestLine().getUri());
        } else {
            HttpParams params = httpResponse.getParams();

            if (HttpConnectionParams.getConnectionTimeout(params) < 0) {
                throw new ConnectTimeoutException("Socket is not connected");
            } else if (HttpConnectionParams.getSoTimeout(params) < 0) {
                throw new ConnectTimeoutException("The operation timed out");
            }
        }

        addRequestInfo(new HttpRequestInfo(httpRequest, httpHost, httpContext, requestDirector));
        addHttpResponse(httpResponse);
        return httpResponse;
    }

    public boolean hasPendingResponses() {
        return !pendingHttpResponses.isEmpty();
    }

    public boolean hasRequestInfos() {
        return !httpRequestInfos.isEmpty();
    }

    public void clearRequestInfos() {
        httpRequestInfos.clear();
    }

    /**
     * This method is not supposed to be consumed by tests. This exists solely for the purpose of
     * logging real HTTP requests, so that functional/integration tests can verify if those were made, without
     * messing with the fake http layer to actually perform the http call, instead of returning a mocked response.
     *
     * If you are just using mocked http calls, you should not even notice this method here.
     *
     * @param requestInfo
     */
    public void addRequestInfo(HttpRequestInfo requestInfo) {
        httpRequestInfos.add(requestInfo);
    }

    public boolean hasResponseRules() {
        return !httpResponseRules.isEmpty();
    }

    public boolean hasRequestMatchingRule(RequestMatcher rule) {
        for (HttpRequestInfo requestInfo : httpRequestInfos) {
            if (rule.matches(requestInfo.httpRequest)) {
                return true;
            }
        }
        return false;
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

    public void clearPendingHttpResponses() {
        pendingHttpResponses.clear();
    }

    /**
     * This method return a list containing all the HTTP responses logged by the fake http layer, be it
     * mocked http responses, be it real http calls (if {code}interceptHttpRequests{/code} is set to false).
     *
     * It doesn't make much sense to call this method if said property is set to true, as you yourself are
     * providing the response, but it's here nonetheless.
     *
     * @return List of all HTTP Responses logged by the fake http layer.
     */
    public List<HttpResponse> getHttpResponses() {
        return new ArrayList<HttpResponse>(httpResponses);
    }

    /**
     * As a consumer of the fake http call, you should never call this method. This should be used solely
     * by components that exercises http calls.
     *
     * @param response The final response received by the server
     */
    public void addHttpResponse(HttpResponse response) {
        this.httpResponses.add(response);
    }

    /**
     * Helper method that returns the latest received response from the server.
     * @return The latest HTTP response or null, if no responses are available
     */
    public HttpResponse getLastHttpResponse() {
        if (httpResponses.isEmpty()) return null;
        return httpResponses.get(httpResponses.size()-1) ;
    }

    /**
     * Call this method if you want to ensure that there's no http responses logged from this point until
     * the next response arrives. Helpful to ensure that the state is "clear" before actions are executed.
     */
    public void clearHttpResponses() {
        this.httpResponses.clear();
    }

    /**
     * You can disable Robolectric's fake HTTP layer temporarily
     * by calling this method.
     * @param interceptHttpRequests whether all HTTP requests should be
     *                              intercepted (true by default)
     */
    public void interceptHttpRequests(boolean interceptHttpRequests) {
        this.interceptHttpRequests = interceptHttpRequests;
    }

    public boolean isInterceptingHttpRequests() {
        return interceptHttpRequests;
    }

    public static class RequestMatcherResponseRule implements HttpEntityStub.ResponseRule {
        private RequestMatcher requestMatcher;
        private HttpResponse responseToGive;
        private IOException ioException;
        private HttpException httpException;
        private List<? extends HttpResponse> responses;

        public RequestMatcherResponseRule(RequestMatcher requestMatcher, HttpResponse responseToGive) {
            this.requestMatcher = requestMatcher;
            this.responseToGive = responseToGive;
        }

        public RequestMatcherResponseRule(RequestMatcher requestMatcher, IOException ioException) {
            this.requestMatcher = requestMatcher;
            this.ioException = ioException;
        }

        public RequestMatcherResponseRule(RequestMatcher requestMatcher, HttpException httpException) {
            this.requestMatcher = requestMatcher;
            this.httpException = httpException;
        }

        public RequestMatcherResponseRule(RequestMatcher requestMatcher, List<? extends HttpResponse> responses) {
            this.requestMatcher = requestMatcher;
            this.responses = responses;
        }

        @Override
        public boolean matches(HttpRequest request) {
            return requestMatcher.matches(request);
        }

        @Override
        public HttpResponse getResponse() throws HttpException, IOException {
            if (httpException != null) throw httpException;
            if (ioException != null) throw ioException;
            if (responseToGive != null) {
                return responseToGive;
            } else {
                if (responses.isEmpty()) {
                    throw new RuntimeException("No more responses left to give");
                }
                return responses.remove(0);
            }
        }
    }

    public static class DefaultRequestMatcher implements RequestMatcher {
        private String method;
        private String uri;

        public DefaultRequestMatcher(String method, String uri) {
            this.method = method;
            this.uri = uri;
        }

        @Override
        public boolean matches(HttpRequest request) {
            return request.getRequestLine().getMethod().equals(method) &&
                    request.getRequestLine().getUri().equals(uri);
        }
    }

    public static class UriRequestMatcher implements RequestMatcher {
        private String uri;

        public UriRequestMatcher(String uri) {
            this.uri = uri;
        }

        @Override
        public boolean matches(HttpRequest request) {
            return request.getRequestLine().getUri().equals(uri);
        }
    }

    public static class RequestMatcherBuilder implements RequestMatcher {
        private String method, hostname, path;
        private boolean noParams;
        private Map<String, String> params = new HashMap<String, String>();
        private Map<String, String> headers = new HashMap<String, String>();
        private PostBodyMatcher postBodyMatcher;

        public interface PostBodyMatcher {
            /**
             * Hint: you can use EntityUtils.toString(actualPostBody) to help you implement your matches method.
             *
             * @param actualPostBody The post body of the actual request that we are matching against.
             * @return true if you consider the body to match
             * @throws IOException Get turned into a RuntimeException to cause your test to fail.
             */
            boolean matches(HttpEntity actualPostBody) throws IOException;
        }

        public RequestMatcherBuilder method(String method) {
            this.method = method;
            return this;
        }

        public RequestMatcherBuilder host(String hostname) {
            this.hostname = hostname;
            return this;
        }

        public RequestMatcherBuilder path(String path) {
            if (path.startsWith("/")) {
                throw new RuntimeException("Path should not start with '/'");
            }
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

        public RequestMatcherBuilder postBody(PostBodyMatcher postBodyMatcher) {
            this.postBodyMatcher = postBodyMatcher;
            return this;
        }

        public RequestMatcherBuilder header(String name, String value) {
            headers.put(name, value);
            return this;
        }

        @Override
        public boolean matches(HttpRequest request) {
            URI uri = URI.create(request.getRequestLine().getUri());
            if (method != null && !method.equals(request.getRequestLine().getMethod())) {
                return false;
            }
            if (hostname != null && !hostname.equals(uri.getHost())) {
                return false;
            }
            if (path != null && !path.equals(uri.getRawPath())) {
                return false;
            }
            if (noParams && !uri.getRawQuery().equals(null)) {
                return false;
            }
            if (params.size() > 0) {
                Map<String, String> requestParams = ParamsParser.parseParams(request);
                if (!requestParams.equals(params)) {
                    return false;
                }
            }
            if (headers.size() > 0) {
                Map<String, String> actualRequestHeaders = new HashMap<String, String>();
                for (Header header : request.getAllHeaders()) {
                    actualRequestHeaders.put(header.getName(), header.getValue());
                }
                if (!headers.equals(actualRequestHeaders)) {
                    return false;
                }
            }
            if (postBodyMatcher != null) {
                if (!(request instanceof HttpEntityEnclosingRequestBase)) {
                    return false;
                }
                HttpEntityEnclosingRequestBase postOrPut = (HttpEntityEnclosingRequestBase) request;
                try {
                    if (!postBodyMatcher.matches(postOrPut.getEntity())) {
                        return false;
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return true;
        }

        String getHostname() {
            return hostname;
        }

        String getPath() {
            return path;
        }

        String getParam(String key) {
            return params.get(key);
        }

        String getHeader(String key) {
            return headers.get(key);
        }

        boolean isNoParams() {
            return noParams;
        }

        String getMethod() {
            return method;
        }
    }

    public static class UriRegexMatcher implements RequestMatcher {
        private String method;
        private final Pattern uriRegex;

        public UriRegexMatcher(String method, String uriRegex) {
            this.method = method;
            this.uriRegex = Pattern.compile(uriRegex);
        }

        @Override
        public boolean matches(HttpRequest request) {
            return request.getRequestLine().getMethod().equals(method) &&
                    uriRegex.matcher(request.getRequestLine().getUri()).matches();
        }
    }
}
