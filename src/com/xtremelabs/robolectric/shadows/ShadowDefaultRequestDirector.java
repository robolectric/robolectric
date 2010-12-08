package com.xtremelabs.robolectric.shadows;

import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.*;
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
import java.util.ArrayList;
import java.util.List;

@Implements(DefaultRequestDirector.class)
public class ShadowDefaultRequestDirector {
    static List<HttpResponse> httpResponses = new ArrayList<HttpResponse>();
    static List<HttpRequest> httpRequests = new ArrayList<HttpRequest>();

    protected Log log;
    protected ClientConnectionManager connManager;
    protected HttpRoutePlanner routePlanner;
    protected ConnectionReuseStrategy reuseStrategy;
    protected ConnectionKeepAliveStrategy keepAliveStrategy;
    protected HttpRequestExecutor requestExec;
    protected HttpProcessor httpProcessor;
    protected HttpRequestRetryHandler retryHandler;
    protected RedirectHandler redirectHandler;
    protected AuthenticationHandler targetAuthHandler;
    protected AuthenticationHandler proxyAuthHandler;
    protected UserTokenHandler userTokenHandler;
    protected HttpParams params;

    public void __constructor__(
            final Log log,
            final HttpRequestExecutor requestExec,
            final ClientConnectionManager conman,
            final ConnectionReuseStrategy reustrat,
            final ConnectionKeepAliveStrategy kastrat,
            final HttpRoutePlanner rouplan,
            final HttpProcessor httpProcessor,
            final HttpRequestRetryHandler retryHandler,
            final RedirectHandler redirectHandler,
            final AuthenticationHandler targetAuthHandler,
            final AuthenticationHandler proxyAuthHandler,
            final UserTokenHandler userTokenHandler,
            final HttpParams params) {
        this.log               = log;
        this.requestExec       = requestExec;
        this.connManager       = conman;
        this.reuseStrategy     = reustrat;
        this.keepAliveStrategy = kastrat;
        this.routePlanner      = rouplan;
        this.httpProcessor     = httpProcessor;
        this.retryHandler      = retryHandler;
        this.redirectHandler   = redirectHandler;
        this.targetAuthHandler = targetAuthHandler;
        this.proxyAuthHandler  = proxyAuthHandler;
        this.userTokenHandler  = userTokenHandler;
        this.params            = params;
    }

    public void __constructor__(
            final HttpRequestExecutor requestExec,
            final ClientConnectionManager conman,
            final ConnectionReuseStrategy reustrat,
            final ConnectionKeepAliveStrategy kastrat,
            final HttpRoutePlanner rouplan,
            final HttpProcessor httpProcessor,
            final HttpRequestRetryHandler retryHandler,
            final RedirectHandler redirectHandler,
            final AuthenticationHandler targetAuthHandler,
            final AuthenticationHandler proxyAuthHandler,
            final UserTokenHandler userTokenHandler,
            final HttpParams params) {
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
        httpRequests.clear();
    }

    public static void addPendingResponse(int statusCode, String responseBody) {
        addPendingResponse(new TestHttpResponse(statusCode, responseBody));
    }

    public static void addPendingResponse(HttpResponse httpResponse) {
        httpResponses.add(httpResponse);
    }

    public static HttpRequest getRequest(int index) {
        return httpRequests.get(index);
    }

    @Implementation
    public HttpResponse execute(HttpHost httpHost, HttpRequest httpRequest, HttpContext httpContext) throws HttpException, IOException {
        if (httpResponses.isEmpty()) {
            throw new RuntimeException("Unexpected call to execute, no pending responses are available. See Robolectric.addPendingResponse().");
        }
        httpRequests.add(httpRequest);
        return httpResponses.remove(0);
    }
}

