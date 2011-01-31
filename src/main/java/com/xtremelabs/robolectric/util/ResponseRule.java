package com.xtremelabs.robolectric.util;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

import java.io.IOException;

public interface ResponseRule {
    boolean matches(HttpRequest request);

    HttpResponse getResponse() throws HttpException, IOException;
}
