package com.xtremelabs.robolectric.shadows;

import org.apache.http.Header;
import org.apache.http.HttpEntity;

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
}
