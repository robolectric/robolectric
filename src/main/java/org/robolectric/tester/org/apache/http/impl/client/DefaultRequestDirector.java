// copied verbatim (except for constructor access) from httpclient-4.0.3 sources

/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package com.xtremelabs.robolectric.tester.org.apache.http.impl.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.*;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.auth.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.AbortableHttpRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.conn.*;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.routing.BasicRouteDirector;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.HttpRouteDirector;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.EntityEnclosingRequestWrapper;
import org.apache.http.impl.client.RequestWrapper;
import org.apache.http.impl.client.RoutedRequest;
import org.apache.http.impl.client.TunnelRefusedException;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestExecutor;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Default implementation of {@link RequestDirector}.
 * <p>
 * The following parameters can be used to customize the behavior of this
 * class:
 * <ul>
 *  <li>{@link org.apache.http.params.CoreProtocolPNames#PROTOCOL_VERSION}</li>
 *  <li>{@link org.apache.http.params.CoreProtocolPNames#STRICT_TRANSFER_ENCODING}</li>
 *  <li>{@link org.apache.http.params.CoreProtocolPNames#HTTP_ELEMENT_CHARSET}</li>
 *  <li>{@link org.apache.http.params.CoreProtocolPNames#USE_EXPECT_CONTINUE}</li>
 *  <li>{@link org.apache.http.params.CoreProtocolPNames#WAIT_FOR_CONTINUE}</li>
 *  <li>{@link org.apache.http.params.CoreProtocolPNames#USER_AGENT}</li>
 *  <li>{@link org.apache.http.params.CoreConnectionPNames#SOCKET_BUFFER_SIZE}</li>
 *  <li>{@link org.apache.http.params.CoreConnectionPNames#MAX_LINE_LENGTH}</li>
 *  <li>{@link org.apache.http.params.CoreConnectionPNames#MAX_HEADER_COUNT}</li>
 *  <li>{@link org.apache.http.params.CoreConnectionPNames#SO_TIMEOUT}</li>
 *  <li>{@link org.apache.http.params.CoreConnectionPNames#SO_LINGER}</li>
 *  <li>{@link org.apache.http.params.CoreConnectionPNames#TCP_NODELAY}</li>
 *  <li>{@link org.apache.http.params.CoreConnectionPNames#CONNECTION_TIMEOUT}</li>
 *  <li>{@link org.apache.http.params.CoreConnectionPNames#STALE_CONNECTION_CHECK}</li>
 *  <li>{@link org.apache.http.conn.params.ConnRoutePNames#FORCED_ROUTE}</li>
 *  <li>{@link org.apache.http.conn.params.ConnRoutePNames#LOCAL_ADDRESS}</li>
 *  <li>{@link org.apache.http.conn.params.ConnRoutePNames#DEFAULT_PROXY}</li>
 *  <li>{@link org.apache.http.conn.params.ConnManagerPNames#TIMEOUT}</li>
 *  <li>{@link org.apache.http.conn.params.ConnManagerPNames#MAX_CONNECTIONS_PER_ROUTE}</li>
 *  <li>{@link org.apache.http.conn.params.ConnManagerPNames#MAX_TOTAL_CONNECTIONS}</li>
 *  <li>{@link org.apache.http.cookie.params.CookieSpecPNames#DATE_PATTERNS}</li>
 *  <li>{@link org.apache.http.cookie.params.CookieSpecPNames#SINGLE_COOKIE_HEADER}</li>
 *  <li>{@link org.apache.http.auth.params.AuthPNames#CREDENTIAL_CHARSET}</li>
 *  <li>{@link org.apache.http.client.params.ClientPNames#COOKIE_POLICY}</li>
 *  <li>{@link org.apache.http.client.params.ClientPNames#HANDLE_AUTHENTICATION}</li>
 *  <li>{@link org.apache.http.client.params.ClientPNames#HANDLE_REDIRECTS}</li>
 *  <li>{@link org.apache.http.client.params.ClientPNames#MAX_REDIRECTS}</li>
 *  <li>{@link org.apache.http.client.params.ClientPNames#ALLOW_CIRCULAR_REDIRECTS}</li>
 *  <li>{@link org.apache.http.client.params.ClientPNames#VIRTUAL_HOST}</li>
 *  <li>{@link org.apache.http.client.params.ClientPNames#DEFAULT_HOST}</li>
 *  <li>{@link org.apache.http.client.params.ClientPNames#DEFAULT_HEADERS}</li>
 * </ul>
 *
 * @since 4.0
 */
@NotThreadSafe // e.g. managedConn
public class DefaultRequestDirector implements RequestDirector {

    private final Log log;

    /** The connection manager. */
    protected final ClientConnectionManager connManager;

    /** The route planner. */
    protected final HttpRoutePlanner routePlanner;

    /** The connection re-use strategy. */
    protected final ConnectionReuseStrategy reuseStrategy;

    /** The keep-alive duration strategy. */
    protected final ConnectionKeepAliveStrategy keepAliveStrategy;

    /** The request executor. */
    protected final HttpRequestExecutor requestExec;

    /** The HTTP protocol processor. */
    protected final HttpProcessor httpProcessor;

    /** The request retry handler. */
    protected final HttpRequestRetryHandler retryHandler;

    /** The redirect handler. */
    protected final RedirectHandler redirectHandler;

    /** The target authentication handler. */
    protected final AuthenticationHandler targetAuthHandler;

    /** The proxy authentication handler. */
    protected final AuthenticationHandler proxyAuthHandler;

    /** The user token handler. */
    protected final UserTokenHandler userTokenHandler;

    /** The HTTP parameters. */
    protected final HttpParams params;

    /** The currently allocated connection. */
    protected ManagedClientConnection managedConn;

    protected final AuthState targetAuthState;

    protected final AuthState proxyAuthState;

    private int redirectCount;

    private int maxRedirects;

    private HttpHost virtualHost;

    public DefaultRequestDirector(
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

        if (log == null) {
            throw new IllegalArgumentException
                ("Log may not be null.");
        }
        if (requestExec == null) {
            throw new IllegalArgumentException
                ("Request executor may not be null.");
        }
        if (conman == null) {
            throw new IllegalArgumentException
                ("Client connection manager may not be null.");
        }
        if (reustrat == null) {
            throw new IllegalArgumentException
                ("Connection reuse strategy may not be null.");
        }
        if (kastrat == null) {
            throw new IllegalArgumentException
                ("Connection keep alive strategy may not be null.");
        }
        if (rouplan == null) {
            throw new IllegalArgumentException
                ("Route planner may not be null.");
        }
        if (httpProcessor == null) {
            throw new IllegalArgumentException
                ("HTTP protocol processor may not be null.");
        }
        if (retryHandler == null) {
            throw new IllegalArgumentException
                ("HTTP request retry handler may not be null.");
        }
        if (redirectHandler == null) {
            throw new IllegalArgumentException
                ("Redirect handler may not be null.");
        }
        if (targetAuthHandler == null) {
            throw new IllegalArgumentException
                ("Target authentication handler may not be null.");
        }
        if (proxyAuthHandler == null) {
            throw new IllegalArgumentException
                ("Proxy authentication handler may not be null.");
        }
        if (userTokenHandler == null) {
            throw new IllegalArgumentException
                ("User token handler may not be null.");
        }
        if (params == null) {
            throw new IllegalArgumentException
                ("HTTP parameters may not be null");
        }
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

        this.managedConn       = null;

        this.redirectCount = 0;
        this.maxRedirects = this.params.getIntParameter(ClientPNames.MAX_REDIRECTS, 100);
        this.targetAuthState = new AuthState();
        this.proxyAuthState = new AuthState();
    } // constructor

    public DefaultRequestDirector(
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
        this(LogFactory.getLog(DefaultRequestDirector.class),
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

    private RequestWrapper wrapRequest(
            final HttpRequest request) throws ProtocolException {
        if (request instanceof HttpEntityEnclosingRequest) {
            return new EntityEnclosingRequestWrapper(
                    (HttpEntityEnclosingRequest) request);
        } else {
            return new RequestWrapper(
                    request);
        }
    }


    protected void rewriteRequestURI(
            final RequestWrapper request,
            final HttpRoute route) throws ProtocolException {
        try {

            URI uri = request.getURI();
            if (route.getProxyHost() != null && !route.isTunnelled()) {
                // Make sure the request URI is absolute
                if (!uri.isAbsolute()) {
                    HttpHost target = route.getTargetHost();
                    uri = URIUtils.rewriteURI(uri, target);
                    request.setURI(uri);
                }
            } else {
                // Make sure the request URI is relative
                if (uri.isAbsolute()) {
                    uri = URIUtils.rewriteURI(uri, null);
                    request.setURI(uri);
                }
            }

        } catch (URISyntaxException ex) {
            throw new ProtocolException("Invalid URI: " +
                    request.getRequestLine().getUri(), ex);
        }
    }


    // non-javadoc, see interface ClientRequestDirector
    public HttpResponse execute(HttpHost target, HttpRequest request,
                                HttpContext context)
        throws HttpException, IOException {

        HttpRequest orig = request;
        RequestWrapper origWrapper = wrapRequest(orig);
        origWrapper.setParams(params);
        HttpRoute origRoute = determineRoute(target, origWrapper, context);

        virtualHost = (HttpHost) orig.getParams().getParameter(
                ClientPNames.VIRTUAL_HOST);

        RoutedRequest roureq = new RoutedRequest(origWrapper, origRoute);

        long timeout = ConnManagerParams.getTimeout(params);

        int execCount = 0;

        boolean reuse = false;
        boolean done = false;
        try {
            HttpResponse response = null;
            while (!done) {
                // In this loop, the RoutedRequest may be replaced by a
                // followup request and route. The request and route passed
                // in the method arguments will be replaced. The original
                // request is still available in 'orig'.

                RequestWrapper wrapper = roureq.getRequest();
                HttpRoute route = roureq.getRoute();
                response = null;

                // See if we have a user token bound to the execution context
                Object userToken = context.getAttribute(ClientContext.USER_TOKEN);

                // Allocate connection if needed
                if (managedConn == null) {
                    ClientConnectionRequest connRequest = connManager.requestConnection(
                            route, userToken);
                    if (orig instanceof AbortableHttpRequest) {
                        ((AbortableHttpRequest) orig).setConnectionRequest(connRequest);
                    }

                    try {
                        managedConn = connRequest.getConnection(timeout, TimeUnit.MILLISECONDS);
                    } catch(InterruptedException interrupted) {
                        InterruptedIOException iox = new InterruptedIOException();
                        iox.initCause(interrupted);
                        throw iox;
                    }

                    if (HttpConnectionParams.isStaleCheckingEnabled(params)) {
                        // validate connection
                        if (managedConn.isOpen()) {
                            this.log.debug("Stale connection check");
                            if (managedConn.isStale()) {
                                this.log.debug("Stale connection detected");
                                managedConn.close();
                            }
                        }
                    }
                }

                if (orig instanceof AbortableHttpRequest) {
                    ((AbortableHttpRequest) orig).setReleaseTrigger(managedConn);
                }

                // Reopen connection if needed
                if (!managedConn.isOpen()) {
                    managedConn.open(route, context, params);
                } else {
                    managedConn.setSocketTimeout(HttpConnectionParams.getSoTimeout(params));
                }

                try {
                    establishRoute(route, context);
                } catch (TunnelRefusedException ex) {
                    if (this.log.isDebugEnabled()) {
                        this.log.debug(ex.getMessage());
                    }
                    response = ex.getResponse();
                    break;
                }

                // Reset headers on the request wrapper
                wrapper.resetHeaders();

                // Re-write request URI if needed
                rewriteRequestURI(wrapper, route);

                // Use virtual host if set
                target = virtualHost;

                if (target == null) {
                    target = route.getTargetHost();
                }

                HttpHost proxy = route.getProxyHost();

                // Populate the execution context
                context.setAttribute(ExecutionContext.HTTP_TARGET_HOST,
                        target);
                context.setAttribute(ExecutionContext.HTTP_PROXY_HOST,
                        proxy);
                context.setAttribute(ExecutionContext.HTTP_CONNECTION,
                        managedConn);
                context.setAttribute(ClientContext.TARGET_AUTH_STATE,
                        targetAuthState);
                context.setAttribute(ClientContext.PROXY_AUTH_STATE,
                        proxyAuthState);

                // Run request protocol interceptors
                requestExec.preProcess(wrapper, httpProcessor, context);

                boolean retrying = true;
                Exception retryReason = null;
                while (retrying) {
                    // Increment total exec count (with redirects)
                    execCount++;
                    // Increment exec count for this particular request
                    wrapper.incrementExecCount();
                    if (!wrapper.isRepeatable()) {
                        this.log.debug("Cannot retry non-repeatable request");
                        if (retryReason != null) {
                            throw new NonRepeatableRequestException("Cannot retry request " +
                                "with a non-repeatable request entity.  The cause lists the " +
                                "reason the original request failed: " + retryReason);
                        } else {
                            throw new NonRepeatableRequestException("Cannot retry request " +
                                    "with a non-repeatable request entity.");
                        }
                    }

                    try {
                        if (this.log.isDebugEnabled()) {
                            this.log.debug("Attempt " + execCount + " to execute request");
                        }
                        response = requestExec.execute(wrapper, managedConn, context);
                        retrying = false;

                    } catch (IOException ex) {
                        this.log.debug("Closing the connection.");
                        managedConn.close();
                        if (retryHandler.retryRequest(ex, wrapper.getExecCount(), context)) {
                            if (this.log.isInfoEnabled()) {
                                this.log.info("I/O exception ("+ ex.getClass().getName() +
                                        ") caught when processing request: "
                                        + ex.getMessage());
                            }
                            if (this.log.isDebugEnabled()) {
                                this.log.debug(ex.getMessage(), ex);
                            }
                            this.log.info("Retrying request");
                            retryReason = ex;
                        } else {
                            throw ex;
                        }

                        // If we have a direct route to the target host
                        // just re-open connection and re-try the request
                        if (!route.isTunnelled()) {
                            this.log.debug("Reopening the direct connection.");
                            managedConn.open(route, context, params);
                        } else {
                            // otherwise give up
                            this.log.debug("Proxied connection. Need to start over.");
                            retrying = false;
                        }

                    }

                }

                if (response == null) {
                    // Need to start over
                    continue;
                }

                // Run response protocol interceptors
                response.setParams(params);
                requestExec.postProcess(response, httpProcessor, context);


                // The connection is in or can be brought to a re-usable state.
                reuse = reuseStrategy.keepAlive(response, context);
                if (reuse) {
                    // Set the idle duration of this connection
                    long duration = keepAliveStrategy.getKeepAliveDuration(response, context);
                    managedConn.setIdleDuration(duration, TimeUnit.MILLISECONDS);

                    if (this.log.isDebugEnabled()) {
                        if (duration >= 0) {
                            this.log.debug("Connection can be kept alive for " + duration + " ms");
                        } else {
                            this.log.debug("Connection can be kept alive indefinitely");
                        }
                    }
                }

                RoutedRequest followup = handleResponse(roureq, response, context);
                if (followup == null) {
                    done = true;
                } else {
                    if (reuse) {
                        // Make sure the response body is fully consumed, if present
                        HttpEntity entity = response.getEntity();
                        if (entity != null) {
                            entity.consumeContent();
                        }
                        // entity consumed above is not an auto-release entity,
                        // need to mark the connection re-usable explicitly
                        managedConn.markReusable();
                    } else {
                        managedConn.close();
                    }
                    // check if we can use the same connection for the followup
                    if (!followup.getRoute().equals(roureq.getRoute())) {
                        releaseConnection();
                    }
                    roureq = followup;
                }

                if (managedConn != null && userToken == null) {
                    userToken = userTokenHandler.getUserToken(context);
                    context.setAttribute(ClientContext.USER_TOKEN, userToken);
                    if (userToken != null) {
                        managedConn.setState(userToken);
                    }
                }

            } // while not done


            // check for entity, release connection if possible
            if ((response == null) || (response.getEntity() == null) ||
                !response.getEntity().isStreaming()) {
                // connection not needed and (assumed to be) in re-usable state
                if (reuse)
                    managedConn.markReusable();
                releaseConnection();
            } else {
                // install an auto-release entity
                HttpEntity entity = response.getEntity();
                entity = new BasicManagedEntity(entity, managedConn, reuse);
                response.setEntity(entity);
            }

            return response;

        } catch (HttpException ex) {
            abortConnection();
            throw ex;
        } catch (IOException ex) {
            abortConnection();
            throw ex;
        } catch (RuntimeException ex) {
            abortConnection();
            throw ex;
        }
    } // execute

    /**
     * Returns the connection back to the connection manager
     * and prepares for retrieving a new connection during
     * the next request.
     */
    protected void releaseConnection() {
        // Release the connection through the ManagedConnection instead of the
        // ConnectionManager directly.  This lets the connection control how
        // it is released.
        try {
            managedConn.releaseConnection();
        } catch(IOException ignored) {
            this.log.debug("IOException releasing connection", ignored);
        }
        managedConn = null;
    }

    /**
     * Determines the route for a request.
     * Called by {@link #execute}
     * to determine the route for either the original or a followup request.
     *
     * @param target    the target host for the request.
     *                  Implementations may accept <code>null</code>
     *                  if they can still determine a route, for example
     *                  to a default target or by inspecting the request.
     * @param request   the request to execute
     * @param context   the context to use for the execution,
     *                  never <code>null</code>
     *
     * @return  the route the request should take
     *
     * @throws HttpException    in case of a problem
     */
    protected HttpRoute determineRoute(HttpHost    target,
                                           HttpRequest request,
                                           HttpContext context)
        throws HttpException {

        if (target == null) {
            target = (HttpHost) request.getParams().getParameter(
                ClientPNames.DEFAULT_HOST);
        }
        if (target == null) {
            throw new IllegalStateException
                ("Target host must not be null, or set in parameters.");
        }

        return this.routePlanner.determineRoute(target, request, context);
    }


    /**
     * Establishes the target route.
     *
     * @param route     the route to establish
     * @param context   the context for the request execution
     *
     * @throws HttpException    in case of a problem
     * @throws IOException      in case of an IO problem
     */
    protected void establishRoute(HttpRoute route, HttpContext context)
        throws HttpException, IOException {

        HttpRouteDirector rowdy = new BasicRouteDirector();
        int step;
        do {
            HttpRoute fact = managedConn.getRoute();
            step = rowdy.nextStep(route, fact);

            switch (step) {

            case HttpRouteDirector.CONNECT_TARGET:
            case HttpRouteDirector.CONNECT_PROXY:
                managedConn.open(route, context, this.params);
                break;

            case HttpRouteDirector.TUNNEL_TARGET: {
                boolean secure = createTunnelToTarget(route, context);
                this.log.debug("Tunnel to target created.");
                managedConn.tunnelTarget(secure, this.params);
            }   break;

            case HttpRouteDirector.TUNNEL_PROXY: {
                // The most simple example for this case is a proxy chain
                // of two proxies, where P1 must be tunnelled to P2.
                // route: Source -> P1 -> P2 -> Target (3 hops)
                // fact:  Source -> P1 -> Target       (2 hops)
                final int hop = fact.getHopCount()-1; // the hop to establish
                boolean secure = createTunnelToProxy(route, hop, context);
                this.log.debug("Tunnel to proxy created.");
                managedConn.tunnelProxy(route.getHopTarget(hop),
                                        secure, this.params);
            }   break;


            case HttpRouteDirector.LAYER_PROTOCOL:
                managedConn.layerProtocol(context, this.params);
                break;

            case HttpRouteDirector.UNREACHABLE:
                throw new IllegalStateException
                    ("Unable to establish route." +
                     "\nplanned = " + route +
                     "\ncurrent = " + fact);

            case HttpRouteDirector.COMPLETE:
                // do nothing
                break;

            default:
                throw new IllegalStateException
                    ("Unknown step indicator "+step+" from RouteDirector.");
            } // switch

        } while (step > HttpRouteDirector.COMPLETE);

    } // establishConnection


    /**
     * Creates a tunnel to the target server.
     * The connection must be established to the (last) proxy.
     * A CONNECT request for tunnelling through the proxy will
     * be created and sent, the response received and checked.
     * This method does <i>not</i> update the connection with
     * information about the tunnel, that is left to the caller.
     *
     * @param route     the route to establish
     * @param context   the context for request execution
     *
     * @return  <code>true</code> if the tunnelled route is secure,
     *          <code>false</code> otherwise.
     *          The implementation here always returns <code>false</code>,
     *          but derived classes may override.
     *
     * @throws HttpException    in case of a problem
     * @throws IOException      in case of an IO problem
     */
    protected boolean createTunnelToTarget(HttpRoute route,
                                           HttpContext context)
        throws HttpException, IOException {

        HttpHost proxy = route.getProxyHost();
        HttpHost target = route.getTargetHost();
        HttpResponse response = null;

        boolean done = false;
        while (!done) {

            done = true;

            if (!this.managedConn.isOpen()) {
                this.managedConn.open(route, context, this.params);
            }

            HttpRequest connect = createConnectRequest(route, context);
            connect.setParams(this.params);

            // Populate the execution context
            context.setAttribute(ExecutionContext.HTTP_TARGET_HOST,
                    target);
            context.setAttribute(ExecutionContext.HTTP_PROXY_HOST,
                    proxy);
            context.setAttribute(ExecutionContext.HTTP_CONNECTION,
                    managedConn);
            context.setAttribute(ClientContext.TARGET_AUTH_STATE,
                    targetAuthState);
            context.setAttribute(ClientContext.PROXY_AUTH_STATE,
                    proxyAuthState);
            context.setAttribute(ExecutionContext.HTTP_REQUEST,
                    connect);

            this.requestExec.preProcess(connect, this.httpProcessor, context);

            response = this.requestExec.execute(connect, this.managedConn, context);

            response.setParams(this.params);
            this.requestExec.postProcess(response, this.httpProcessor, context);

            int status = response.getStatusLine().getStatusCode();
            if (status < 200) {
                throw new HttpException("Unexpected response to CONNECT request: " +
                        response.getStatusLine());
            }

            CredentialsProvider credsProvider = (CredentialsProvider)
                context.getAttribute(ClientContext.CREDS_PROVIDER);

            if (credsProvider != null && HttpClientParams.isAuthenticating(params)) {
                if (this.proxyAuthHandler.isAuthenticationRequested(response, context)) {

                    this.log.debug("Proxy requested authentication");
                    Map<String, Header> challenges = this.proxyAuthHandler.getChallenges(
                            response, context);
                    try {
                        processChallenges(
                                challenges, this.proxyAuthState, this.proxyAuthHandler,
                                response, context);
                    } catch (AuthenticationException ex) {
                        if (this.log.isWarnEnabled()) {
                            this.log.warn("Authentication error: " +  ex.getMessage());
                            break;
                        }
                    }
                    updateAuthState(this.proxyAuthState, proxy, credsProvider);

                    if (this.proxyAuthState.getCredentials() != null) {
                        done = false;

                        // Retry request
                        if (this.reuseStrategy.keepAlive(response, context)) {
                            this.log.debug("Connection kept alive");
                            // Consume response content
                            HttpEntity entity = response.getEntity();
                            if (entity != null) {
                                entity.consumeContent();
                            }
                        } else {
                            this.managedConn.close();
                        }

                    }

                } else {
                    // Reset proxy auth scope
                    this.proxyAuthState.setAuthScope(null);
                }
            }
        }

        int status = response.getStatusLine().getStatusCode(); // can't be null

        if (status > 299) {

            // Buffer response content
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                response.setEntity(new BufferedHttpEntity(entity));
            }

            this.managedConn.close();
            throw new TunnelRefusedException("CONNECT refused by proxy: " +
                    response.getStatusLine(), response);
        }

        this.managedConn.markReusable();

        // How to decide on security of the tunnelled connection?
        // The socket factory knows only about the segment to the proxy.
        // Even if that is secure, the hop to the target may be insecure.
        // Leave it to derived classes, consider insecure by default here.
        return false;

    } // createTunnelToTarget



    /**
     * Creates a tunnel to an intermediate proxy.
     * This method is <i>not</i> implemented in this class.
     * It just throws an exception here.
     *
     * @param route     the route to establish
     * @param hop       the hop in the route to establish now.
     *                  <code>route.getHopTarget(hop)</code>
     *                  will return the proxy to tunnel to.
     * @param context   the context for request execution
     *
     * @return  <code>true</code> if the partially tunnelled connection
     *          is secure, <code>false</code> otherwise.
     *
     * @throws HttpException    in case of a problem
     * @throws IOException      in case of an IO problem
     */
    protected boolean createTunnelToProxy(HttpRoute route, int hop,
                                          HttpContext context)
        throws HttpException, IOException {

        // Have a look at createTunnelToTarget and replicate the parts
        // you need in a custom derived class. If your proxies don't require
        // authentication, it is not too hard. But for the stock version of
        // HttpClient, we cannot make such simplifying assumptions and would
        // have to include proxy authentication code. The HttpComponents team
        // is currently not in a position to support rarely used code of this
        // complexity. Feel free to submit patches that refactor the code in
        // createTunnelToTarget to facilitate re-use for proxy tunnelling.

        throw new UnsupportedOperationException
            ("Proxy chains are not supported.");
    }



    /**
     * Creates the CONNECT request for tunnelling.
     * Called by {@link #createTunnelToTarget createTunnelToTarget}.
     *
     * @param route     the route to establish
     * @param context   the context for request execution
     *
     * @return  the CONNECT request for tunnelling
     */
    protected HttpRequest createConnectRequest(HttpRoute route,
                                               HttpContext context) {
        // see RFC 2817, section 5.2 and
        // INTERNET-DRAFT: Tunneling TCP based protocols through
        // Web proxy servers

        HttpHost target = route.getTargetHost();

        String host = target.getHostName();
        int port = target.getPort();
        if (port < 0) {
            Scheme scheme = connManager.getSchemeRegistry().
                getScheme(target.getSchemeName());
            port = scheme.getDefaultPort();
        }

        StringBuilder buffer = new StringBuilder(host.length() + 6);
        buffer.append(host);
        buffer.append(':');
        buffer.append(Integer.toString(port));

        String authority = buffer.toString();
        ProtocolVersion ver = HttpProtocolParams.getVersion(params);
        HttpRequest req = new BasicHttpRequest
            ("CONNECT", authority, ver);

        return req;
    }


    /**
     * Analyzes a response to check need for a followup.
     *
     * @param roureq    the request and route.
     * @param response  the response to analayze
     * @param context   the context used for the current request execution
     *
     * @return  the followup request and route if there is a followup, or
     *          <code>null</code> if the response should be returned as is
     *
     * @throws HttpException    in case of a problem
     * @throws IOException      in case of an IO problem
     */
    protected RoutedRequest handleResponse(RoutedRequest roureq,
                                           HttpResponse response,
                                           HttpContext context)
        throws HttpException, IOException {

        HttpRoute route = roureq.getRoute();
        RequestWrapper request = roureq.getRequest();

        HttpParams params = request.getParams();
        if (HttpClientParams.isRedirecting(params) &&
                this.redirectHandler.isRedirectRequested(response, context)) {

            if (redirectCount >= maxRedirects) {
                throw new RedirectException("Maximum redirects ("
                        + maxRedirects + ") exceeded");
            }
            redirectCount++;

            // Virtual host cannot be used any longer
            virtualHost = null;

            URI uri = this.redirectHandler.getLocationURI(response, context);

            HttpHost newTarget = new HttpHost(
                    uri.getHost(),
                    uri.getPort(),
                    uri.getScheme());

            // Unset auth scope
            targetAuthState.setAuthScope(null);
            proxyAuthState.setAuthScope(null);

            // Invalidate auth states if redirecting to another host
            if (!route.getTargetHost().equals(newTarget)) {
                targetAuthState.invalidate();
                AuthScheme authScheme = proxyAuthState.getAuthScheme();
                if (authScheme != null && authScheme.isConnectionBased()) {
                    proxyAuthState.invalidate();
                }
            }

            HttpRedirect redirect = new HttpRedirect(request.getMethod(), uri);
            HttpRequest orig = request.getOriginal();
            redirect.setHeaders(orig.getAllHeaders());

            RequestWrapper wrapper = new RequestWrapper(redirect);
            wrapper.setParams(params);

            HttpRoute newRoute = determineRoute(newTarget, wrapper, context);
            RoutedRequest newRequest = new RoutedRequest(wrapper, newRoute);

            if (this.log.isDebugEnabled()) {
                this.log.debug("Redirecting to '" + uri + "' via " + newRoute);
            }

            return newRequest;
        }

        CredentialsProvider credsProvider = (CredentialsProvider)
            context.getAttribute(ClientContext.CREDS_PROVIDER);

        if (credsProvider != null && HttpClientParams.isAuthenticating(params)) {

            if (this.targetAuthHandler.isAuthenticationRequested(response, context)) {

                HttpHost target = (HttpHost)
                    context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
                if (target == null) {
                    target = route.getTargetHost();
                }

                this.log.debug("Target requested authentication");
                Map<String, Header> challenges = this.targetAuthHandler.getChallenges(
                        response, context);
                try {
                    processChallenges(challenges,
                            this.targetAuthState, this.targetAuthHandler,
                            response, context);
                } catch (AuthenticationException ex) {
                    if (this.log.isWarnEnabled()) {
                        this.log.warn("Authentication error: " +  ex.getMessage());
                        return null;
                    }
                }
                updateAuthState(this.targetAuthState, target, credsProvider);

                if (this.targetAuthState.getCredentials() != null) {
                    // Re-try the same request via the same route
                    return roureq;
                } else {
                    return null;
                }
            } else {
                // Reset target auth scope
                this.targetAuthState.setAuthScope(null);
            }

            if (this.proxyAuthHandler.isAuthenticationRequested(response, context)) {

                HttpHost proxy = route.getProxyHost();

                this.log.debug("Proxy requested authentication");
                Map<String, Header> challenges = this.proxyAuthHandler.getChallenges(
                        response, context);
                try {
                    processChallenges(challenges,
                            this.proxyAuthState, this.proxyAuthHandler,
                            response, context);
                } catch (AuthenticationException ex) {
                    if (this.log.isWarnEnabled()) {
                        this.log.warn("Authentication error: " +  ex.getMessage());
                        return null;
                    }
                }
                updateAuthState(this.proxyAuthState, proxy, credsProvider);

                if (this.proxyAuthState.getCredentials() != null) {
                    // Re-try the same request via the same route
                    return roureq;
                } else {
                    return null;
                }
            } else {
                // Reset proxy auth scope
                this.proxyAuthState.setAuthScope(null);
            }
        }
        return null;
    } // handleResponse


    /**
     * Shuts down the connection.
     * This method is called from a <code>catch</code> block in
     * {@link #execute execute} during exception handling.
     */
    private void abortConnection() {
        ManagedClientConnection mcc = managedConn;
        if (mcc != null) {
            // we got here as the result of an exception
            // no response will be returned, release the connection
            managedConn = null;
            try {
                mcc.abortConnection();
            } catch (IOException ex) {
                if (this.log.isDebugEnabled()) {
                    this.log.debug(ex.getMessage(), ex);
                }
            }
            // ensure the connection manager properly releases this connection
            try {
                mcc.releaseConnection();
            } catch(IOException ignored) {
                this.log.debug("Error releasing connection", ignored);
            }
        }
    } // abortConnection


    private void processChallenges(
            final Map<String, Header> challenges,
            final AuthState authState,
            final AuthenticationHandler authHandler,
            final HttpResponse response,
            final HttpContext context)
                throws MalformedChallengeException, AuthenticationException {

        AuthScheme authScheme = authState.getAuthScheme();
        if (authScheme == null) {
            // Authentication not attempted before
            authScheme = authHandler.selectScheme(challenges, response, context);
            authState.setAuthScheme(authScheme);
        }
        String id = authScheme.getSchemeName();

        Header challenge = challenges.get(id.toLowerCase(Locale.ENGLISH));
        if (challenge == null) {
            throw new AuthenticationException(id +
                " authorization challenge expected, but not found");
        }
        authScheme.processChallenge(challenge);
        this.log.debug("Authorization challenge processed");
    }


    private void updateAuthState(
            final AuthState authState,
            final HttpHost host,
            final CredentialsProvider credsProvider) {

        if (!authState.isValid()) {
            return;
        }

        String hostname = host.getHostName();
        int port = host.getPort();
        if (port < 0) {
            Scheme scheme = connManager.getSchemeRegistry().getScheme(host);
            port = scheme.getDefaultPort();
        }

        AuthScheme authScheme = authState.getAuthScheme();
        AuthScope authScope = new AuthScope(
                hostname,
                port,
                authScheme.getRealm(),
                authScheme.getSchemeName());

        if (this.log.isDebugEnabled()) {
            this.log.debug("Authentication scope: " + authScope);
        }
        Credentials creds = authState.getCredentials();
        if (creds == null) {
            creds = credsProvider.getCredentials(authScope);
            if (this.log.isDebugEnabled()) {
                if (creds != null) {
                    this.log.debug("Found credentials");
                } else {
                    this.log.debug("Credentials not found");
                }
            }
        } else {
            if (authScheme.isComplete()) {
                this.log.debug("Authentication failed");
                creds = null;
            }
        }
        authState.setAuthScope(authScope);
        authState.setCredentials(creds);
    }

} // class DefaultClientRequestDirector
