package com.xtremelabs.robolectric.shadows;

import com.xtremelabs.robolectric.util.HttpRequestData;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;
import com.xtremelabs.robolectric.util.RealObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.*;
import org.apache.http.client.AuthenticationHandler;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.UserTokenHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.impl.client.DefaultRequestDirector;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestExecutor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(DefaultRequestDirector.class)
public class ShadowDefaultRequestDirector {
    static List<HttpResponse> httpResponses = new ArrayList<HttpResponse>();
    static List<HttpRequestData> httpRequestDatas = new ArrayList<HttpRequestData>();
    static List<ResponseRule> httpResponseRules = new ArrayList<ResponseRule>();
    static HttpResponse defaultHttpResponse;

    @RealObject DefaultRequestDirector realObject;

    protected Log log;
    protected ClientConnectionManager connectionManager;
    protected HttpRoutePlanner httpRoutePlanner;
    protected ConnectionReuseStrategy connectionReuseStrategy;
    protected ConnectionKeepAliveStrategy connectionKeepAliveStrategy;
    protected HttpRequestExecutor httpRequestExecutor;
    protected HttpProcessor httpProcessor;
    protected HttpRequestRetryHandler httpRequestRetryHandler;
    protected RedirectHandler redirectHandler;
    protected AuthenticationHandler targetAuthenticationHandler;
    protected AuthenticationHandler proxyAuthenticationHandler;
    protected UserTokenHandler userTokenHandler;
    protected HttpParams httpParams;

    public void __constructor__(
            Log log,
            HttpRequestExecutor requestExec,
            ClientConnectionManager conman,
            ConnectionReuseStrategy reustrat,
            ConnectionKeepAliveStrategy kastrat,
            HttpRoutePlanner rouplan,
            HttpProcessor httpProcessor,
            HttpRequestRetryHandler retryHandler,
            RedirectHandler redirectHandler,
            AuthenticationHandler targetAuthHandler,
            AuthenticationHandler proxyAuthHandler,
            UserTokenHandler userTokenHandler,
            HttpParams params) {
        this.log               = log;
        this.httpRequestExecutor = requestExec;
        this.connectionManager = conman;
        this.connectionReuseStrategy = reustrat;
        this.connectionKeepAliveStrategy = kastrat;
        this.httpRoutePlanner = rouplan;
        this.httpProcessor     = httpProcessor;
        this.httpRequestRetryHandler = retryHandler;
        this.redirectHandler = redirectHandler;
        this.targetAuthenticationHandler = targetAuthHandler;
        this.proxyAuthenticationHandler = proxyAuthHandler;
        this.userTokenHandler  = userTokenHandler;
        this.httpParams = params;
    }

    public void __constructor__(
            HttpRequestExecutor requestExec,
            ClientConnectionManager conman,
            ConnectionReuseStrategy reustrat,
            ConnectionKeepAliveStrategy kastrat,
            HttpRoutePlanner rouplan,
            HttpProcessor httpProcessor,
            HttpRequestRetryHandler retryHandler,
            RedirectHandler redirectHandler,
            AuthenticationHandler targetAuthHandler,
            AuthenticationHandler proxyAuthHandler,
            UserTokenHandler userTokenHandler,
            HttpParams params) {
        __constructor__(
                LogFactory.getLog(DefaultRequestDirector.class),
                requestExec,
                conman,
                reustrat,
                kastrat,
                rouplan,
                httpProcessor,
                retryHandler,
                redirectHandler,
                targetAuthHandler,
                proxyAuthHandler,
                userTokenHandler,
                params);
    }

    public static void reset() {
        httpResponses.clear();
        httpRequestDatas.clear();
        httpResponseRules.clear();
        defaultHttpResponse = null;
    }

    public static void addPendingHttpResponse(int statusCode, String responseBody) {
        addPendingHttpResponse(new TestHttpResponse(statusCode, responseBody));
    }

    public static void addPendingHttpResponse(HttpResponse httpResponse) {
        httpResponses.add(httpResponse);
    }

    public static HttpRequest getSentHttpRequest(int index) {
        return getSentHttpRequestData(index).getHttpRequest();
    }

