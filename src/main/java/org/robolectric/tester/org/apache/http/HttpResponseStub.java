package com.xtremelabs.robolectric.tester.org.apache.http;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.params.HttpParams;

import java.util.Locale;

public class HttpResponseStub implements HttpResponse {
    @Override public StatusLine getStatusLine() {
        throw new UnsupportedOperationException();

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
        throw new UnsupportedOperationException();
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
}
