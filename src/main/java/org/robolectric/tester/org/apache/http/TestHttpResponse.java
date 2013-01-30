package com.xtremelabs.robolectric.tester.org.apache.http;

import com.xtremelabs.robolectric.shadows.StatusLineStub;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpVersion;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

public class TestHttpResponse extends HttpResponseStub {

    private int statusCode;
    private byte[] responseBody;
    private TestStatusLine statusLine = new TestStatusLine();
    private TestHttpEntity httpEntity = new TestHttpEntity();
    private int openEntityContentStreamCount = 0;
    private Header[] headers = new Header[0];
    private HttpParams params = new BasicHttpParams();

    public TestHttpResponse() {
        this.statusCode = 200;
        this.responseBody = new byte[0];
    }

    public TestHttpResponse(int statusCode, String responseBody) {
        this.statusCode = statusCode;
        this.responseBody = responseBody.getBytes();
    }

    public TestHttpResponse(int statusCode, String responseBody, Header... headers) {
        this(statusCode, responseBody.getBytes(), headers);
    }

    public TestHttpResponse(int statusCode, byte[] responseBody, Header... headers) {
        this.statusCode = statusCode;
        this.responseBody = responseBody.clone();
        this.headers = headers;
    }

    protected void setResponseBody(String responseBody) {
        this.responseBody = responseBody.getBytes();
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

    @Override public Header getFirstHeader(String s) {
        for (Header h : headers) {
            if (s.equalsIgnoreCase(h.getName())) {
                return h;
            }
        }
        return null;
    }

    @Override public Header getLastHeader(String s) {
        for (int i = headers.length -1; i >= 0; i--) {
            if (headers[i].getName().equalsIgnoreCase(s)) {
                return headers[i];
            }
        }
        return null;
    }

    @Override public Header[] getHeaders(String s) {
        List<Header> found = new ArrayList<Header>();
        for (Header h : headers) {
            if (h.getName().equalsIgnoreCase(s)) found.add(h);
        }
        return found.toArray(new Header[found.size()]);
    }

    @Override
    public void addHeader(Header header) {
        List<Header> temp = new ArrayList<Header>();
        Collections.addAll(temp, headers);
        temp.add(header);
        headers = temp.toArray(new Header[temp.size()]);
    }

    @Override
    public void setHeader(Header newHeader) {
        for (int i = 0; i < headers.length; i++) {
            Header header = headers[i];
            if (header.getName().equals(newHeader.getName())) {
                headers[i] = newHeader;
                return;
            }
        }
    }

    @Override public HeaderIterator headerIterator() {
        return new HeaderIterator() {
            int index = 0;

            @Override public boolean hasNext() {
                return index < headers.length;
            }

            @Override public Header nextHeader() {
                if (index >= headers.length) throw new NoSuchElementException();
                return headers[index++];
            }

            @Override public Object next() {
                return nextHeader();
            }

            @Override public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }


    @Override public HeaderIterator headerIterator(final String s) {
        return new HeaderIterator() {
            int index = 0;

            @Override public boolean hasNext() {
                return nextIndex() != -1;
            }

            private int nextIndex() {
                for (int i = index; i<headers.length; i++) {
                    if (headers[i].getName().equalsIgnoreCase(s)) {
                        return i;
                    }
                }
                return -1;
            }

            @Override public Header nextHeader() {
                index = nextIndex();
                if (index == -1) throw new NoSuchElementException();
                return headers[index++];
            }

            @Override public Object next() {
                return nextHeader();
            }

            @Override public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override public boolean containsHeader(String s) {
        return getFirstHeader(s) != null;

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
            return responseBody.length;
        }
        
        @Override public Header getContentType() {
            for (Header header : headers) {
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
            inputStream = new ByteArrayInputStream(responseBody) {
                @Override
                public void close() throws IOException {
                    openEntityContentStreamCount--;
                    super.close();
                }
            };
            return inputStream;
        }

        @Override public void writeTo(OutputStream outputStream) throws IOException {
            outputStream.write(responseBody);
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

        @Override public String toString() {
            return "TestStatusLine[" + getReasonPhrase() + "]";
        }
    }
}
