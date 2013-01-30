package com.xtremelabs.robolectric.tester.org.apache.http;

import org.apache.http.HttpRequest;

public interface RequestMatcher {
    public boolean matches(HttpRequest request);
}
