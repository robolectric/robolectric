package com.xtremelabs.robolectric.shadows;

import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;
import com.xtremelabs.robolectric.util.RealObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.*;
import org.apache.http.auth.AuthSchemeRegistry;
import org.apache.http.client.*;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.cookie.CookieSpecRegistry;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.ClientParamsStack;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Implements(AbstractHttpClient.class)
public class ShadowAbstractHttpClient {
    private final Log log = LogFactory.getLog(getClass());

    @RealObject AbstractHttpClient httpClient;

    static List<HttpResponse> httpResponses = new ArrayList<HttpResponse>();
    static List<HttpRequest> httpRequests = new ArrayList<HttpRequest>();

    private HttpParams defaultParams;
    private HttpRequestExecutor requestExec;
    private ClientConnectionManager connManager;
    private ConnectionReuseStrategy reuseStrategy;
    private ConnectionKeepAliveStrategy keepAliveStrategy;
    private CookieSpecRegistry supportedCookieSpecs;
    private AuthSchemeRegistry supportedAuthSchemes;
    private BasicHttpProcessor httpProcessor;
    private HttpRequestRetryHandler retryHandler;
    private RedirectHandler redirectHandler;
    private AuthenticationHandler targetAuthHandler;
    private AuthenticationHandler proxyAuthHandler;
    private CookieStore cookieStore;
    private CredentialsProvider credsProvider;
    private HttpRoutePlanner routePlanner;
    private UserTokenHandler userTokenHandler;

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

    public void __constructor__(
            final ClientConnectionManager conman,
            final HttpParams params) {
        defaultParams = params;
        connManager = conman;
    }

    @Implementation public HttpParams getParams() {
        if (defaultParams == null) {
            defaultParams = (HttpParams) callMethodReflectively("createHttpParams");
        }
        return defaultParams;
    }

    @Implementation public ClientConnectionManager getConnectionManager() {
        if (connManager == null) {
            connManager = (ClientConnectionManager) callMethodReflectively("createClientConnectionManager");
        }
        return connManager;
    }

    @Implementation public CredentialsProvider getCredentialsProvider() {
        if (credsProvider == null) {
            credsProvider = (CredentialsProvider) callMethodReflectively("createCredentialsProvider");
        }
        return credsProvider;
    }

    @Implementation
    public void setParams(HttpParams params) {
        defaultParams = params;
    }

    @Implementation
    public final HttpRequestExecutor getRequestExecutor() {
        if (requestExec == null) {
            requestExec = (HttpRequestExecutor) callMethodReflectively("createRequestExecutor");
        }
        return requestExec;
    }

    @Implementation
    public final AuthSchemeRegistry getAuthSchemes() {
        if (supportedAuthSchemes == null) {
            supportedAuthSchemes = (AuthSchemeRegistry) callMethodReflectively("createAuthSchemeRegistry");
        }
        return supportedAuthSchemes;
    }

    @Implementation
    public void setAuthSchemes(final AuthSchemeRegistry authSchemeRegistry) {
        supportedAuthSchemes = authSchemeRegistry;
    }

    @Implementation
    public final CookieSpecRegistry getCookieSpecs() {
        if (supportedCookieSpecs == null) {
            supportedCookieSpecs = (CookieSpecRegistry) callMethodReflectively("createCookieSpecRegistry");
        }
        return supportedCookieSpecs;
    }

    @Implementation
    public void setCookieSpecs(final CookieSpecRegistry cookieSpecRegistry) {
        supportedCookieSpecs = cookieSpecRegistry;
    }

    @Implementation
    public final ConnectionReuseStrategy getConnectionReuseStrategy() {
        if (reuseStrategy == null) {
            reuseStrategy = (ConnectionReuseStrategy) callMethodReflectively("createConnectionReuseStrategy");
        }
        return reuseStrategy;
    }

    @Implementation
    public void setReuseStrategy(final ConnectionReuseStrategy reuseStrategy) {
        this.reuseStrategy = reuseStrategy;
    }

    @Implementation
    public final ConnectionKeepAliveStrategy getConnectionKeepAliveStrategy() {
        if (keepAliveStrategy == null) {
            keepAliveStrategy = (ConnectionKeepAliveStrategy) callMethodReflectively("createConnectionKeepAliveStrategy");
        }
        return keepAliveStrategy;
    }

    @Implementation
    public void setKeepAliveStrategy(final ConnectionKeepAliveStrategy keepAliveStrategy) {
        this.keepAliveStrategy = keepAliveStrategy;
    }

    @Implementation
    public final HttpRequestRetryHandler getHttpRequestRetryHandler() {
        if (retryHandler == null) {
            retryHandler = (HttpRequestRetryHandler) callMethodReflectively("createHttpRequestRetryHandler");
        }
        return retryHandler;
    }

