package com.xtremelabs.robolectric.shadows;

import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Implements(AbstractHttpClient.class)
public class ShadowAbstractHttpClient {

    static List<HttpResponse> httpResponses = new ArrayList<HttpResponse>();
    static List<HttpUriRequest> httpRequests = new ArrayList<HttpUriRequest>();

    public static void reset() {
        httpResponses.clear();
        httpRequests.clear();
    }

    public static void addPendingResponse(int statusCode, String responseBody) {
        addPendingResponse(new TestHttpResponse(statusCode, responseBody));
    }

    public static void addPendingResponse(HttpResponse httpResponse) {
        ShadowAbstractHttpClient.httpResponses.add(httpResponse);
    }

    public static HttpUriRequest getRequest(int index) {
        return httpRequests.get(index);
    }

    @Implementation public HttpResponse execute(HttpUriRequest httpUriRequest) throws IOException, ClientProtocolException {
        if (httpResponses.isEmpty()) {
            throw new RuntimeException("Unexpected call to execute, no pending responses are available.");
        }
        httpRequests.add(httpUriRequest);
        return httpResponses.remove(0);
    }

//TODO: evaluate which of these we intend to support and kill the rest ///////////////////////////////////////////////////////////
    @Implementation public HttpParams getParams() {
        return null;
    }

    @Implementation public ClientConnectionManager getConnectionManager() {
        return null;
    }

    @Implementation
    public HttpResponse execute(HttpUriRequest httpUriRequest, HttpContext httpContext) throws IOException, ClientProtocolException {
        return null;
    }

    @Implementation
    public HttpResponse execute(HttpHost httpHost, HttpRequest httpRequest) throws IOException, ClientProtocolException {
        return null;
    }

    @Implementation
    public HttpResponse execute(HttpHost httpHost, HttpRequest httpRequest, HttpContext httpContext) throws IOException, ClientProtocolException {
        return null;
    }

    @Implementation
    public <T> T execute(HttpUriRequest httpUriRequest, ResponseHandler<? extends T> responseHandler) throws IOException, ClientProtocolException {
        return null;
    }

    @Implementation
    public <T> T execute(HttpUriRequest httpUriRequest, ResponseHandler<? extends T> responseHandler, HttpContext httpContext) throws IOException, ClientProtocolException {
        return null;
    }

    @Implementation
    public <T> T execute(HttpHost httpHost, HttpRequest httpRequest, ResponseHandler<? extends T> responseHandler) throws IOException, ClientProtocolException {
        return null;
    }

    @Implementation
    public <T> T execute(HttpHost httpHost, HttpRequest httpRequest, ResponseHandler<? extends T> responseHandler, HttpContext httpContext) throws IOException, ClientProtocolException {
        return null;
    }
}
