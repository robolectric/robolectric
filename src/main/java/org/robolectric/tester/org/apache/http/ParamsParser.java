package com.xtremelabs.robolectric.tester.org.apache.http;

import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class ParamsParser {
    public static Map<String, String> parseParams(HttpRequest request) {
        URI uri = URI.create(request.getRequestLine().getUri());
        String rawQuery = uri.getRawQuery();
        Map<String, String> params;
        if (rawQuery != null) {
            params = parseParamsFromQuery(rawQuery);
        } else if (request instanceof HttpPost && ((HttpPost) request).getEntity() != null) {
            try {
                params = parseParamsFromQuery(EntityUtils.toString(((HttpPost) request).getEntity()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            HttpParams httpParams = request.getParams();
            if (httpParams instanceof BasicHttpParams) {
                Map<String, String> parameters = getPrivateMember(httpParams, "parameters");
                params = new LinkedHashMap<String, String>(parameters);
            }
            else {
                throw new RuntimeException("Was expecting a "+BasicHttpParams.class.getName());
            }
            return params;
        }
        return params;
    }

    private static Map<String, String> parseParamsFromQuery(String rawQuery) {
        Map<String, String> params = new LinkedHashMap<String, String>();
        StringTokenizer tok = new StringTokenizer(rawQuery, "&", false);
        while (tok.hasMoreTokens()) {
            String name, value;
            String nextParam = tok.nextToken();
            if (nextParam.contains("=")) {
                String[] nameAndValue = nextParam.split("=");
                name = nameAndValue[0];
                try {
                    value = URLDecoder.decode(nameAndValue[1], "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            } else {
                name = nextParam;
                value = "";
            }
            params.put(name, value);
        }
        return params;
    }

    private static <T> T getPrivateMember(Object obj, String name) {
        try {
            Field f = obj.getClass().getDeclaredField(name);
            f.setAccessible(true);
            //noinspection unchecked
            return (T) f.get(obj);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
