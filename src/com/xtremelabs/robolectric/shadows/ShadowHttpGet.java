package com.xtremelabs.robolectric.shadows;

import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;
import com.xtremelabs.robolectric.util.RealObject;
import org.apache.http.client.methods.HttpGet;

import java.net.URI;

@Implements(HttpGet.class)
public class ShadowHttpGet extends ShadowHttpRequestBase {
    @RealObject
    private HttpGet realHttpGet;

    public void __constructor__(URI uri) {
        realHttpGet.setURI(uri);
    }

    public void __constructor__(String uri) {
        this.__constructor__(URI.create(uri));
    }

    @Implementation
    public String getMethod() {
        return HttpGet.METHOD_NAME;
    }
}
