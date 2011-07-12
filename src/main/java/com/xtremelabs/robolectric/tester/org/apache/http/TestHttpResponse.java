package com.xtremelabs.robolectric.tester.org.apache.http;

import com.xtremelabs.robolectric.shadows.StatusLineStub;
import org.apache.http.*;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class TestHttpResponse extends HttpResponseStub {

    private int statusCode;
    private String responseBody;
    private TestStatusLine statusLine = new TestStatusLine();
    private TestHttpEntity httpEntity = new TestHttpEntity();
    private int openEntityContentStreamCount = 0;
    private Header[] headers;
    private HttpParams params = new BasicHttpParams();

    public TestHttpResponse() {
        this.statusCode = 200;
        this.responseBody = "";
    }

    public TestHttpResponse(int statusCode, String responseBody) {
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public TestHttpResponse(int statusCode, String responseBody, Header[] headers) {
        this(statusCode, responseBody);
        this.headers = headers;
    }

    protected void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    @Override public StatusLine getStatusLine() {
        return statusLine;
    }

    @Override public HttpEntity getEntity() {
        return httpEntity;
    }

    @Override public Header[] getAllHeaders() {
        return headers;
    }

    @Override public HttpParams getParams() {
        return params;
    }

    @Override public void setParams(HttpParams httpParams) {
        this.params = httpParams;
    }

    public boolean entityContentStreamsHaveBeenClosed() {
        return openEntityContentStreamCount == 0;
    }
    
    public class TestHttpEntity extends HttpEntityStub {

        private ByteArrayInputStream inputStream;

        @Override public long getContentLength() {
            return responseBody.length();
        }
        
        @Override public Header getContentType() {
            for (int i = 0; i < headers.length; i++) {
                Header header = headers[i];
                if (header.getName().equals("Content-Type")) {
                    return header;
                }
            }
            return null;
        }
        
        @Override public boolean isStreaming() {
            return true;
        }
        
        @Override public boolean isRepeatable() {
            return true;
        }

        @Override public InputStream getContent() throws IOException, IllegalStateException {
            openEntityContentStreamCount++;
            inputStream = new ByteArrayInputStream(responseBody.getBytes()) {
                @Override
                public void close() throws IOException {
                    openEntityContentStreamCount--;
                    super.close();
                }
            };
            return inputStream;
        }

        @Override public void writeTo(OutputStream outputStream) throws IOException {
            outputStream.write(responseBody.getBytes());
        }

        @Override public void consumeContent() throws IOException {
        }
    }

    public class TestStatusLine extends StatusLineStub {
        @Override public ProtocolVersion getProtocolVersion() {
            return new HttpVersion(1, 0);
        }

        @Override public int getStatusCode() {
            return statusCode;
        }

        @Override public String getReasonPhrase() {
            return "HTTP status " + statusCode;
        }
    }
}
