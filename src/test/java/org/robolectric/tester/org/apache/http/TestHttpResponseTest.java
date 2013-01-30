package com.xtremelabs.robolectric.tester.org.apache.http;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHeader;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class TestHttpResponseTest {

    @Test
    public void shouldSupportGetFirstHeader() throws Exception {
        HttpResponse resp =
                new TestHttpResponse(304, "REDIRECTED",
                        new BasicHeader("Location", "http://bar.com"));

        assertThat(resp.getFirstHeader("None"), nullValue());
        assertThat(new TestHttpResponse(200, "OK").getFirstHeader("Foo"), nullValue());

        for (String l : new String[] { "location", "Location" }) {
            assertThat(resp.getFirstHeader(l).getValue(), equalTo("http://bar.com"));
        }
    }

    @Test
    public void shouldSupportGetLastHeader() throws Exception {
        HttpResponse resp =
                new TestHttpResponse(304, "REDIRECTED",
                        new BasicHeader("Location", "http://bar.com"),
                        new BasicHeader("Location", "http://zombo.com"));

        assertThat(resp.getLastHeader("None"), nullValue());

        for (String l : new String[] { "location", "Location" }) {
            assertThat(resp.getLastHeader(l).getValue(), equalTo("http://zombo.com"));
        }
    }

    @Test
    public void shouldSupportContainsHeader() throws Exception {
        HttpResponse resp =
                new TestHttpResponse(304, "ZOMBO",
                        new BasicHeader("X-Zombo-Com", "Welcome"));

        assertThat(resp.containsHeader("X-Zombo-Com"), is(true));
        assertThat(resp.containsHeader("Location"), is(false));
    }

    @Test
    public void shouldSupportHeaderIterator() throws Exception {
        HttpResponse resp =
                new TestHttpResponse(304, "REDIRECTED",
                        new BasicHeader("Location", "http://bar.com"),
                        new BasicHeader("Location", "http://zombo.com"));

        HeaderIterator it = resp.headerIterator();

        assertThat(it.hasNext(), is(true));
        assertThat(it.nextHeader().getValue(), equalTo("http://bar.com"));
        assertThat(it.nextHeader().getValue(), equalTo("http://zombo.com"));
        assertThat(it.hasNext(), is(false));
    }

    @Test
    public void shouldSupportHeaderIteratorWithArg() throws Exception {
        HttpResponse resp =
                new TestHttpResponse(304, "REDIRECTED",
                        new BasicHeader("Location", "http://bar.com"),
                        new BasicHeader("X-Zombo-Com", "http://zombo.com"),
                        new BasicHeader("Location", "http://foo.com"));

        HeaderIterator it = resp.headerIterator("Location");

        assertThat(it.hasNext(), is(true));
        assertThat(it.nextHeader().getValue(), equalTo("http://bar.com"));
        assertThat(it.hasNext(), is(true));
        assertThat(it.nextHeader().getValue(), equalTo("http://foo.com"));
        assertThat(it.hasNext(), is(false));
    }


    @Test
    public void shouldSupportGetHeadersWithArg() throws Exception {
        HttpResponse resp =
                new TestHttpResponse(304, "REDIRECTED",
                        new BasicHeader("Location", "http://bar.com"),
                        new BasicHeader("X-Zombo-Com", "http://zombo.com"),
                        new BasicHeader("Location", "http://foo.com"));


        Header[] headers = resp.getHeaders("Location");
        assertThat(headers.length, is(2));
        assertThat(headers[0].getValue(), CoreMatchers.equalTo("http://bar.com"));
        assertThat(headers[1].getValue(), CoreMatchers.equalTo("http://foo.com"));
    }

    @Test
    public void canAddNewBasicHeader() {
        TestHttpResponse response = new TestHttpResponse(200, "abc");
        assertThat(response.getAllHeaders().length, is(0));
        response.addHeader(new BasicHeader("foo", "bar"));
        assertThat(response.getAllHeaders().length, is(1));
        assertThat(response.getHeaders("foo")[0].getValue(), CoreMatchers.equalTo("bar"));
    }
    
    @Test
    public void canOverrideExistingHeaderValue() {
        TestHttpResponse response = new TestHttpResponse(200, "abc", new BasicHeader("foo", "bar"));
        response.setHeader(new BasicHeader("foo", "bletch"));
        assertThat(response.getAllHeaders().length, is(1));
        assertThat(response.getHeaders("foo")[0].getValue(), CoreMatchers.equalTo("bletch"));
    }
    
    @Test
    public void onlyOverridesFirstHeaderValue() {
        TestHttpResponse response = new TestHttpResponse(200, "abc", new BasicHeader("foo", "bar"), new BasicHeader("foo", "baz"));
        response.setHeader(new BasicHeader("foo", "bletch"));
        assertThat(response.getAllHeaders().length, is(2));
        assertThat(response.getHeaders("foo")[0].getValue(), CoreMatchers.equalTo("bletch"));
        assertThat(response.getHeaders("foo")[1].getValue(), CoreMatchers.equalTo("baz"));
    }

}