    public static HttpRequestData getSentHttpRequestData(int index) {
        return httpRequestDatas.get(index);
    }

    @Implementation
    public HttpResponse execute(HttpHost httpHost, HttpRequest httpRequest, HttpContext httpContext) throws HttpException, IOException {
        HttpResponse httpResponse = findResponse(httpRequest);

        if (httpResponse == null) {
            throw new RuntimeException("Unexpected call to execute, no pending responses are available. See Robolectric.addPendingResponse().");
        }

        httpRequestDatas.add(new HttpRequestData(httpRequest, httpHost, httpContext, this.realObject));
        
        return httpResponse;
    }

    private HttpResponse findResponse(HttpRequest httpRequest) {
        if (!httpResponses.isEmpty()) {
            return httpResponses.remove(0);
        }

        for (ResponseRule httpResponseRule : httpResponseRules) {
            if (httpResponseRule.matches(httpRequest)) {
                return httpResponseRule.getResponse();
            }
        }

        return defaultHttpResponse;
    }

    public Log getLog() {
        return log;
    }

    public ClientConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public HttpRoutePlanner getHttpRoutePlanner() {
        return httpRoutePlanner;
    }

    public ConnectionReuseStrategy getConnectionReuseStrategy() {
        return connectionReuseStrategy;
    }

    public ConnectionKeepAliveStrategy getConnectionKeepAliveStrategy() {
        return connectionKeepAliveStrategy;
    }

    public HttpRequestExecutor getHttpRequestExecutor() {
        return httpRequestExecutor;
    }

    public HttpProcessor getHttpProcessor() {
        return httpProcessor;
    }

    public HttpRequestRetryHandler getHttpRequestRetryHandler() {
        return httpRequestRetryHandler;
    }

    public RedirectHandler getRedirectHandler() {
        return redirectHandler;
    }

    public AuthenticationHandler getTargetAuthenticationHandler() {
        return targetAuthenticationHandler;
    }

    public AuthenticationHandler getProxyAuthenticationHandler() {
        return proxyAuthenticationHandler;
    }

    public UserTokenHandler getUserTokenHandler() {
        return userTokenHandler;
    }

    public HttpParams getHttpParams() {
        return httpParams;
    }

    public static void addHttpResponseRule(String method, String uri, HttpResponse response) {
        addHttpResponseRule(new DefaultRequestMatcher(method, uri), response);
    }

    public static void addHttpResponseRule(String uri, HttpResponse response) {
        addHttpResponseRule(new DefaultRequestMatcher(HttpGet.METHOD_NAME, uri), response);
    }

    public static void addHttpResponseRule(String uri, String response) {
        addHttpResponseRule(new DefaultRequestMatcher(HttpGet.METHOD_NAME, uri), new TestHttpResponse(200, response));
    }

    public static void addHttpResponseRule(RequestMatcher requestMatcher, HttpResponse response) {
        addHttpResponseRule(new ResponseRule(response, requestMatcher));
    }

    public static void addHttpResponseRule(ResponseRule responseRule) {
        httpResponseRules.add(responseRule);
    }

    public static void setDefaultHttpResponse(TestHttpResponse httpDefaultResponse) {
        ShadowDefaultRequestDirector.defaultHttpResponse = httpDefaultResponse;
    }

    public interface RequestMatcher {
        public boolean matches(HttpRequest request);
    }

    private static class ResponseRule {
        private HttpResponse responseToGive;

        private RequestMatcher requestMatcher;

        private ResponseRule(HttpResponse responseToGive, RequestMatcher requestMatcher) {
            this.responseToGive = responseToGive;
            this.requestMatcher = requestMatcher;
        }

        public boolean matches(HttpRequest request) {
            return requestMatcher.matches(request);
        }
        public HttpResponse getResponse() {
            return responseToGive;
        }
    }

    private static class DefaultRequestMatcher implements RequestMatcher {
        private String method;
        private String uri;

        private DefaultRequestMatcher(String method, String uri) {
            this.method = method;
            this.uri = uri;
        }

        @Override public boolean matches(HttpRequest request) {
            return request.getRequestLine().getMethod().equals(method) &&
                    request.getRequestLine().getUri().equals(uri);
        }
    }
}

