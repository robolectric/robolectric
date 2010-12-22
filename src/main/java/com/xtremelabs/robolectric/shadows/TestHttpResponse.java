package com.xtremelabs.robolectric.shadows;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

class TestHttpResponse extends HttpResponseStub {

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

    @Override public HttpEntity getEntity() {
        return httpEntity;
    }

    public class TestHttpEntity extends HttpEntityStub {

        @Override public long getContentLength() {
            return responseBody.length();
        }

        @Override public InputStream getContent() throws IOException, IllegalStateException {
            return new ByteArrayInputStream(responseBody.getBytes());
        }
    }

    public class TestStatusLine extends StatusLineStub {
        @Override public int getStatusCode() {
            return statusCode;
        }
    }
}
