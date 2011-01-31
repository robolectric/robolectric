package com.xtremelabs.robolectric.util;

import org.apache.http.HttpRequest;

public interface RequestMatcher {
    public boolean matches(HttpRequest request);
}