    @Implementation
    public void setHttpRequestRetryHandler(final HttpRequestRetryHandler retryHandler) {
        this.retryHandler = retryHandler;
    }

    @Implementation
    public final RedirectHandler getRedirectHandler() {
        if (redirectHandler == null) {
            redirectHandler = (RedirectHandler) callMethodReflectively("createRedirectHandler");
        }
        return redirectHandler;
    }

    @Implementation
    public void setRedirectHandler(final RedirectHandler redirectHandler) {
        this.redirectHandler = redirectHandler;
    }

    @Implementation
    public final AuthenticationHandler getTargetAuthenticationHandler() {
        if (targetAuthHandler == null) {
            targetAuthHandler = (AuthenticationHandler) callMethodReflectively("createTargetAuthenticationHandler");
        }
        return targetAuthHandler;
    }

    @Implementation
    public void setTargetAuthenticationHandler(
            final AuthenticationHandler targetAuthHandler) {
        this.targetAuthHandler = targetAuthHandler;
    }

    @Implementation
    public final AuthenticationHandler getProxyAuthenticationHandler() {
        if (proxyAuthHandler == null) {
            proxyAuthHandler = (AuthenticationHandler) callMethodReflectively("createProxyAuthenticationHandler");
        }
        return proxyAuthHandler;
    }

    @Implementation
    public void setProxyAuthenticationHandler(
            final AuthenticationHandler proxyAuthHandler) {
        this.proxyAuthHandler = proxyAuthHandler;
    }

    @Implementation
    public final CookieStore getCookieStore() {
        if (cookieStore == null) {
            cookieStore = (CookieStore) callMethodReflectively("createCookieStore");
        }
        return cookieStore;
    }

    @Implementation
    public void setCookieStore(final CookieStore cookieStore) {
        this.cookieStore = cookieStore;
    }

    @Implementation
    public void setCredentialsProvider(final CredentialsProvider credsProvider) {
        this.credsProvider = credsProvider;
    }

    @Implementation
    public final HttpRoutePlanner getRoutePlanner() {
        if (this.routePlanner == null) {
            this.routePlanner = (HttpRoutePlanner) callMethodReflectively("createHttpRoutePlanner");
        }
        return this.routePlanner;
    }

    @Implementation
    public void setRoutePlanner(final HttpRoutePlanner routePlanner) {
        this.routePlanner = routePlanner;
    }

    @Implementation
    public final UserTokenHandler getUserTokenHandler() {
        if (this.userTokenHandler == null) {
            this.userTokenHandler = (UserTokenHandler) callMethodReflectively("createUserTokenHandler");
        }
        return this.userTokenHandler;
    }

    @Implementation
    public void setUserTokenHandler(final UserTokenHandler userTokenHandler) {
        this.userTokenHandler = userTokenHandler;
    }

    @Implementation
    public final BasicHttpProcessor getHttpProcessor() {
        if (httpProcessor == null) {
            httpProcessor = (BasicHttpProcessor) callMethodReflectively("createHttpProcessor");
        }
        return httpProcessor;
    }

    @Implementation
    public void addResponseInterceptor(final HttpResponseInterceptor itcp) {
        getHttpProcessor().addInterceptor(itcp);
    }

    @Implementation
    public void addResponseInterceptor(final HttpResponseInterceptor itcp, int index) {
        getHttpProcessor().addInterceptor(itcp, index);
    }

    @Implementation
    public HttpResponseInterceptor getResponseInterceptor(int index) {
        return getHttpProcessor().getResponseInterceptor(index);
    }

    @Implementation
    public int getResponseInterceptorCount() {
        return getHttpProcessor().getResponseInterceptorCount();
    }

    @Implementation
    public void clearResponseInterceptors() {
        getHttpProcessor().clearResponseInterceptors();
    }

    @Implementation
    public void removeResponseInterceptorByClass(Class<? extends HttpResponseInterceptor> clazz) {
        getHttpProcessor().removeResponseInterceptorByClass(clazz);
    }

    @Implementation
    public void addRequestInterceptor(final HttpRequestInterceptor itcp) {
        getHttpProcessor().addInterceptor(itcp);
    }

    @Implementation
    public void addRequestInterceptor(final HttpRequestInterceptor itcp, int index) {
        getHttpProcessor().addInterceptor(itcp, index);
    }

    @Implementation
    public HttpRequestInterceptor getRequestInterceptor(int index) {
        return getHttpProcessor().getRequestInterceptor(index);
    }

    @Implementation
    public int getRequestInterceptorCount() {
        return getHttpProcessor().getRequestInterceptorCount();
    }

    @Implementation
    public void clearRequestInterceptors() {
        getHttpProcessor().clearRequestInterceptors();
    }

    @Implementation
    public void removeRequestInterceptorByClass(Class<? extends HttpRequestInterceptor> clazz) {
        getHttpProcessor().removeRequestInterceptorByClass(clazz);
    }

