package com.xtremelabs.robolectric.shadows;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;
import com.xtremelabs.robolectric.tester.org.apache.http.HttpRequestInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.AuthenticationHandler;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.UserTokenHandler;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.impl.client.DefaultRequestDirector;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestExecutor;

import java.io.IOException;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(DefaultRequestDirector.class)
public class ShadowDefaultRequestDirector {
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

    com.xtremelabs.robolectric.tester.org.apache.http.impl.client.DefaultRequestDirector redirector;

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
        this.log = log;
        this.httpRequestExecutor = requestExec;
        this.connectionManager = conman;
        this.connectionReuseStrategy = reustrat;
        this.connectionKeepAliveStrategy = kastrat;
        this.httpRoutePlanner = rouplan;
        this.httpProcessor = httpProcessor;
        this.httpRequestRetryHandler = retryHandler;
        this.redirectHandler = redirectHandler;
        this.targetAuthenticationHandler = targetAuthHandler;
        this.proxyAuthenticationHandler = proxyAuthHandler;
        this.userTokenHandler = userTokenHandler;
        this.httpParams = params;

        try {
            redirector = new com.xtremelabs.robolectric.tester.org.apache.http.impl.client.DefaultRequestDirector(
                  log,
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
                  params
            );
        } catch (IllegalArgumentException ignored) {
            Robolectric.getFakeHttpLayer().interceptHttpRequests(true);
        }
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

    public static HttpRequest getSentHttpRequest(int index) {
        return getSentHttpRequestInfo(index).getHttpRequest();
    }

    public static HttpRequest getLatestSentHttpRequest() {
        return getLatestSentHttpRequestInfo().getHttpRequest();
    }

    public static HttpRequestInfo getLatestSentHttpRequestInfo() {
        int requestCount = Robolectric.getFakeHttpLayer().getSentHttpRequestInfos().size();
        return Robolectric.getFakeHttpLayer().getSentHttpRequestInfo(requestCount - 1);
    }

    public static HttpRequestInfo getSentHttpRequestInfo(int index) {
        return Robolectric.getFakeHttpLayer().getSentHttpRequestInfo(index);
    }

    @Implementation
    public HttpResponse execute(HttpHost httpHost, HttpRequest httpRequest, HttpContext httpContext) throws HttpException, IOException {
        if (Robolectric.getFakeHttpLayer().isInterceptingHttpRequests()) {
            return Robolectric.getFakeHttpLayer().emulateRequest(httpHost, httpRequest, httpContext, realObject);
        } else {
            Robolectric.getFakeHttpLayer().addRequestInfo(new HttpRequestInfo(httpRequest, httpHost, httpContext, redirector));
            HttpResponse response = redirector.execute(httpHost, httpRequest, httpContext);
            Robolectric.getFakeHttpLayer().addHttpResponse(response);
            return response;
        }
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
}