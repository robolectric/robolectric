package com.xtremelabs.robolectric.shadows;

import org.apache.http.*;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringBufferInputStream;
import java.util.Locale;

class TestHttpResponse implements HttpResponse {

    private int statusCode;
    private String responseBody;
    private TestStatusLine statusLine = new TestStatusLine();
    private TestHttpEntity httpEntity = new TestHttpEntity();

    public TestHttpResponse(int statusCode, String responseBody) {
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    @Override public StatusLine getStatusLine() {
        return statusLine;
    }

    @Override public void setStatusLine(StatusLine statusLine) {
        throw new UnsupportedOperationException();
    }

    @Override public void setStatusLine(ProtocolVersion protocolVersion, int i) {
        throw new UnsupportedOperationException();
    }

    @Override public void setStatusLine(ProtocolVersion protocolVersion, int i, String s) {
        throw new UnsupportedOperationException();
    }

    @Override public void setStatusCode(int i) throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override public void setReasonPhrase(String s) throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override public HttpEntity getEntity() {
        return httpEntity;
    }

    @Override public void setEntity(HttpEntity httpEntity) {
        throw new UnsupportedOperationException();
    }

    @Override public Locale getLocale() {
        throw new UnsupportedOperationException();
    }

    @Override public void setLocale(Locale locale) {
        throw new UnsupportedOperationException();
    }

    @Override public ProtocolVersion getProtocolVersion() {
        throw new UnsupportedOperationException();
    }

    @Override public boolean containsHeader(String s) {
        throw new UnsupportedOperationException();
    }

    @Override public Header[] getHeaders(String s) {
        throw new UnsupportedOperationException();
    }

    @Override public Header getFirstHeader(String s) {
        throw new UnsupportedOperationException();
    }

    @Override public Header getLastHeader(String s) {
        throw new UnsupportedOperationException();
    }

    @Override public Header[] getAllHeaders() {
        throw new UnsupportedOperationException();
    }

    @Override public void addHeader(Header header) {
        throw new UnsupportedOperationException();
    }

    @Override public void addHeader(String s, String s1) {
        throw new UnsupportedOperationException();
    }

    @Override public void setHeader(Header header) {
        throw new UnsupportedOperationException();
    }

    @Override public void setHeader(String s, String s1) {
        throw new UnsupportedOperationException();
    }

    @Override public void setHeaders(Header[] headers) {
        throw new UnsupportedOperationException();
    }

    @Override public void removeHeader(Header header) {
        throw new UnsupportedOperationException();
    }

    @Override public void removeHeaders(String s) {
        throw new UnsupportedOperationException();
    }

    @Override public HeaderIterator headerIterator() {
        throw new UnsupportedOperationException();
    }

    @Override public HeaderIterator headerIterator(String s) {
        throw new UnsupportedOperationException();
    }

    @Override public HttpParams getParams() {
        throw new UnsupportedOperationException();
    }

    @Override public void setParams(HttpParams httpParams) {
        throw new UnsupportedOperationException();
    }

    public class TestHttpEntity implements HttpEntity {

        @Override public boolean isRepeatable() {
            throw new UnsupportedOperationException();
        }

        @Override public boolean isChunked() {
            throw new UnsupportedOperationException();
        }

        @Override public long getContentLength() {
            return responseBody.length();
        }

        @Override public Header getContentType() {
            throw new UnsupportedOperationException();
        }

        @Override public Header getContentEncoding() {
            throw new UnsupportedOperationException();
        }

        @Override public InputStream getContent() throws IOException, IllegalStateException {
            return new StringBufferInputStream(responseBody);
        }

        @Override public void writeTo(OutputStream outputStream) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override public boolean isStreaming() {
            throw new UnsupportedOperationException();
        }

        @Override public void consumeContent() throws IOException {
        }
    }

    public class TestStatusLine implements StatusLine {

        @Override public ProtocolVersion getProtocolVersion() {
            throw new UnsupportedOperationException();
        }

        @Override public int getStatusCode() {
            return statusCode;
        }

        @Override public String getReasonPhrase() {
            throw new UnsupportedOperationException();
        }
    }
}
