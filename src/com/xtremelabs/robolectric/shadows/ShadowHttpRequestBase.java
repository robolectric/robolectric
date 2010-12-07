package com.xtremelabs.robolectric.shadows;

import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;
import org.apache.http.client.methods.HttpRequestBase;

import java.net.URI;

@Implements(HttpRequestBase.class)
public class ShadowHttpRequestBase {
    URI uri;

    @Implementation
    public URI getURI() {
        return uri;
    }

    @Implementation
    public void setURI(URI uri) {
        this.uri = uri;
    }
}