    @Implementation
    public final HttpResponse execute(HttpUriRequest request)
            throws IOException, ClientProtocolException {

        return execute(request, (HttpContext) null);
    }

    @Implementation
    public final HttpResponse execute(HttpUriRequest request,
                                      HttpContext context)
            throws IOException, ClientProtocolException {
        if (request == null) {
            throw new IllegalArgumentException("Request must not be null.");
        }
        return execute(determineTarget(request), request, context);
    }

    @Implementation
    public HttpHost determineTarget(HttpUriRequest request) {
        // A null target may be acceptable if there is a default target.
        // Otherwise, the null target is detected in the director.
        HttpHost target = null;

        URI requestURI = request.getURI();
        if (requestURI.isAbsolute()) {
            target = new HttpHost(requestURI.getHost(), requestURI.getPort(), requestURI.getScheme());
        }
        return target;
    }

    @Implementation
    public final HttpResponse execute(HttpHost target, HttpRequest request) throws IOException, ClientProtocolException {
        return execute(target, request, (HttpContext) null);
    }

    @Implementation
    public final HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context) throws IOException, ClientProtocolException {
        if (request == null) {
            throw new IllegalArgumentException("Request must not be null.");
        }

        HttpContext execContext;
        HttpContext defaultContext = (HttpContext) callMethodReflectively("createHttpContext");
        if (context == null) {
            execContext = defaultContext;
        } else {
            execContext = new DefaultedHttpContext(context, defaultContext);
        }

        RequestDirector director = createClientRequestDirector(
                getRequestExecutor(),
                getConnectionManager(),
                getConnectionReuseStrategy(),
                getConnectionKeepAliveStrategy(),
                getRoutePlanner(),
                getHttpProcessor().copy(),
                getHttpRequestRetryHandler(),
                getRedirectHandler(),
                getTargetAuthenticationHandler(),
                getProxyAuthenticationHandler(),
                getUserTokenHandler(),
                determineParams(request));

        try {
            return director.execute(target, request, execContext);
        } catch (HttpException httpException) {
            throw new ClientProtocolException(httpException);
        }
    }

    @Implementation
    public RequestDirector createClientRequestDirector(
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
            final UserTokenHandler stateHandler,
            final HttpParams params) {

        return new RequestDirector() {

            @Override
            public HttpResponse execute(HttpHost httpHost, HttpRequest httpRequest, HttpContext httpContext) throws HttpException, IOException {
                if (httpResponses.isEmpty()) {
                    throw new RuntimeException("Unexpected call to execute, no pending responses are available.");
                }
                httpRequests.add(httpRequest);
                return httpResponses.remove(0);
            }
        };
    }

    @Implementation
    public HttpParams determineParams(HttpRequest req) {
        return new ClientParamsStack(null, getParams(), req.getParams(), null);
    }

    @Implementation
    public <T> T execute(
            final HttpUriRequest request,
            final ResponseHandler<? extends T> responseHandler)
            throws IOException, ClientProtocolException {
        return execute(request, responseHandler, null);
    }

    @Implementation
    public <T> T execute(
            final HttpUriRequest request,
            final ResponseHandler<? extends T> responseHandler,
            final HttpContext context)
            throws IOException, ClientProtocolException {
        HttpHost target = determineTarget(request);
        return execute(target, request, responseHandler, context);
    }

    @Implementation
    public <T> T execute(
            final HttpHost target,
            final HttpRequest request,
            final ResponseHandler<? extends T> responseHandler)
            throws IOException, ClientProtocolException {
        return execute(target, request, responseHandler, null);
    }

    @Implementation
    public <T> T execute(final HttpHost target, final HttpRequest request, final ResponseHandler<? extends T> responseHandler, final HttpContext context)
            throws IOException, ClientProtocolException {

        if (responseHandler == null) {
            throw new IllegalArgumentException("Response handler must not be null.");
        }

        HttpResponse response = execute(target, request, context);

        T result;
        try {
            result = responseHandler.handleResponse(response);
        } catch (Throwable t) {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                try {
                    entity.consumeContent();
                } catch (Throwable t2) {
                    // Log this exception. The original exception is more
                    // important and will be thrown to the caller.
                    this.log.warn("Error consuming content after an exception.", t2);
                }
            }

            if (t instanceof Error) {
                throw (Error) t;
            }

            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            }

            if (t instanceof IOException) {
                throw (IOException) t;
            }

            throw new UndeclaredThrowableException(t);
        }

        // Handling the response was successful. Ensure that the content has
        // been fully consumed.
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            // Let this exception go to the caller.
            entity.consumeContent();
        }

        return result;
    }

    private Object callMethodReflectively(String methodName) {
        try {
            Method method = AbstractHttpClient.class.getDeclaredMethod(methodName);
            method.setAccessible(true);
            return method.invoke(httpClient);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

