package com.xtremelabs.robolectric.shadows;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.util.Strings;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.net.http.AndroidHttpClient;

import java.io.IOException;
import java.io.InputStream;

@RunWith(WithTestDefaultsRunner.class)
public class AndroidHttpClientTest {

    @Test
    public void testNewInstance() throws Exception {
        AndroidHttpClient client = AndroidHttpClient.newInstance("foo");
        assertThat(client, not(nullValue()));
    }

    @Test
    public void testNewInstanceWithContext() throws Exception {
        AndroidHttpClient client = AndroidHttpClient.newInstance("foo", Robolectric.application);
        assertThat(client, not(nullValue()));
    }

    @Test
    public void testExecute() throws IOException {
        AndroidHttpClient client = AndroidHttpClient.newInstance("foo");
        Robolectric.addPendingHttpResponse(200, "foo");
        HttpResponse resp = client.execute(new HttpGet("/foo"));
        assertThat(resp.getStatusLine().getStatusCode(), is(200));
        assertThat(Strings.fromStream(resp.getEntity().getContent()), equalTo("foo"));
    }
}
