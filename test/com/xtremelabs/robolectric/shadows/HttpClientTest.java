package com.xtremelabs.robolectric.shadows;

import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;

@RunWith(WithTestDefaultsRunner.class)
public class HttpClientTest {
    @Test
    public void shouldGetHttpResponseFromExecute() throws Exception {
        HttpClient client = new DefaultHttpClient();
        HttpResponse response = client.execute(new HttpGet("http://google.com"));
        assertNotNull(response);
    }
}
