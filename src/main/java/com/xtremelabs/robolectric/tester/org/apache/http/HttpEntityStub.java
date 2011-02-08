package com.xtremelabs.robolectric.tester.org.apache.http;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class HttpEntityStub implements HttpEntity {
    @Override public boolean isRepeatable() {
        throw new UnsupportedOperationException();
    }

    @Override public boolean isChunked() {
        throw new UnsupportedOperationException();
    }

    @Override public long getContentLength() {
        throw new UnsupportedOperationException();
    }

    @Override public Header getContentType() {
        throw new UnsupportedOperationException();
    }

    @Override public Header getContentEncoding() {
        throw new UnsupportedOperationException();
    }

    @Override public InputStream getContent() throws IOException, IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override public void writeTo(OutputStream outputStream) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override public boolean isStreaming() {
        throw new UnsupportedOperationException();
    }

    @Override public void consumeContent() throws IOException {
        throw new UnsupportedOperationException();
    }

    public static interface ResponseRule {
        boolean matches(HttpRequest request);

        HttpResponse getResponse() throws HttpException, IOException;
    }
}
